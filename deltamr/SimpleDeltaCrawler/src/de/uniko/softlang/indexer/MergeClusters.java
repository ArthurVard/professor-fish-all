package de.uniko.softlang.indexer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Crawl;

/**
 * Tool to merge multiple clusters.
 */
public class MergeClusters extends Configured implements Tool {
	
	public boolean mergeClusters(Path[] in, Path out) throws Exception {
		Job job = Job.getInstance(new org.apache.hadoop.mapreduce.Cluster(getConf()), getConf());
		job.setJobName("MergeClusters");
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		for(int i = 0; i < in.length; i++){
			SequenceFileInputFormat.addInputPath(job, in[i]);
		}

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setJarByClass(MergeClusters.class);
	  job.setMapperClass(Mapper.class);
	  job.setMapOutputKeyClass(Text.class);
	  job.setMapOutputValueClass(org.apache.mahout.clustering.kmeans.Cluster.class);
	  job.setReducerClass(Reducer.class);
	  job.setOutputKeyClass(Text.class);
	  job.setOutputValueClass(org.apache.mahout.clustering.kmeans.Cluster.class);

	  SequenceFileOutputFormat.setOutputPath(job, out);
	  job.setOutputFormatClass(SequenceFileOutputFormat.class);
	  return job.waitForCompletion(true);
	}
	

	
	public static void usage(){
		System.err.println("Usage: MergeClusters <out_dir> <clusterdir1> <clusterdir2> (<clusterdir>*)");
	}
	
	public int run(String[] args) throws Exception {
	    if (args.length < 3) {
	      usage();
	    	return -1;
	    }	
	    Path[] in = new Path[args.length-1];
	    for (int i = 0; i < args.length-1; i++) {
				in[i] = new Path(args[i+1]);
			}
			mergeClusters(in, new Path(args[0]));
			return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MergeClusters(), args);
	    System.exit(res);
	}

}
