package de.uniko.softlang.crawler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.datastores.CrawlDbData;
import de.uniko.softlang.crawler.datastores.CrawlDbData.SiteStatus;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;

/**
 * Generate a fetchlist of URLs that are supposed to be downloaded. In order to
 * make use of data locality in the context of deltas, a <code>BRUTE</code> flag
 * is activated, that leads to inclusion of all URLs present in the
 * <code>CrawlDb</code>. If this flag is disabled, inclusion is determined based
 * on the timestamp of the last fetch.
 */
public class Generator extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Generator.class);
	private static final boolean BRUTE = true;
	public static final String GENERATOR_CUR_TIME = "generate.curTime";
	public static final String GENERATOR_TOP_N = "generate.topN";
	public static final String INCLUDE_NEW_LINKS = "generate.include.links";
	public static final String GENERATOR_DIR = "generate";
	public static final String SEGMENTS_DIR = "segments";

	public static class GenerateMapper extends
			Mapper<Text, CrawlDbData, Text, CrawlDbData> {
		private long curTime;
		private Text outKey;
		private boolean fetchNewSites;

		protected void setup(Context context) throws IOException,
				InterruptedException {
			outKey = new Text();
			curTime = context.getConfiguration().getLong(GENERATOR_CUR_TIME,
					System.currentTimeMillis());
			fetchNewSites = context.getConfiguration().getBoolean(INCLUDE_NEW_LINKS,
					true);

		}

		protected void map(Text key, CrawlDbData value, Context context)
				throws IOException, InterruptedException {
			if (value.getStatus() == CrawlDbData.SiteStatus.BLOCKED)
				return;
			if (dueToFetch(value)) {
				if (!fetchNewSites) {
					if (value.getVersion() == -1)
						return;
				}
				value.setStatus(SiteStatus.UNFETCHED);
				context.write(key, value); // partition by domain-name
			}
		}

		private boolean dueToFetch(CrawlDbData data) {
			if (BRUTE)
				return true;

			if (data.getStatus() == SiteStatus.INJECTED
					|| data.getStatus() == SiteStatus.UNFETCHED) {
				return true;
			}
			if (curTime - data.getLastFetched() >= data.getFetchInterval()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static class GenerateReducer extends
			Reducer<Text, CrawlDbData, Text, CrawlDbData> {

		private CrawlDbData empty;
		private boolean limitUrls;
		private long limit;
		private long count;

		protected void setup(Context context) throws IOException,
				InterruptedException {
			empty = new CrawlDbData();
			empty.setVersion(-1);
			limit = context.getConfiguration().getLong(GENERATOR_TOP_N,
					Long.MIN_VALUE)
					/ context.getNumReduceTasks();
			count = 0;
			if (limit > 0)
				limitUrls = true;
			else
				limitUrls = false;
		}

		protected void reduce(Text key, Iterable<CrawlDbData> values,
				Context context) throws IOException, InterruptedException {
			for (CrawlDbData value : values) {
				if (limitUrls && count >= limit)
					return;
				empty.setUrl(key);
				empty.setLastModified(value.getLastModified());
				context.write(key, empty);
				count++;
			}
		}
	}

	public Generator() {
	}

	public Generator(Configuration conf) {
		setConf(conf);
	}

	public boolean generate(Path crawlDb, Path baseDir, long topN,
			boolean inclNewLinks) throws Exception {
		if (getConf() == null)
			setConf(new Configuration());
		getConf().setLong(GENERATOR_CUR_TIME, System.currentTimeMillis());
		getConf().setBoolean(INCLUDE_NEW_LINKS, inclNewLinks);
		getConf().setLong(GENERATOR_TOP_N, topN);
		setConf(getConf());
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("Generator");
		job.setJarByClass(Generator.class);
		job.setMapperClass(GenerateMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(CrawlDbData.class);

		job.setReducerClass(GenerateReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		SequenceFileInputFormat.addInputPath(job, crawlDb);
		job.setInputFormatClass(SequenceFileInputFormat.class);

		// out
		Path output = new Path(baseDir, GENERATOR_DIR);
		FileSystem fs = output.getFileSystem(getConf());
		if (fs.exists(output)) {
			fs.delete(output, true);
		}
		Path dataDir = new Path(baseDir, DeltaFetcher.DATA_DIR);
		if (!fs.exists(dataDir)) {
			fs.mkdirs(dataDir);
		}
		SequenceFileOutputFormat.setOutputPath(job, output);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(CrawlDbData.class);

		boolean success = job.waitForCompletion(true);
		Counter c1 = job.getCounters().findCounter(
				"org.apache.hadoop.mapreduce.TaskCounter", "MAP_OUTPUT_RECORDS");
		LOG.info("generated entries for " + c1.getValue() + " urls");

		return success;
	}

	public static void usage() {
		System.err
				.println("Usage: Generator <crawldb> <base_dir> [-topN N] [noAdditions]");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			boolean inclNewLinks = true;
			long topN = Long.MIN_VALUE;
			for (int i = 2; i < args.length; i++) {
				if ("-topN".equals(args[i])) {
					topN = Long.parseLong(args[++i]);
				} else if ("noAdditions".equals(args[i])) {
					inclNewLinks = false;
				}
			}
			generate(new Path(args[0]), new Path(args[1]), topN, inclNewLinks);
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleGenerator: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Generator(), args);
		System.exit(res);
	}

}
