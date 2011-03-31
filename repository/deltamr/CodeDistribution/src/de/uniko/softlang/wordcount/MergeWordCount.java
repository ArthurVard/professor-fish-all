package de.uniko.softlang.wordcount;

import java.io.IOException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import de.uniko.softlang.benchmarks.teraSort.generate.Generate;


public class MergeWordCount {

	 private static final Log LOG = LogFactory.getLog(MergeWordCount.class);
	 
	 public static class MergeIntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
		 	private IntWritable result = new IntWritable();

		 	public void reduce(Text key, Iterable<IntWritable> values, 
		 										Context context
                   			) throws IOException, InterruptedException {
		 		int sum = 0;
		 		for (IntWritable val : values) {
		 			sum += val.get();
		 		}
		 		if(sum > 0){
		 			result.set(sum);
			 		context.write(key, result);
		 		}else if(sum < 0){
		 			LOG.warn("Negative count for word '" + key.toString() + "'!");
		 		}else{
		 			return;
		 		}
		 	}
}

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 3) {
      System.err.println("Usage: wordcount <in> <delta> <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "merge word count");
    
    job.setJarByClass(MergeWordCount.class);
    job.setCombinerClass(MergeIntSumReducer.class);
    job.setReducerClass(MergeIntSumReducer.class);	
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class); 
    job.setInputFormatClass(SequenceFileInputFormat.class);
    SequenceFileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    SequenceFileInputFormat.addInputPath(job, new Path(otherArgs[1]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
        
  }
}
