package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.IOException;
import java.util.Random;
import java.util.zip.Checksum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.terasort.TeraGen.Counters;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.original.Unsigned16;
import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;
import de.uniko.softlang.benchmarks.teraSort.sort.SortOutputFormat;


public class GenerateNew extends Configured implements Tool {
	private static final Log LOG = LogFactory.getLog(GenerateNew.class);
	private static final String DELTA_SIZE = "uni-ko.delta.size";
	
	
	/**
	 * The Mapper class that given a row number, will generate the appropriate
	 * output line.
	 */
	public static class GenerateNewMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
		private float deltaSize;
		
		private Random rand = new Random();
		private byte[] keyBuffer = new byte[SortInputFormat.KEY_LENGTH];
		private long rowId = 0;

		private Unsigned16 checksum = new Unsigned16();
		private Checksum crc32 = new PureJavaCrc32();
		private Unsigned16 total = new Unsigned16();
		private Counter checksumCounter;

		 public void setup(Context context){
	    	try{
	    		deltaSize = Float.parseFloat(context.getConfiguration().get(DELTA_SIZE))/100;
	    		deltaSize /= 2;	//since a modified key or value will resut in 2 delta-entries (add & delete)
	    	}catch(NumberFormatException e){
	    		LOG.error("Invalid change-percentage: " + context.getConfiguration().get(DELTA_SIZE) + " => set to 10%");
	    		deltaSize = 0.1F;
	    	}
	   }
		 
		 private boolean keep(){
	    	double rand = Math.random();
	    	if(rand < deltaSize){
	    		return false;
	    	}else{
	    		return true;
	    	}
	    }
	    
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			if (checksumCounter == null) {
				checksumCounter = context.getCounter(Counters.CHECKSUM);
			}
			if(!keep()){
				KVGenerator.generateRecord(key, value, rand.nextLong(), rowId);
			}
			context.write(key, value);
			
			KVGenerator.longToBytes(keyBuffer, key.get());
			crc32.reset();
			crc32.update(keyBuffer, 0, SortInputFormat.KEY_LENGTH);
			crc32.update(value.getBytes(), 0, SortInputFormat.VALUE_LENGTH);
			checksum.set(crc32.getValue());
			total.add(checksum);
		}

		@Override
		public void cleanup(Context context) {
			checksumCounter.increment(total.getLow8());
			System.out.println("Checksum  = " + total.getLow8());
		}
	}

	private static void usage() throws IOException {
		System.err.println("myGenNew <delta-size> <input dir> <output dir>");
	}

  
	/**
	 * @param args
	 *          the cli arguments
	 */
	public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		if (args.length < 3) {
			usage();
			return 2;
		}
		
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		
		job.getConfiguration().set(DELTA_SIZE, args[2]);
    Path inputDir = new Path(args[0]);
		Path outputDir = new Path(args[1]);
		if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
    }
		
		SortInputFormat.setInputPaths(job, inputDir);
		SortOutputFormat.setOutputPath(job, outputDir);
		job.setJobName("GenerateNew");
		job.setJarByClass(GenerateNew.class);
		job.setMapperClass(GenerateNewMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(SortInputFormat.class);
		job.setOutputFormatClass(SortOutputFormat.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new GenerateNew(), args);
		System.exit(res);
	}

}
