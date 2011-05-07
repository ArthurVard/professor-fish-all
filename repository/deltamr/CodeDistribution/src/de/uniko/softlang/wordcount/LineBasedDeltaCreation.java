package de.uniko.softlang.wordcount;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import de.uniko.softlang.utils.PairWritable;

public class LineBasedDeltaCreation {
	
	public static final String POS_STRING = "+";
	public static final String NEG_STRING = "-";
	
  public static class InverterMapper 
       extends Mapper<LongWritable, PairWritable<Text, BooleanWritable>, Text, BooleanWritable>{
    
  	public void map(LongWritable key, PairWritable<Text, BooleanWritable> value, Context context
                    ) throws IOException, InterruptedException {
    	context.write(value.getFirst(), value.getSecond());	
    }
  }
  
  public static class DeltaCreatorReducer 
       extends Reducer<Text,BooleanWritable,Text,Text> {
    
  	public void reduce(Text key, Iterable<BooleanWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
    	int orig = 0;
    	int delta = 0;
    	for(BooleanWritable val : values){
    	  if(val.get()){
    	  	orig++;
    	  }else{
    	  	delta++;
    	  }
    	}
    	int total = orig - delta;
    	String sign;
    	if(total < 0){
    		sign = POS_STRING;	//more occurences in the new file -> add them
    		total = 0 - total;
    	}else if (total > 0){
    		sign = NEG_STRING;	//more occurences in the old file -> delete them
    	}else{
    		//same amount in both
    		return;
    	}	
    	for(int i = 0; i < total; i++){
    		 	context.write(new Text(sign), key);
    	}
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = new Job(conf, "word count");
    job.setJarByClass(LineBasedDeltaCreation.class);
    
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 3) {
      System.err.println("Usage: <original> <new> <out>");
      System.exit(2);
    }
    
    //in
    //job.setInputFormatClass(MultipleInputs.class);
    MultipleInputs.addInputPath(job, new Path(args[0]), LineBasedOriginalInput.class);
    MultipleInputs.addInputPath(job, new Path(args[1]), LineBasedNewInput.class);
    
    //mapreduce
    job.setMapperClass(InverterMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(BooleanWritable.class);
    job.setReducerClass(DeltaCreatorReducer.class);
    
    //out
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
