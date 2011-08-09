package de.uniko.softlang.crawler.print;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class FilterSeedList extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(FilterSeedList.class);
	private static final String INCL_UNFETCHED = "uniko.include.unfetched.sites";
	private static final String SEED_PERCENTAGE = "uniko.percentage.to.include";

	public static class FilterMapper extends Mapper<LongWritable, Text, Text, Text> {

		float percentage;
		
		protected void setup(Context context) throws IOException,
				InterruptedException {
			this.percentage = context.getConfiguration().getFloat(SEED_PERCENTAGE, 1f);
		}
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			double random = Math.random();
			if(random < percentage)
				context.write(value, new Text(""));
		}
	}

	public FilterSeedList(){ }
	
	public FilterSeedList(Configuration conf){
		setConf(conf);
	}
	
	public boolean filterSeeds(Path in, Path out, float size) throws IOException,
			InterruptedException, ClassNotFoundException {
		if(getConf() == null)
			setConf(new Configuration());
		getConf().setFloat(SEED_PERCENTAGE, size);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("FilterSeedList");
		job.setJarByClass(FilterSeedList.class);
		job.setMapperClass(FilterMapper.class);
		job.setNumReduceTasks(0);

		// in
		TextInputFormat.addInputPath(job, in);
		job.setInputFormatClass(TextInputFormat.class);

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
		System.err.println("Usage: FilterSeedList <seedList_in> <seedList_out> <percentage>");
	}
	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			usage();
			return -1;
		}
		try {
			filterSeeds(new Path(args[0]), new Path(args[1]), Float.parseFloat(args[2])/100f);
			return 0;
		} catch (Exception e) {
			LOG.fatal("FilterSeedList: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new FilterSeedList(), args);
		System.exit(res);
	}

}
