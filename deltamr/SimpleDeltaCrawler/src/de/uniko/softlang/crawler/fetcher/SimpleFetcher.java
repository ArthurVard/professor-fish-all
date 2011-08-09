package de.uniko.softlang.crawler.fetcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.fetcher.nutch.Content;
import de.uniko.softlang.crawler.fetcher.nutch.ProtocolStatus;
import de.uniko.softlang.utils.Pair;

public class SimpleFetcher extends Configured implements Tool, Fetcher {
	public static final Log LOG = LogFactory.getLog(SimpleFetcher.class);
	
	public static class SimpleFetcherMapper extends Mapper<Text, CrawlDbData, Text, SiteVersion> {
	//MultithreadedMapper<Text, CrawlDbData, Text, SiteVersion> {
		private long fetchTime;
		

		protected void setup(Context context) throws IOException,
				InterruptedException {
			this.fetchTime = context.getConfiguration().getLong(FETCH_TIME, 0);
		}

		public void map(Text key, CrawlDbData value, Context context)
				throws InterruptedException, IOException {

			ContentDelta emptyContent = new DocumentLevelDelta(new Text());
			HttpFetchUsingNutch fetcher = new HttpFetchUsingNutch();
			ProtocolOutput pOut;
			pOut = fetcher.fetchContentUsingNutchJar(key, value.getLastModified(), context.getConfiguration());
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
				//status.getCode();
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
			DocumentLevelDelta allContent = new DocumentLevelDelta(cleanContent);

			SiteContent newContent = new SiteContent(key, 0, allContent); // document-level diff on content
			SiteMetadata newMeta = new SiteMetadata(key, 0, new DeltaLink[0]);
			newMeta.setFetchTime(fetchTime);
			SiteVersion	v = new SiteVersion(newMeta,newContent);

			// parse for outlinks 
			List<Pair<String, String>> links = parser.parseLinks(contentString);
			List<DeltaLink> linkDiff = Differ.createLinkDiff(links, new TreeMap<Integer,SiteVersion>()); 
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
		}
		
	}

	public static class SimpleFetcherReducer
			extends
			Reducer<Text, SiteVersion, Text, SiteVersion> {
		protected void reduce(Text key,
				Iterable<SiteVersion> values,
				Context context) throws IOException, InterruptedException {
			for (SiteVersion val : values) {
				context.write(key, val);
			}
		}
	}

	public SimpleFetcher(){ }
	
	public SimpleFetcher(Configuration conf){
		setConf(conf);
	}
	
	public Path fetch(Path segmentBase) throws Exception {
		if(getConf() == null)
			setConf(new Configuration());
		long currTime = System.currentTimeMillis();
		getConf().setLong(FETCH_TIME, currTime);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("SimpleFetcher");

		job.setJarByClass(SimpleFetcher.class);

		// input
		Path generateDir = new Path(segmentBase, Generator.GENERATOR_DIR);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		SequenceFileInputFormat.addInputPath(job, generateDir);

		job.setMapperClass(MultithreadedMapper.class);
		MultithreadedMapper.setMapperClass(job, SimpleFetcherMapper.class);
		MultithreadedMapper.setNumberOfThreads(job, 10);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(SiteVersion.class);
		job.setReducerClass(SimpleFetcherReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);

		// output
		Path dataDir = new Path(segmentBase, DATA_DIR);
		Path outDir = new Path(dataDir + "/" + currTime);
		FetcherOutputFormat.setOutputPath(job, outDir);
		job.setOutputFormatClass(FetcherOutputFormat.class);
		job.setOutputKeyClass(Text.class); // the version
		job.setOutputValueClass(SiteVersion.class); // the complete site or a delta

		boolean success = job.waitForCompletion(true);
		return success ? outDir : null;
	}

	public static void usage(){
		System.err.println("Usage: SimpleFetcher <base-dir>");
	}
	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
			return -1;
		}
		try {
			fetch(new Path(args[0]));
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleFetcher: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new SimpleFetcher(), args);
		System.exit(res);
	}

}
