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
import org.apache.hadoop.mapreduce.Reducer;
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
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.SiteVersion;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.datastores.CrawlDbData.SiteStatus;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;

public class PrintSegment extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(PrintSegment.class);

	public static class PrintMapper extends
			Mapper<Text, VersionedSiteData, Text, Text> {

		protected void map(Text key, VersionedSiteData value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value.prettyPrint());
		}
	}
	
	public static class PrintReducer	extends Reducer<Text, VersionedSiteData, Text, Text> {
		protected void reduce(Text key, Iterable<VersionedSiteData> values,Context context) throws IOException, InterruptedException {
			String prettyString = "";
			for (VersionedSiteData val : values) {
				prettyString += val.prettyPrint().toString();
			}
			context.write(key, new Text(prettyString));
		}
	}

	public PrintSegment(){ }
	
	public PrintSegment(Configuration conf){
		setConf(conf);
	}
	
	
	public boolean print(Path segmentPath, Path out) throws IOException,
			InterruptedException, ClassNotFoundException {
		if(getConf() == null)
			setConf(new Configuration());
		
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("PrintSegment");
		job.setJarByClass(PrintSegment.class);
		job.setMapperClass(PrintMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		//job.setMapOutputValueClass(VersionedSiteData.class);
		job.setNumReduceTasks(0);
		//job.setReducerClass(PrintReducer.class);

		// in
		SequenceFileInputFormat.addInputPath(job, new Path(segmentPath,
				DeltaFetcher.META_DIR));
		SequenceFileInputFormat.addInputPath(job, new Path(segmentPath,
				DeltaFetcher.CONTENT_DIR));
		job.setInputFormatClass(SequenceFileInputFormat.class);

		// out
		FileSystem fs = out.getFileSystem(getConf());
		if (fs.exists(out)) {
			fs.delete(out, true);
		}
		TextOutputFormat.setOutputPath(job, out);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true);
	}

	public static void usage(){
		System.err.println("Usage: PrintSegment <segment_path> <out>");
	}
	
	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			print(new Path(args[0]), new Path(args[1]));
			return 0;
		} catch (Exception e) {
			LOG.fatal("PrintSegment: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PrintSegment(), args);
		System.exit(res);
	}

}
