package de.uniko.softlang.crawler.print;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Generator;
import de.uniko.softlang.crawler.Generator.GenerateMapper;
import de.uniko.softlang.crawler.Generator.GenerateReducer;
import de.uniko.softlang.crawler.datastores.CrawlDbData;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.datastores.CrawlDbData.SiteStatus;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;

public class ExtractSeedList extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(ExtractSeedList.class);
	private static final String INCL_UNFETCHED = "uniko.include.unfetched.sites";

	public static class ExtractMapper extends Mapper<Text, CrawlDbData, Text, Text> {

		boolean includeUnfetched;
		
		protected void setup(Context context) throws IOException,
				InterruptedException {
			this.includeUnfetched = context.getConfiguration().getBoolean(INCL_UNFETCHED, false);
		}
		protected void map(Text key, CrawlDbData value, Context context)
				throws IOException, InterruptedException {
			if(value.getStatus() == CrawlDbData.SiteStatus.BLOCKED)
				return;
			if(!includeUnfetched && value.getVersion() == -1)
				return;
			context.write(key, new Text(""));
		}
	}

	public ExtractSeedList(){ }
	
	public ExtractSeedList(Configuration conf){
		setConf(conf);
	}
	
	public boolean extractSeeds(Path crawlDb, Path out, boolean includeUnfectched) throws IOException,
			InterruptedException, ClassNotFoundException {
		if(getConf() == null)
			setConf(new Configuration());
		getConf().setBoolean(INCL_UNFETCHED, includeUnfectched);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("ExtractSeedList");
		job.setJarByClass(ExtractSeedList.class);
		job.setMapperClass(ExtractMapper.class);
		job.setNumReduceTasks(0);

		// in
		SequenceFileInputFormat.addInputPath(job, crawlDb);
		job.setInputFormatClass(SequenceFileInputFormat.class);

		// out
		Path output = out;
		FileSystem fs = output.getFileSystem(getConf());
		if (fs.exists(output)) {
			fs.delete(output, true);
		}
		TextOutputFormat.setOutputPath(job, output);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true);
	}

	public static void usage(){
		System.err.println("Usage: ExtractSeedList <crawldb> <seedList_out> [includeUnfetched]");
	}
	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			boolean inclUnfetched = false;
			if(args.length > 2 && args[2].equals("includeUnfetched"))
				inclUnfetched = true;
			extractSeeds(new Path(args[0]), new Path(args[1]), inclUnfetched);
			return 0;
		} catch (Exception e) {
			LOG.fatal("ExtractSeedList: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new ExtractSeedList(), args);
		System.exit(res);
	}

}
