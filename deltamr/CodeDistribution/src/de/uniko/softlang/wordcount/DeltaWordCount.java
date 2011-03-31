package de.uniko.softlang.wordcount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import de.uniko.softlang.utils.PairWritable;


public class DeltaWordCount {

  public static class DeltaTokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{
    
    private final static IntWritable newValue = new IntWritable();
    
    private Text word = new Text();
      
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    		int sign = value.toString().startsWith(LineBasedDeltaCreation.NEG_STRING) ? -1: 1;
    		newValue.set(sign);
    		StringTokenizer itr = new StringTokenizer(value.toString().substring(2));
    		while (itr.hasMoreTokens()) {
    			word.set(itr.nextToken());
    			context.write(word, newValue);
    		}
    }
  }
  
  public static class DeltaIntSumReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        	sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: wordcount <delta> <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "delta word count");
    job.setJarByClass(DeltaWordCount.class);
    job.setMapperClass(DeltaTokenizerMapper.class);
    job.setCombinerClass(DeltaIntSumReducer.class);
    job.setReducerClass(DeltaIntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    SequenceFileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    job.setOutputFormatClass(SequenceFileOutputFormat.class);
    
    job.waitForCompletion(true);
    
  }
}
