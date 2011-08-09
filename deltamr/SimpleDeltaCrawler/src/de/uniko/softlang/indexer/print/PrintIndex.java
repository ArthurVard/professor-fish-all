package de.uniko.softlang.indexer.print;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.utils.ListWritable;

public class PrintIndex extends Configured implements Tool{
	
	public static class PrintIndexMapper extends Mapper<Text, ListWritable, Text, Text>{
		private Text word = new Text();
		private boolean useDocMap = false;
		
		protected void map(Text key, ListWritable value, Context context) throws IOException, InterruptedException {
			context.write(key, value.prettyPrint());
		}
	}
	
	public PrintIndex(){ }
	
	public PrintIndex(Configuration conf){
		setConf(conf);
	}
	
	public boolean print(Path in, Path out) throws Exception {
		if(getConf() == null)
			setConf(new Configuration());
		
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("PrintIndex");
		job.setInputFormatClass(SequenceFileInputFormat.class);
	  SequenceFileInputFormat.addInputPath(job, in);
		
		job.setJarByClass(PrintIndex.class);
	  job.setMapperClass(PrintIndexMapper.class);
	  job.setNumReduceTasks(0);
	  job.setOutputKeyClass(Text.class);
	  job.setOutputValueClass(Text.class);

	  TextOutputFormat.setOutputPath(job, out);
	  job.setOutputFormatClass(TextOutputFormat.class);
	  return job.waitForCompletion(true);
	}

	public int run(String[] args) throws Exception {
	    if (args.length < 2) {
	      System.err.println("Usage: PrintIndex <segment_dir> <out_dir>");
	      return -1;
	    }	
			print(new Path(args[0]), new Path(args[1]));
			return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PrintIndex(), args);
	    System.exit(res);
	}

}
