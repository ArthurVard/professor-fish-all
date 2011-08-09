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
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.datastores.CrawlDbData;
import de.uniko.softlang.crawler.datastores.LinkMap;

public class PrintLinkDb extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(PrintLinkDb.class);

	public static class PrintMapper extends Mapper<Text, LinkMap, Text, Text> {

		protected void map(Text key, LinkMap value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value.prettyPrint());
		}
	}

	public PrintLinkDb() {
	}

	public PrintLinkDb(Configuration conf) {
		setConf(conf);
	}

	public boolean print(Path linkDb, Path out) throws IOException,
			InterruptedException, ClassNotFoundException {
		if (getConf() == null)
			setConf(new Configuration());
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("PrintLinkDb");
		job.setJarByClass(PrintLinkDb.class);
		job.setMapperClass(PrintMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(0);

		// in
		SequenceFileInputFormat.addInputPath(job, linkDb);
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

	public static void usage() {
		System.err.println("Usage: PrintLinkDb <linkdb> <out>");
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
			LOG.fatal("PrintLinkDb: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PrintLinkDb(), args);
		System.exit(res);
	}

}
