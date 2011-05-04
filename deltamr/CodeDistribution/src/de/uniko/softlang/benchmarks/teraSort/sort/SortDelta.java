package de.uniko.softlang.benchmarks.teraSort.sort;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.utils.PairWritable;



public class SortDelta extends Configured implements Tool {
	private static final Log LOG = LogFactory.getLog(SortDelta.class);
	
	
	private static class LongPartitioner extends Partitioner<LongWritable, PairWritable<Text,Text>> implements Configurable {
		private Configuration conf;
		double keysPerReducer;
		double reducers;
		
		public static int getPartitionNumber(int partitions, long value) {
	    assert partitions >= 1;
	    if (partitions == 1) {
	        return 0;
	    }
	    if (value <= Long.MIN_VALUE + 2) {
	        return 0;
	    }
	    if (value >= Long.MAX_VALUE - 2) {
	        return partitions - 1;
	    }

	    int halfPartitions = partitions / 2;
	    boolean odd = partitions % 2 == 1;
	    long partitionSize = Long.MAX_VALUE / halfPartitions;
	    if (odd) {
	        partitionSize -= partitionSize / partitions;
	    }

	    long firstPositive = odd ? (partitionSize / 2) : 0l;
	    
	    int firstPositivePartition = odd ? halfPartitions + 1 : halfPartitions;
	    int lastNegativePartition = halfPartitions - 1;

	    long absValue = Math.abs(value);

	    if (odd) {
	        if (absValue < firstPositive) {
	            return firstPositivePartition - 1;
	        }
	    }

	    int relativePartition = (int) ((absValue - firstPositive) / partitionSize);
	    int out =  value >= 0 ? firstPositivePartition + relativePartition
	            : lastNegativePartition - relativePartition;
	    
	    //stay in bounds 
	    if(out < 0){
	    	out = 0;
	    	LOG.warn("Partition out of bounds for key " + value);
	    }else if(out > partitions - 1){
	    	out = partitions -1; 
	    	LOG.warn("Partition out of bounds for key " + value);
	    }
	    return out;
		}
		
		public int getPartition(LongWritable key, PairWritable<Text,Text> value, int numReducers) {
			if(reducers > 1){
				int reducer = getPartitionNumber(numReducers, key.get());// + 1;
				LOG.info("Reducer " + reducer + " for key " + key.get() + "(keysPerReducer="+keysPerReducer +")");
				return reducer;
			}else{
				return 1;
			}
		}

		@Override
		public Configuration getConf() {
			return conf;
		}

		@Override
		public void setConf(Configuration conf) {
			this.conf = conf;
			reducers = conf.getInt(MRJobConfig.NUM_REDUCES, 1);
			if(reducers>1){
				keysPerReducer =  Math.ceil((Long.MAX_VALUE/reducers)*2);
				LOG.info("Configuring ...." + keysPerReducer + " keysPerReducer");
			}
		}
		
	}
	
	private static void usage() throws IOException {
		System.err.println("sortdelta <in> <out>");
	}

  
	 public int run(String[] args) throws Exception {
	    LOG.info("starting");
	    Job job = Job.getInstance(new Cluster(getConf()), getConf());
	    Path inputDir = new Path(args[0]);
	    Path outputDir = new Path(args[1]);
	    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
	    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
	    }
	    SortDeltaInputFormat.setInputPaths(job, inputDir);
	    SortDeltaOutputFormat.setOutputPath(job, outputDir);
	    job.setJobName("MySortDelta");
	    job.getConfiguration().setInt(MRJobConfig.NUM_MAPS, 39);
	    job.getConfiguration().setInt("dfs.replication", 1);
	    job.setNumReduceTasks(39);
	    
	    job.setJarByClass(SortDelta.class);
	    job.setPartitionerClass(LongPartitioner.class);
	    job.setOutputKeyClass(LongWritable.class);
	    job.setOutputValueClass(PairWritable.class);
	    job.setInputFormatClass(SortDeltaInputFormat.class);
	    job.setOutputFormatClass(SortDeltaOutputFormat.class);
	    
	    
	    int ret = job.waitForCompletion(true) ? 0 : 1;
	    LOG.info("done");
	    return ret;
	  }

	  /**
	   * @param args
	   */
	  public static void main(String[] args) throws Exception {
	    int res = ToolRunner.run(new Configuration(), new SortDelta(), args);
	    System.exit(res);
	  }

}
