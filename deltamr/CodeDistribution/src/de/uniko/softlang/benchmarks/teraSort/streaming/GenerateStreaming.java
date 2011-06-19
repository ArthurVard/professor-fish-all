package de.uniko.softlang.benchmarks.teraSort.streaming;

import java.io.IOException;
import java.util.zip.Checksum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.terasort.TeraGen.Counters;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.generate.Generate;
import de.uniko.softlang.benchmarks.teraSort.generate.Generate.RangeInputFormat;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;
import de.uniko.softlang.benchmarks.teraSort.original.Random16;
import de.uniko.softlang.benchmarks.teraSort.original.Unsigned16;
import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;
import de.uniko.softlang.benchmarks.teraSort.sort.SortOutputFormat;


public class GenerateStreaming extends Configured implements Tool {
	private static final Log LOG = LogFactory.getLog(GenerateStreaming.class);
	
	
	/**
	 * The Mapper class that given a row number, will generate the appropriate
	 * output line.
	 */
	public static class GenerateNewMapper extends Mapper<LongWritable, NullWritable, LongWritable, Text> {
		
		private Unsigned16 rand = null;
		private Unsigned16 rowId = null;
		private LongWritable key = new LongWritable();
		private Text value = new Text();

	    private byte[] keyBuffer = new byte[SortInputFormat.KEY_LENGTH];
		
		private Unsigned16 checksum = new Unsigned16();
		private Checksum crc32 = new PureJavaCrc32();
		private Unsigned16 total = new Unsigned16();
		private Counter checksumCounter;

		private int valueOffset = 0;
		
		public void map(LongWritable row, NullWritable ignored, Context context)
				throws IOException, InterruptedException {
			if (rand == null) {
				checksumCounter = context.getCounter(Counters.CHECKSUM);
			}
			rowId = new Unsigned16(row.get());
			rand = Random16.skipAhead(rowId);
			Random16.nextRand(rand);

			KVGenerator.generateDetRecord(key, value, rand.getHigh8(), row.get(),
					valueOffset, new boolean[0]);
			valueOffset += 2;

			context.write(key, value);

			KVGenerator.longToBytes(keyBuffer, 0, key.get());
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
		System.err.println("genStream <num rows> <output dir>");
	}

  
	/**
	 * @param args
	 *          the cli arguments
	 */
	public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		if (args.length < 2) {
			usage();
			return 2;
		}
		
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		
		Path outputDir;
		job.getConfiguration().setLong(Generate.NUM_ROWS,Generate.parseHumanLong(args[0]));
		job.setInputFormatClass(RangeInputFormat.class);
		outputDir = new Path(args[1]);
		
		if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
			outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
		}
		SortOutputFormat.setOutputPath(job, outputDir);

		job.setJobName("GenerateStreaming");
		job.setJarByClass(GenerateStreaming.class);
		job.setMapperClass(GenerateNewMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(RangeInputFormat.class);
		job.setOutputFormatClass(SortOutputFormat.class);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new GenerateStreaming(), args);
		System.exit(res);
	}

}
