package de.uniko.softlang.benchmarks.teraSort.sort;

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



public class Sort extends Configured implements Tool {
	private static final Log LOG = LogFactory.getLog(Sort.class);
	
	
	private static class LongPartitioner extends Partitioner<LongWritable, Text> implements Configurable {
		private Configuration conf;
		double keysPerReducer;
		double reducers;
		
		/**
		 * Calculates the partition for a passed <code>value</code>. Positive and negative <code>values</code> are treated alike at first.
		 * After calculating a relative partition first, the result-partition is shifted by <code>numPartitions/2</code> if the value is positive. This
		 * assigns partitions <code>[0,numPartitions/2]</code> to negative values, and <code>[numPartitions/2,numPartitions]</code> to positive values.
		 * 
		 * @param numPartitions the total number of partitions
		 * @param value the <code>long</code>-value to assgin to a partition
		 * @return the partition <code>value</code> has been assigned to
		 */
		public static int getPartitionNumber(int numPartitions, long value) {
	    assert numPartitions >= 1;
	    if (numPartitions == 1) {
	        return 0;
	    }
	    if (value <= Long.MIN_VALUE + 2) {
	        return 0;
	    }
	    if (value >= Long.MAX_VALUE - 2) {
	        return numPartitions - 1;
	    }

	    int halfPartitions = numPartitions / 2;
	    boolean odd = numPartitions % 2 == 1;
	    long partitionSize = Long.MAX_VALUE / halfPartitions;
	    if (odd) {
	    		//"middle-partition" contains longs from [-n,0] and [0,n] 
	        partitionSize -= partitionSize / numPartitions;
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
	    //if value is positive -> shift
	    int out =  value >= 0 ? firstPositivePartition + relativePartition
	            : lastNegativePartition - relativePartition;
	    
	    //stay in bounds 
	    if(out < 0){
	    	out = 0;
	    	LOG.warn("Partition out of bounds for key " + value);
	    }else if(out > numPartitions - 1){
	    	out = numPartitions -1; 
	    	LOG.warn("Partition out of bounds for key " + value);
	    }
	    return out;
		}
		
		public int getPartition(LongWritable key, Text value, int numReducers) {
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
	
	 public int run(String[] args) throws Exception {
	    LOG.info("starting");
	    Job job = Job.getInstance(new Cluster(getConf()), getConf());
	    Path inputDir = new Path(args[0]);
	    Path outputDir = new Path(args[1]);
	    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
	    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
	    }
	    SortInputFormat.setInputPaths(job, inputDir);
	    SortOutputFormat.setOutputPath(job, outputDir);
	    job.setJobName("Sort");
	    
	    job.setJarByClass(Sort.class);
	    job.setPartitionerClass(LongPartitioner.class);
	    job.setOutputKeyClass(LongWritable.class);
	    job.setOutputValueClass(Text.class);
	    job.setInputFormatClass(SortInputFormat.class);
	    job.setOutputFormatClass(SortOutputFormat.class);
	    
	    
	    int ret = job.waitForCompletion(true) ? 0 : 1;
	    LOG.info("done");
	    return ret;
	  }

	  /**
	   * @param args
	   */
	  public static void main(String[] args) throws Exception {
	    int res = ToolRunner.run(new Configuration(), new Sort(), args);
	    System.exit(res);
	  }

}
