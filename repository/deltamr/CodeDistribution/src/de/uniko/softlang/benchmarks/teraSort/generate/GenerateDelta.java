package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.utils.PairWritable;


public class GenerateDelta extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(GenerateDelta.class);
	public static final String ORIG_FILES_SET = "orig.files.names";
	public static final String POS_STR = "+";
  public static final String NEG_STR = "-";
  
	public static class SignMapper extends Mapper<Text, PairWritable<LongWritable,Text>, PairWritable<LongWritable,Text>,BooleanWritable>{
 
		private Collection<String> origFiles;
		BooleanWritable isOld;
		
		protected void setup(Context context) throws IOException, InterruptedException {
			origFiles = context.getConfiguration().getStringCollection(ORIG_FILES_SET);
			isOld = new BooleanWritable();
		}
		
		public void map(Text key, PairWritable<LongWritable,Text> value,
				Context context) throws IOException, InterruptedException {
		
			if(origFiles.contains(key.toString())){
				isOld.set(true);
			}else{
				isOld.set(false);
			}
			context.write(value,isOld);
		}
  }
	
	
	public static class DeltaCreatorReducer extends Reducer<PairWritable<LongWritable,Text>,BooleanWritable,LongWritable,PairWritable<Text,Text>> {

		protected void reduce(PairWritable<LongWritable,Text> key, Iterable<BooleanWritable> values, 
				Context context) throws IOException, InterruptedException {
			
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
				sign = POS_STR;	//more occurences in the new file -> add them
				total = 0 - total;
			}else if (total > 0){
				sign = NEG_STR;	//more occurences in the old file -> delete them
			}else{
				//same amount in both
				return;
			}
		for(int i = 0; i < total; i++){
				context.write(key.getFirst(), new PairWritable<Text,Text>(key.getSecond(),new Text(sign)));	
			}
		}
	}
	
	

	
  public int run(String[] args) throws Exception {
  	LOG.info("starting");
    Job job = Job.getInstance(new Cluster(getConf()), getConf());
    Path inputDirOld = new Path(args[0]);
    Path inputDirNew = new Path(args[1]);
    Path outputDir = new Path(args[2]);
    
    CreateDeltaInputFormat.addInputPath(job, inputDirOld);
    CreateDeltaInputFormat.addInputPath(job, inputDirNew);
    DeltaOutputFormat.setOutputPath(job, outputDir);
    job.setJobName("GenerateDelta");
    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
    }
    
    String origFiles = extractOrigDirs(job, inputDirOld);
    job.getConfiguration().set(ORIG_FILES_SET, origFiles);
        
    job.setJarByClass(GenerateDelta.class);
    job.setInputFormatClass(CreateDeltaInputFormat.class);
    job.setMapperClass(SignMapper.class);
    job.setMapOutputKeyClass(PairWritable.class);
    job.setMapOutputValueClass(BooleanWritable.class);
    job.setReducerClass(DeltaCreatorReducer.class);
	  job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(PairWritable.class);
    job.setOutputFormatClass(DeltaOutputFormat.class);
    
    int ret = job.waitForCompletion(true) ? 0 : 1;
    LOG.info("done");
    return ret;
  }

	public String extractOrigDirs(Job job, Path inputDirOld) throws IOException,
			FileNotFoundException {
		FileStatus f = inputDirOld.getFileSystem(job.getConfiguration()).getFileStatus(inputDirOld);
    String origFiles = "";
    if(f.isDir()){
    	FileStatus[] fs = inputDirOld.getFileSystem(job.getConfiguration()).listStatus(inputDirOld);
    	for (int i = 0; i < fs.length; i++) {
    		String path = fs[i].getPath().toString();
    		origFiles = origFiles.concat(path + ",");
    	}
    }else{
    	origFiles.concat(inputDirOld.toString());
    }
		return origFiles;
	}

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new GenerateDelta(), args);
    System.exit(res);
  }
  
}
