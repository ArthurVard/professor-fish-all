package de.uniko.softlang.crawler.fetcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.protocol.ProtocolOutput;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.CrawlDbUpdater;
import de.uniko.softlang.crawler.Generator;
import de.uniko.softlang.crawler.datastores.CrawlDbData;
import de.uniko.softlang.crawler.datastores.SimpleLink;
import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.SiteVersion;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;
import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.delta.WordLevelDelta;
import de.uniko.softlang.crawler.fetcher.nutch.ProtocolStatus;
import de.uniko.softlang.utils.Pair;

public class DeltaFetcher extends Configured implements Tool, Fetcher {
	public static final Log LOG = LogFactory.getLog(DeltaFetcher.class);
	
	public static class DeltaFetcherMapper extends Mapper<Text,TupleWritable, Text, SiteVersion>{
	//MultithreadedMapper<Text,TupleWritable, Text, SiteVersion>{
		private long fetchTime;
		private DeltaType deltaLvl;
		float deltaSize;

		

		protected void setup(Context context) throws IOException,
				InterruptedException {
			this.fetchTime = context.getConfiguration().getLong(FETCH_TIME, 0);
			this.deltaLvl = DeltaType.decode(context.getConfiguration().getInt(DELTA_LVL, 0));
			this.deltaSize = ((float)context.getConfiguration().getInt(BaselineFetcher.DELTA_SIZE, 100))/100f;
		}

		public void map(Text key, TupleWritable values, Context context)
				throws InterruptedException, IOException {

			ContentDelta emptyContent = new DocumentLevelDelta(new Text());
			VersionedSiteData generateData = null;
			TreeMap<Integer, SiteVersion> oldData = new TreeMap<Integer, SiteVersion>();
			boolean firstFetch = false;

			// collect old data and generate-data
			for (Writable val : values) {
				if (val instanceof SiteContent) {
					SiteContent content = (SiteContent) val;
					int version = content.getVersion();
					if (!oldData.containsKey(version)) {
						oldData.put(version,new SiteVersion());
					}
					oldData.get(version).setContent(content);
				} else if (val instanceof SiteMetadata) {
					SiteMetadata meta = (SiteMetadata) val;
					int version = meta.getVersion();
					if (version != -1) { // otherwise it's only a previously discorvered
																// link
						if (!oldData.containsKey(version)) {
							oldData.put(version, new SiteVersion());
						}
						oldData.get(version).setMeta(meta);
					}
				} else if (val instanceof VersionedSiteData) {
					generateData = (VersionedSiteData) val;
				} else {
					LOG.error("Illegal input for Mapper for key " + key.toString() + ": "
							+ val.getClass());
				}
			}
			if (generateData == null) {
				// not due to fetch
				return;
			}
			if (oldData.size() == 0) {
				// first time we fetch this site
				firstFetch = true;
			} else {
				firstFetch = false;
			}
			// fetch it from Web
//			HttpFetch fetcher = new HttpFetch(context.getConfiguration());
//			Content c;
//			c = fetcher.fetchContentUsingNutch(key,
//					((CrawlDbData) generateData).getLastModified());
			HttpFetchUsingNutch fetcher = new HttpFetchUsingNutch();
			ProtocolOutput pOut;
			pOut = fetcher.fetchContentUsingNutchJar(key, ((CrawlDbData) generateData).getLastModified(), context.getConfiguration());
			org.apache.nutch.protocol.ProtocolStatus status = pOut.getStatus();
			org.apache.nutch.protocol.Content content = pOut.getContent();
			
			
			if(!fetcher.shouldRefetch(status)){
				context.write(key, new SiteVersion(new SiteMetadata(key, CrawlDbUpdater.BLOCKED, new DeltaLink[0]), new SiteContent(key,-1, emptyContent)));
				if(status.getCode() == ProtocolStatus.MOVED || status.getCode() == ProtocolStatus.TEMP_MOVED){
					//create a link-entry for the CrawlDb so this URL is fetched during the next crawl instead
					String newUrl = status.getMessage();
					try{
						URL u = new URL(newUrl);
						Text newTarget = new Text(newUrl);
						SiteVersion newLink = new SiteVersion(new SimpleLink(newTarget), new SiteContent(newTarget,-1, emptyContent));
						context.write(newTarget, newLink);
					}catch(MalformedURLException e){
						//URL is not valid -> skip it
					}
					
				}
			}

			String contentString = "";
			if(content == null || content.getContent() == null){
				// no content fetched, reason can be found via: 
				// status.getCode();
				return;
			}else{	
				contentString = new String(content.getContent());
				if (contentString.equals("")) 
					return;
			}

			//parse content: remove html-related stuff and stop-words
			HttpParser parser = new SimpleHttpParser();
			String parsedContent = parser.removeHtmlElements(contentString);
			String cleanContent = parser.cleanText(parsedContent);
			
			//create the content delta 
			ContentDelta diff= Differ.createContentDiff(cleanContent, oldData, deltaLvl);

			//if a given delta size is set, artificially create a delta
			boolean writeDelta = false;
			double randomValue = Math.random();
			if(deltaSize < 1 && randomValue < deltaSize){
				writeDelta = true;
			}else if(deltaSize == 1){
				writeDelta = true;
			}
				
			if (!diff.isEmpty() && writeDelta) {
				// if last version is different from this one, calculate link-delta
				// then write content- and link-delta
				SiteVersion v;
				int version = firstFetch ? 0 : oldData.lastKey() + 1; // calculate a new
																															// version-id
				SiteContent newContent = new SiteContent(key, version, diff); // document-level diff on content
				SiteMetadata newMeta = new SiteMetadata(key, version, new DeltaLink[0]);
				newMeta.setFetchTime(fetchTime);
				v = new SiteVersion(newMeta,newContent);

				// parse for outlinks and create link-delta
				List<Pair<String, String>> links = parser.parseLinks(contentString);
				List<DeltaLink> linkDiff = Differ.createLinkDiff(links, oldData); 
				((SiteMetadata) v.getMeta()).setLinks(linkDiff.toArray(new DeltaLink[linkDiff.size()]));
				((SiteMetadata) v.getMeta()).setModified(true);

				// Create a link-entry for each newly discovered link,
				// in case there is no crawlDb entry for it yet.
				for (DeltaLink link : linkDiff) {
					if (link.getSign().get()) {
						SiteVersion newLink = new SiteVersion(new SimpleLink(link.getUrl()), new SiteContent(link.getUrl(),-1, emptyContent));
						context.write(new Text(link.getUrl()), newLink);
					}
				}
				context.write(key, v);
			}	else if(writeDelta){
				//diff was empty, but we are supposed to write a delta
				if(deltaLvl == DeltaType.DOC_LVL){
					diff = new DocumentLevelDelta(cleanContent);
				}else{
					int cutIndex = (int)Math.ceil(cleanContent.length() * deltaSize);
					diff = new WordLevelDelta(cleanContent.substring(0, cutIndex));
				}
				SiteVersion v;
				int version = firstFetch ? 0 : oldData.lastKey() + 1; // calculate a new
				SiteContent newContent = new SiteContent(key, version, diff); // document-level diff on content
				SiteMetadata newMeta = new SiteMetadata(key, version, new DeltaLink[0]);
				newMeta.setFetchTime(fetchTime);
				v = new SiteVersion(newMeta,newContent);

				// parse for outlinks and create link-delta
				List<DeltaLink> linkDiff = Differ.createLinkDiff(new LinkedList(), oldData); 
				int cutList = (int)Math.ceil(linkDiff.size()*deltaSize);
				List<DeltaLink> newDiff = new LinkedList<DeltaLink>();
				Iterator<DeltaLink> it = linkDiff.iterator();
				for(int i = 0; i < cutList; i++){
					newDiff.add(it.next());
				}
				((SiteMetadata) v.getMeta()).setLinks(newDiff.toArray(new DeltaLink[newDiff.size()]));
				((SiteMetadata) v.getMeta()).setModified(true);
				context.write(key, v);
			
			}
			// if there is no difference, then no need to write the site
			// TODO maybe write "lastFetched"
		}
	}

	public static class DeltaFetcherReducer extends Reducer<Text, SiteVersion, Text, SiteVersion> {
		protected void reduce(Text key,
				Iterable<SiteVersion> values,
				Context context) throws IOException, InterruptedException {
			for (SiteVersion val : values) {
				context.write(key, val);
			}
		}
	}

	public DeltaFetcher(){ }
	
	public DeltaFetcher(Configuration conf){
		setConf(conf);
	}
	
	public Path fetch(Path segmentBase) throws Exception {
		return fetch(segmentBase, 100);
	}
	public Path fetch(Path segmentBase, int deltaSize) throws Exception {
		if(getConf() == null)
			setConf(new Configuration());
		long currTime = System.currentTimeMillis();
		getConf().setLong(FETCH_TIME, currTime);
		getConf().setInt(BaselineFetcher.DELTA_SIZE, deltaSize);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		String jobName = "DeltaFetcher-";
		DeltaType lvl = DeltaType.decode(getConf().getInt(DELTA_LVL, 0));
		if(lvl.equals(DeltaType.WORD_LVL))
			jobName += "WordLevel";
		else
			jobName += "DocLevel";	
		job.setJobName(jobName);
		job.setJarByClass(DeltaFetcher.class);

		// input
		List<Path> plist = new LinkedList<Path>();
		Path generateDir = new Path(segmentBase, Generator.GENERATOR_DIR);
		plist.add(generateDir);

		Path dataDir = new Path(segmentBase, DATA_DIR);
		FileSystem fs = dataDir.getFileSystem(getConf());
		FileStatus[] oldVersions = fs.listStatus(dataDir);

		for (int i = 0; i < oldVersions.length; i++) {
			Path content = new Path(oldVersions[i].getPath(), CONTENT_DIR);
			Path meta = new Path(oldVersions[i].getPath(), META_DIR);
			if(fs.exists(content))
				plist.add(content);
			if(fs.exists(meta))
				plist.add(meta);
		}
		job.setInputFormatClass(CompositeInputFormat.class);
		job.getConfiguration().set(
				CompositeInputFormat.JOIN_EXPR,
				CompositeInputFormat.compose("outer", SequenceFileInputFormat.class,
						plist.toArray(new Path[0])));

		// mapper
//		job.setMapperClass(BruteFetcherMapper.class);
		job.setMapperClass(MultithreadedMapper.class);
		MultithreadedMapper.setMapperClass(job, DeltaFetcherMapper.class);
		MultithreadedMapper.setNumberOfThreads(job, 10);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(SiteVersion.class);
		job.setReducerClass(DeltaFetcherReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);

		// output
		Path outDir = new Path(dataDir + "/" + currTime);
		FetcherOutputFormat.setOutputPath(job, outDir);
		job.setOutputFormatClass(FetcherOutputFormat.class);
		job.setOutputKeyClass(Text.class); // the version
		job.setOutputValueClass(SiteVersion.class); // the complete site or a delta

		boolean success = job.waitForCompletion(true);
		return success ? outDir : null;
	}

	public static void usage(){
		System.err.println("Usage: DeltaFetcher <base-dir> [<delta_percentage>]");
	}
	
	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
			return -1;
		}
		try {
			int deltaSize = 100;
			if(args.length > 1)
				deltaSize = Integer.parseInt(args[1]);
			fetch(new Path(args[0]), deltaSize);
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleFetcher: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DeltaFetcher(), args);
		System.exit(res);
	}

}
