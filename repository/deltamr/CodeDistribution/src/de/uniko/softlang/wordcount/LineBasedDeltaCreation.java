package de.uniko.softlang.wordcount;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class LineBasedDeltaCreation {
	
	public static final String POS_STRING = "+";
	public static final String NEG_STRING = "-";
	
  public static class InverterMapper 
       extends Mapper<Text, Text, Text, BooleanWritable>{
    
  	private Collection<String> origFiles;
  	BooleanWritable isOld;
		
  	protected void setup(Context context) throws IOException, InterruptedException {
  		origFiles = context.getConfiguration().getStringCollection(FileNameLineInputFormat.ORIG_FILES_SET);
  		isOld = new BooleanWritable();
  	}

    public void map(Text key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	if(origFiles.contains(key.toString())){
    		isOld.set(true);
    	}else{
    		isOld.set(false);
    	}
    	context.write(value, isOld);	
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
    		sign = NEG_STRING;	//more occurences in the new file -> add them
    		total = 0 - total;
    	}else if (total > 0){
    		sign = POS_STRING;	//more occurences in the old file -> delete them
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
    
    //remember names of original file
    Path origDir = new Path(args[0]);
    FileStatus f = origDir.getFileSystem(conf).getFileStatus(origDir);
    String origFiles = "";
    if(f.isDir()){
    	FileStatus[] fs = origDir.getFileSystem(conf).listStatus(origDir);
    	for (int i = 0; i < fs.length; i++) {
    		String path = fs[i].getPath().toString();
    		origFiles = origFiles.concat(path + ",");
    	}
    }else{
    	origFiles.concat(origDir.toString());
    }
    job.getConfiguration().set(FileNameLineInputFormat.ORIG_FILES_SET, origFiles);
    
    //in
    job.setInputFormatClass(FileNameLineInputFormat.class);
    FileNameLineInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileNameLineInputFormat.addInputPath(job, new Path(otherArgs[1]));
    
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
