package de.uniko.softlang.benchmarks.teraSort.merge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.sort.SortOutputFormat;
import de.uniko.softlang.utils.PairWritable;

public class MergeByJoin extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(MergeByJoin.class);
	public static final String ORIG_FILES_SET = "orig.files.names";
	
	public static class Merger extends Mapper<LongWritable, TupleWritable, LongWritable,Text>{
 
		public void map(LongWritable key, TupleWritable values, Context context) throws IOException, InterruptedException {
		
			int orig = 0;
			int delta = 0;
			PairWritable<Text, BooleanWritable> lastVal = null;
			if(values.size()>1){
				int i = 0;
			}
			for(Writable val : values){
				PairWritable<Text,BooleanWritable> value = (PairWritable)val;
				if(lastVal == null){
					lastVal = value;
					continue;
				}else{
					if(lastVal.getFirst().equals(value.getFirst())){
						if(lastVal.getSecond().get() == value.getSecond().get()){
							if(value.getSecond().get()){
								context.write(key, value.getFirst());
								continue;
							}else{
								try{throw new Exception("two negative occurences");}catch (Exception e) {e.printStackTrace();}
							}
						}else{
							//they invert each other
							lastVal = null;
							continue;
						}
					}else{
						context.write(key, lastVal.getFirst());
						lastVal = value;
						continue;
					}
				}
			}	
			if(lastVal != null){
				if(lastVal.getSecond().get())
					context.write(key, lastVal.getFirst());
				else
					try{throw new Exception("Invalid negative value");}catch (Exception e) {e.printStackTrace();}
				
			}
		}
	}
	
  public int run(String[] args) throws Exception {
  	LOG.info("starting");
    Job job = Job.getInstance(new Cluster(getConf()), getConf());
    Path inputDirOld = new Path(args[0]);
    Path inputDirNew = new Path(args[1]);
    Path outputDir = new Path(args[2]);
    	
    List<Path> plist = new ArrayList<Path>(2);
    plist.add(inputDirOld);
    plist.add(inputDirNew);
    job.setInputFormatClass(CompositeInputFormat.class);
    job.getConfiguration().set(CompositeInputFormat.JOIN_EXPR, 
      CompositeInputFormat.compose("outer", MergeByJoinInputFormat.class,
      plist.toArray(new Path[0])));
    SortOutputFormat.setOutputPath(job, outputDir);
    
    
    job.setJobName("MergeByJoin");
    job.setNumReduceTasks(0);
    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
    }
    
    String origFiles = extractOrigDirs(job, inputDirOld);
    job.getConfiguration().set(ORIG_FILES_SET, origFiles);
        
    job.setJarByClass(MergeByJoin.class);
    job.setMapperClass(Merger.class);
	  job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);
    job.setOutputFormatClass(SortOutputFormat.class);
    
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
    int res = ToolRunner.run(new Configuration(), new MergeByJoin(), args);
    System.exit(res);
  }
  
}
