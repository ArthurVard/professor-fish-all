package de.uniko.softlang.benchmarks.teraSort.streaming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.Checksum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.terasort.TeraGen.Counters;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.generate.GenerateDelta;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;
import de.uniko.softlang.benchmarks.teraSort.original.Random16;
import de.uniko.softlang.benchmarks.teraSort.original.Unsigned16;
import de.uniko.softlang.benchmarks.teraSort.sort.SortDeltaOutputFormat;
import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;
import de.uniko.softlang.utils.PairWritable;


public class GenerateStreamingDelta extends Configured implements Tool {
	public static Text posStr;
	public static Text negStr;
	private static final Log LOG = LogFactory.getLog(GenerateStreamingDelta.class);
	private static final String DELTA_SIZE = "uni-ko.delta.size";

	static enum TupleStatistics { INPUT_TUPLES, CANCELED_TUPLES, PROCESSED_TUPLES, CHANGED_TUPLES };
	/**
	 * The Mapper class that given a row number, will generate the appropriate
	 * output line.
	 */
	public static class StreamingDeltaMapper extends Mapper<LongWritable, TupleWritable, LongWritable,PairWritable<Text,Text>> {
		private float deltaSize;
		private LongWritable newKey;
		private Text newValue;

		private byte[] keyBuffer = new byte[SortInputFormat.KEY_LENGTH];

		private Unsigned16 rand = null;
		private Unsigned16 rowId = null;
		private Unsigned16 checksum = new Unsigned16();
		private Checksum crc32 = new PureJavaCrc32();
		private Unsigned16 total = new Unsigned16();
		private Counter checksumCounter;

		private int valueOffset = 0;

		public void setup(Context context){
			try{
				deltaSize = Float.parseFloat(context.getConfiguration().get(DELTA_SIZE))/100;
				deltaSize /= 2;	//since a modified key or value will resut in 2 delta-entries (add & delete)
			}catch(NumberFormatException e){
				LOG.error("Invalid change-percentage: " + context.getConfiguration().get(DELTA_SIZE) + " => set to 10%");
				deltaSize = 0.1F;
			}
			newKey = new LongWritable(0);
			newValue = new Text();
			posStr = new Text(GenerateDelta.POS_STR);
			negStr = new Text(GenerateDelta.NEG_STR);
		}

		private boolean keep(){
			double rand = Math.random();
			if(rand < deltaSize){
				return false;
			}else{
				return true;
			}
		}

		public void map(LongWritable oldKey, TupleWritable values, Context context)
		throws IOException, InterruptedException {

			context.getCounter(TupleStatistics.INPUT_TUPLES).increment(values.size());
			TreeSet<PairWritable<Text, Text>> oldValues = new TreeSet<PairWritable<Text,Text>>();
			for(Writable val : values){
				PairWritable<Text, Text> pair = (PairWritable<Text, Text>)val;
				oldValues.add(pair);
			}
			
			PairWritable<Text, Text> last = null;
			LinkedList<PairWritable<Text, Text>> oldReduced = new LinkedList<PairWritable<Text, Text>>();
			for(PairWritable<Text, Text> next : oldValues){
				//sort oldValues by first -> iterate and evict
				if(last == null){
					last = new PairWritable(next.getFirst(), next.getSecond());
					//last = next;
				}else{
					if(!next.getSecond().toString().equals(last.getSecond().toString())){
						//they invert each other -> omit both
						last = null;
						context.getCounter(TupleStatistics.CANCELED_TUPLES).increment(2);
					}else{
						if(last.getSecond().toString().equals(GenerateDelta.NEG_STR))
							LOG.warn("Negative amount of tuples for key...skipping!");
						else{
							oldReduced.add(last);
						}
						last = next;
					}
				}
			}
			if(last != null){
				oldReduced.add(last);
			}
			for(PairWritable<Text, Text> pair : oldReduced){
				context.getCounter(TupleStatistics.PROCESSED_TUPLES).increment(1);
				Text oldValue = pair.getFirst();
				boolean[] versions = KVGenerator.extractVersionInfo(oldValue);
				long rowL = KVGenerator.extractRowId(oldValue);
				if (rand == null) {
					checksumCounter = context.getCounter(Counters.CHECKSUM);
				}
				rowId = new Unsigned16(rowL);
				rand = Random16.skipAhead(rowId);
				Random16.nextRand(rand);
				
				if(keep()){
					versions[versions.length-1] = false;
				}else{
					context.getCounter(TupleStatistics.CHANGED_TUPLES).increment(1);
					versions[versions.length-1] = true;
				}
				KVGenerator.generateDetRecord(newKey, newValue, rand.getHigh8(), rowL, valueOffset, versions);
				
				valueOffset += 2;

				//create diff 
				if(oldKey.equals(newKey)){
					return;	//no changes
				}else{
					//write the newly created version, marked as 'added'
					context.write(newKey, new PairWritable<Text,Text>(newValue, posStr));
					//write the old version, marked as 'deleted'
					context.write(oldKey, new PairWritable<Text,Text>(oldValue, negStr));
				}

				KVGenerator.longToBytes(keyBuffer, 0, newKey.get());
				crc32.reset();
				crc32.update(keyBuffer, 0, SortInputFormat.KEY_LENGTH);
				crc32.update(newValue.getBytes(), 0, SortInputFormat.VALUE_LENGTH);
				checksum.set(crc32.getValue());
				total.add(checksum);
			}
		}

		@Override
		public void cleanup(Context context) {
			checksumCounter.increment(total.getLow8());
			System.out.println("Checksum  = " + total.getLow8());
		}
	}



	private static void usage() throws IOException {
		System.err.println("genStreamDelta <delta-size> <output dir> <original version> [<delta>*]");
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

		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("GenerateStreamingDelta");
		String deltaSize = args[0];
		job.getConfiguration().set(DELTA_SIZE, deltaSize);

		//input
		List<Path> plist = new ArrayList<Path>(2);
		Path originalDir = new Path(args[2]);
		plist.add(originalDir);

		//remember names of delta-files
		String deltaFiles = "";
		for(int i = 3; i < args.length; i++){
			plist.add(new Path(args[i]));
			deltaFiles = deltaFiles.concat(args[i] + ",");
		}
		job.getConfiguration().set(GenStreamingInputFormat.DELTA_FILES_SET, deltaFiles);

		job.setInputFormatClass(CompositeInputFormat.class);
		job.getConfiguration().set(CompositeInputFormat.JOIN_EXPR, 
				CompositeInputFormat.compose("outer", GenStreamingInputFormat.class,
						plist.toArray(new Path[0])));

		//output
		Path outputDir;
		outputDir = new Path(args[1]);
		if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
			outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
		}
		SortDeltaOutputFormat.setOutputPath(job, outputDir);
		job.setOutputFormatClass(SortDeltaOutputFormat.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(PairWritable.class);

		//MR
		job.setJarByClass(GenerateStreamingDelta.class);
		job.setMapperClass(StreamingDeltaMapper.class);
		job.setNumReduceTasks(0);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new GenerateStreamingDelta(), args);
		System.exit(res);
	}

}
