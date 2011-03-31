package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.sort.SortDeltaOutputFormat;
import de.uniko.softlang.utils.PairWritable;


public class DeltaByJoin extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(DeltaByJoin.class);
	
	public static class SignMapper extends Mapper<PairWritable<LongWritable, Text>, TupleWritable, LongWritable,PairWritable<Text,Text>>{
 
		private Collection<String> origFiles;
		Text sign;
		
		protected void setup(Context context) throws IOException, InterruptedException {
			origFiles = context.getConfiguration().getStringCollection(GenerateDelta.ORIG_FILES_SET);
			sign = new Text();
		}
		
		public void map(PairWritable<LongWritable, Text> key, TupleWritable values, Context context) throws IOException, InterruptedException {
		
			if(sign == null){
				sign = new Text();
			}
			
			int orig = 0;
			int delta = 0;
			for(Writable val : values){
				Text value = (Text)val;
				if(origFiles.contains(value.toString())){
					orig++;
				}else{
					delta++;
				}
			}	
			int total = orig - delta;
			if(total < 0){
				sign.set(GenerateDelta.POS_STR);	//more occurences in the new file -> add them
				total = 0 - total;
			}else if (total > 0){
				sign.set(GenerateDelta.NEG_STR);	//more occurences in the old file -> delete them
			}else{
				//same amount in both
				return;
			}
			for(int i = 0; i < total; i++){
				context.write(key.getFirst(),new PairWritable<Text, Text>(key.getSecond(),sign));	
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
      CompositeInputFormat.compose("outer", DeltaByJoinInputFormat.class,
      plist.toArray(new Path[0])));
    SortDeltaOutputFormat.setOutputPath(job, outputDir);
    
    
    job.setJobName("MyGenDeltaJoin");
    job.getConfiguration().setInt("dfs.replication", 1);
    job.getConfiguration().setInt(MRJobConfig.NUM_MAPS, 39);
    job.setNumReduceTasks(0);
    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
    }
    
    String origFiles = extractOrigDirs(job, inputDirOld);
    job.getConfiguration().set(GenerateDelta.ORIG_FILES_SET, origFiles);
        
    job.setJarByClass(DeltaByJoin.class);
    job.setMapperClass(SignMapper.class);
	  job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(PairWritable.class);
    job.setOutputFormatClass(SortDeltaOutputFormat.class);
    
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
    int res = ToolRunner.run(new Configuration(), new DeltaByJoin(), args);
    System.exit(res);
  }
  
}
