package de.uniko.softlang.benchmarks.pipelined;

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
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.sort.SortDeltaInputFormat;
import de.uniko.softlang.benchmarks.teraSort.sort.SortDeltaOutputFormat;
import de.uniko.softlang.utils.PairWritable;

public class PipelinedDeltaJob extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(PipelinedDeltaJob.class);
	private static final String MAP_PERCENTAGE = "uni-ko.map.keep";
	private static final String REDUCE_PERCENTAGE = "uni-ko.reduce.keep";
	
	private static boolean keep(float percentage){
  	double rand = Math.random();
  	if(rand < percentage){
  		return true;
  	}else{
  		return false;
  	}
  }
	
	/**
	 *	Mapper that keeps tuples to a certain percentage. 
	 *  The percentage has to be supplied in the configuration using MAP_PERCENTAGE
	 */
	public static class PipelinedDeltaMapper extends Mapper<LongWritable, PairWritable<Text,Text>, LongWritable, PairWritable<Text,Text>> {

		private float mapKeep;
		
		public void setup(Context context){
    	try{
    		mapKeep = Float.parseFloat(context.getConfiguration().get(MAP_PERCENTAGE))/100;
    		System.out.println("MapPercentage = " + mapKeep);
    	}catch(NumberFormatException e){
    		LOG.error("Invalid change-percentage for map: " + context.getConfiguration().get(MAP_PERCENTAGE) + " => set to 100%");
    		mapKeep = 1.0F;
    	}
    }
		
		public void map(LongWritable key, PairWritable<Text,Text> value, Context context) throws IOException, InterruptedException {
			if(keep(mapKeep)){
				context.write(key, value);
			}
		}
	}
	
	/**
	 *	Reducer that keeps tuples to a certain percentage. 
	 *  The percentage has to be supplied in the configuration using REDUCE_PERCENTAGE
	 */
	 public static class PipelinedDeltaReducer extends Reducer<LongWritable, PairWritable<Text,Text>, LongWritable, PairWritable<Text,Text>> {

		private float reduceKeep;
		
		public void setup(Context context){
    	try{
    		reduceKeep = Float.parseFloat(context.getConfiguration().get(REDUCE_PERCENTAGE))/100;
    		System.out.println("ReducePercentage = " + reduceKeep);
    	}catch(NumberFormatException e){
    		LOG.error("Invalid change-percentage for reduce: " + context.getConfiguration().get(REDUCE_PERCENTAGE) + " => set to 100%");
    		reduceKeep = 1.0F;
    	}
    }
		
		public void reduce(LongWritable key, Iterable<PairWritable<Text,Text>> values, Context context) throws IOException, InterruptedException {
			
			for(PairWritable<Text,Text> val : values){
				if(keep(reduceKeep)){
					context.write(key, val);
				}
			}	
		}
	}
	
	
		
  public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
  	LOG.info("starting");
  	Path baseInputDir = new Path(args[0]);
  	Path baseOutputDir = new Path(args[1]);
  	int numJobs = Integer.parseInt(args[2]);
  	//String[] keepPercentages = {"100", "100", "100", "100", "100", "10"};
  	String[] keepPercentages = {"100", "100", "100", "100", "100", "100"};
  	
  	Path inputDir = baseInputDir;
  	Path outputDir = new Path(baseOutputDir.toString() + "/0");
  	
  	for (int i = 0; i < numJobs; i++) {
			
  		Job job = Job.getInstance(new Cluster(getConf()), getConf());
  		job.getConfiguration().set(MAP_PERCENTAGE, keepPercentages[i*2 % keepPercentages.length]);
  		job.getConfiguration().set(REDUCE_PERCENTAGE, keepPercentages[(i*2+1) % keepPercentages.length]);
  		LOG.info("MapPercentage = " + keepPercentages[i*2 % keepPercentages.length] + " ReducePercentage = " + keepPercentages[(i*2+1) % keepPercentages.length]);
  		
  		//in
  		SortDeltaInputFormat.addInputPath(job, inputDir);
  		job.setInputFormatClass(SortDeltaInputFormat.class);
    
  		//misc
  		job.setJobName("Pipelined Delta" + i);
  		job.setJarByClass(PipelinedDeltaJob.class);
  		job.setMapperClass(PipelinedDeltaMapper.class);
  		job.setReducerClass(PipelinedDeltaReducer.class);
  		job.setMapOutputKeyClass(LongWritable.class);
  		job.setMapOutputValueClass(PairWritable.class);
    
  		//out
  		SortDeltaOutputFormat.setOutputPath(job, outputDir);
  		job.setOutputFormatClass(SortDeltaOutputFormat.class);
  		job.setOutputKeyClass(LongWritable.class);
  		job.setOutputValueClass(PairWritable.class);

  		if(!job.waitForCompletion(true)){
  			LOG.error("Job #" + i + " failed.....aborting....");
  			return 1;
  		}
    	inputDir = outputDir;
    	outputDir = new Path(baseOutputDir.toString() + "/" + (i+1));
    	
    	//delete previous outputDir to keep storage small
    	if(i>0){
    		Path oldOutDir = new Path(baseOutputDir.toString() + "/" + (i-1));
    		FileSystem fs = oldOutDir.getFileSystem(job.getConfiguration());
    		fs.delete(oldOutDir, true);
    	}
  	}
  	
  	return 0;
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new PipelinedDeltaJob(), args);
    System.exit(res);
  }
  
}
