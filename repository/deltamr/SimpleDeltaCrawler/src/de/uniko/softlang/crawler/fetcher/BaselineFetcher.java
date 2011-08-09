package de.uniko.softlang.crawler.fetcher;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
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

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.Generator;
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
import de.uniko.softlang.utils.Pair;

public class BaselineFetcher extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(BaselineFetcher.class);
	private static final String USE_DELTA = "uniko.use.deltas";
	public static final String DELTA_SIZE = "uniko.delta.size";
	
	protected static enum MyCounter {
		NO_OLD_DATA
	};
	
	public static class BaselineFetcherMapper extends
			Mapper<Text, TupleWritable, Text, SiteVersion> {
		// MultithreadedMapper<Text,TupleWritable, Text, SiteVersion>{
		private long fetchTime;
		private ContentDelta.DeltaType deltaLvl;
		boolean useDeltas;
		float deltaSize;

		protected void setup(Context context) throws IOException,
				InterruptedException {
			this.fetchTime = context.getConfiguration().getLong(Fetcher.FETCH_TIME, 0);
			this.deltaLvl = ContentDelta.DeltaType.decode(context.getConfiguration()
					.getInt(Fetcher.DELTA_LVL, 0));
			this.useDeltas = context.getConfiguration().getBoolean(USE_DELTA, false);
			this.deltaSize = ((float)context.getConfiguration().getInt(DELTA_SIZE, 0))/100f;
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
						oldData.put(version, new SiteVersion());
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
				// no data can be written
				context.getCounter(MyCounter.NO_OLD_DATA).increment(1);
				return;
			} else {
				firstFetch = false;
			}

			String contentString = Differ.extractLatestContent(oldData, deltaLvl);

			// parse content: remove html-related stuff and stop-words
			HttpParser parser = new SimpleHttpParser();
			String parsedContent = parser.removeHtmlElements(contentString);
			String cleanContent = parser.cleanText(parsedContent);

			if (!useDeltas) {
				DocumentLevelDelta allContent = new DocumentLevelDelta(cleanContent);

				SiteContent newContent = new SiteContent(key, 0, allContent); // document-level
																																			// diff on
																																			// content
				SiteMetadata newMeta = new SiteMetadata(key, 0, new DeltaLink[0]);
				newMeta.setFetchTime(fetchTime);
				SiteVersion v = new SiteVersion(newMeta, newContent);

				// parse for outlinks
				Map<Pair<String, String>, Integer> linkMap = Differ.extractLatestLinks(oldData);
				List<Pair<String, String>> linkList = new LinkedList<Pair<String,String>>();
				for (Pair<String,String> p : linkMap.keySet()) {
					for (int i = 0; i < Math.abs(linkMap.get(p)); i++) {
						linkList.add(p);
					}
				}
				List<DeltaLink> linkDiff = Differ.createLinkDiff(linkList,
						new TreeMap<Integer, SiteVersion>());
				((SiteMetadata) v.getMeta()).setLinks(linkDiff
						.toArray(new DeltaLink[linkDiff.size()]));
				((SiteMetadata) v.getMeta()).setModified(true);

				for (DeltaLink link : linkDiff) {
					if (link.getSign().get()) {
						SiteVersion newLink = new SiteVersion(
								new SimpleLink(link.getUrl()), new SiteContent(link.getUrl(),
										-1, emptyContent));
						context.write(new Text(link.getUrl()), newLink);
					}
				}
				context.write(key, v);
			} else {
				// create the content delta
				ContentDelta diff = Differ.createContentDiff(cleanContent, oldData,
						deltaLvl);
				// as the delta will always be empty, artificially create a delta
				double randomValue = Math.random();
				if(randomValue < deltaSize){
					if(deltaLvl == DeltaType.DOC_LVL){
						diff = new DocumentLevelDelta(cleanContent);
					}else{
						diff = new WordLevelDelta(cleanContent);
					}
				}
				
				if (!diff.isEmpty()) {
					// if last version is different from this one, calculate link-delta
					// then write content- and link-delta
					SiteVersion v;
					int version = firstFetch ? 0 : oldData.lastKey() + 1; // calculate a
																																// new
																																// version-id
					SiteContent newContent = new SiteContent(key, version, diff); // document-level
																																				// diff
																																				// on
																																				// content
					SiteMetadata newMeta = new SiteMetadata(key, version,
							new DeltaLink[0]);
					newMeta.setFetchTime(fetchTime);
					v = new SiteVersion(newMeta, newContent);

					// parse for outlinks and create link-delta
					Map<Pair<String, String>, Integer> linkMap = Differ.extractLatestLinks(oldData);
					List<Pair<String, String>> linkList = new LinkedList<Pair<String,String>>();
					for (Pair<String,String> p : linkMap.keySet()) {
						for (int i = 0; i < Math.abs(linkMap.get(p)); i++) {
							linkList.add(p);
						}
					}
					List<DeltaLink> linkDiff = Differ.createLinkDiff(linkList, oldData);
					if(randomValue < deltaSize){
						for(Pair<String, String> p : linkMap.keySet()){
							linkDiff.add(new DeltaLink(new Text(p.getFirst()), new BooleanWritable(true), new Text(p.getSecond())));
						}
					}
						
					((SiteMetadata) v.getMeta()).setLinks(linkDiff
							.toArray(new DeltaLink[linkDiff.size()]));
					((SiteMetadata) v.getMeta()).setModified(true);

					// Create a link-entry for each newly discovered link,
					// in case there is no crawlDb entry for it yet.
					for (DeltaLink link : linkDiff) {
						if (link.getSign().get()) {
							SiteVersion newLink = new SiteVersion(new SimpleLink(
									link.getUrl()), new SiteContent(link.getUrl(), -1,
									emptyContent));
							context.write(new Text(link.getUrl()), newLink);
						}
					}
					context.write(key, v);
				}
				// if there is no difference, then no need to write the site
				// TODO maybe write "lastFetched"
			}
		}
	}

	public static class BaselineFetcherReducer extends
			Reducer<Text, SiteVersion, Text, SiteVersion> {
		protected void reduce(Text key, Iterable<SiteVersion> values,
				Context context) throws IOException, InterruptedException {
			for (SiteVersion val : values) {
				context.write(key, val);
			}
		}
	}

	public BaselineFetcher() {
	}

	public BaselineFetcher(Configuration conf) {
		setConf(conf);
	}

	public Path fetch(Path segmentBase, boolean useDeltas, int deltaSize) throws Exception {
		if (getConf() == null)
			setConf(new Configuration());
		long currTime = System.currentTimeMillis();
		getConf().setLong(Fetcher.FETCH_TIME, currTime);
		getConf().setBoolean(USE_DELTA, useDeltas);
		getConf().setInt(DELTA_SIZE, deltaSize);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("DeltaFetcher");
		job.setJarByClass(BaselineFetcher.class);

		// input
		List<Path> plist = new LinkedList<Path>();
		Path generateDir = new Path(segmentBase, Generator.GENERATOR_DIR);
		plist.add(generateDir);

		Path dataDir = new Path(segmentBase, Fetcher.DATA_DIR);
		FileSystem fs = dataDir.getFileSystem(getConf());
		FileStatus[] oldVersions = fs.listStatus(dataDir);

		for (int i = 0; i < oldVersions.length; i++) {
			Path content = new Path(oldVersions[i].getPath(), Fetcher.CONTENT_DIR);
			Path meta = new Path(oldVersions[i].getPath(), Fetcher.META_DIR);
			if (fs.exists(content))
				plist.add(content);
			if (fs.exists(meta))
				plist.add(meta);
		}
		job.setInputFormatClass(CompositeInputFormat.class);
		job.getConfiguration().set(
				CompositeInputFormat.JOIN_EXPR,
				CompositeInputFormat.compose("outer", SequenceFileInputFormat.class,
						plist.toArray(new Path[0])));

		// mapper
		//job.setMapperClass(BaselineFetcherMapper.class);
		job.setMapperClass(MultithreadedMapper.class);
		MultithreadedMapper.setMapperClass(job, BaselineFetcherMapper.class);
		MultithreadedMapper.setNumberOfThreads(job, 10);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(SiteVersion.class);
		job.setReducerClass(BaselineFetcherReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);

		// output
		Path outDir = new Path(dataDir + "/" + currTime);
		FetcherOutputFormat.setOutputPath(job, outDir);
		job.setOutputFormatClass(FetcherOutputFormat.class);
		job.setOutputKeyClass(Text.class); // the version
		job.setOutputValueClass(SiteVersion.class); // the complete site or a delta

		boolean success = job.waitForCompletion(true);
		Counters counters = job.getCounters();
		Counter c1 = counters.findCounter(MyCounter.NO_OLD_DATA);
		System.out.println(c1.getDisplayName()+":"+c1.getValue());
		return success ? outDir : null;
	}

	public static void usage() {
		System.err.println("Usage: BaselineFetcher <base-dir> [useDeltas] [deltaSize <percentage>]");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
			return -1;
		}
		try {
			boolean useDeltas = false;
			int percentage = 0;
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("useDeltas"))
					useDeltas = true;
				else if(args[i].equals("deltaSize")){
					if(args.length > i+1)
						percentage = Integer.parseInt(args[++i]);
				}
			}
			if(args.length > 1 && args[1].equals("useDeltas"))
				useDeltas = true;
			fetch(new Path(args[0]), useDeltas, percentage);
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleFetcher: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new BaselineFetcher(), args);
		System.exit(res);
	}

}
