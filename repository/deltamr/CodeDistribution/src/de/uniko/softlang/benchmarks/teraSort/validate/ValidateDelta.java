package de.uniko.softlang.benchmarks.teraSort.validate;

import java.io.IOException;
import java.util.zip.Checksum;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.benchmarks.teraSort.generate.Generate;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;
import de.uniko.softlang.benchmarks.teraSort.original.Unsigned16;
import de.uniko.softlang.benchmarks.teraSort.sort.SortDeltaInputFormat;
import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;
import de.uniko.softlang.utils.PairWritable;


/**
 * Generate 1 mapper per a file that checks to make sure the keys
 * are sorted within each file. The mapper also generates 
 * "$file:begin", first key and "$file:end", last key. The reduce verifies that
 * all of the start/end items are in order.
 * Any output from the reduce is problem report.
 * <p>
 * To run the program: 
 * <b>bin/hadoop jar hadoop-*-examples.jar teravalidate out-dir report-dir</b>
 * <p>
 * If there is any output, something is wrong and the output of the reduce
 * will have the problem report.
 */
public class ValidateDelta extends Configured implements Tool {
  private static final Text ERROR = new Text("error");
  private static final Text CHECKSUM = new Text("checksum");
  
  private static String textifyBytes(Text t) {
    BytesWritable b = new BytesWritable();
    b.set(t.getBytes(), 0, t.getLength());
    return b.toString();
  }

  static class ValidateMapper extends Mapper<LongWritable,PairWritable<Text,Text>,Text,Text> {
    private LongWritable lastKey;
    private String filename;
    private Unsigned16 checksum = new Unsigned16();
    private Unsigned16 tmp = new Unsigned16();
    private Checksum crc32 = new PureJavaCrc32();
    private byte[] keyBuffer = new byte[SortInputFormat.KEY_LENGTH];
		

    /**
     * Get the final part of the input name
     * @param split the input split
     * @return the "part-r-00000" for the input
     */
    private String getFilename(FileSplit split) {
      return split.getPath().getName();
    }

    public void map(LongWritable key, PairWritable<Text,Text> value, Context context) 
        throws IOException, InterruptedException {
      if (lastKey == null) {
        FileSplit fs = (FileSplit) context.getInputSplit();
        filename = getFilename(fs);
        byte[] tmpBuff = new byte[Generate.LONG_SIZE];
        KVGenerator.longToBytes(tmpBuff, key.get());
        context.write(new Text(filename + ":begin"), new Text(tmpBuff));
        lastKey = new LongWritable();
      } else {
        if (key.compareTo(lastKey) < 0) {
          context.write(ERROR, new Text("misorder in " + filename + 
                                         " between " + lastKey.get() + 
                                         " and " + lastKey.get()));
        }
      }
      // compute the crc of the key and value and add it to the sum
      KVGenerator.longToBytes(keyBuffer, key.get());
			crc32.reset();
      crc32.update(keyBuffer, 0, keyBuffer.length);
      crc32.update(value.getFirst().getBytes(), 0, value.getFirst().getLength());
      tmp.set(crc32.getValue());
      checksum.add(tmp);
      lastKey.set(key.get());
    }
    
    public void cleanup(Context context) 
        throws IOException, InterruptedException  {
      if (lastKey != null) {
      	byte[] tmpBuff = new byte[Generate.LONG_SIZE];
        KVGenerator.longToBytes(tmpBuff, lastKey.get());
        context.write(new Text(filename + ":end"), new Text(tmpBuff));
        context.write(CHECKSUM, new Text(checksum.toString()));
      }
    }
  }

  /**
   * Check the boundaries between the output files by making sure that the
   * boundary keys are always increasing.
   * Also passes any error reports along intact.
   */
  static class ValidateReducer extends Reducer<Text,Text,Text,Text> {
    private boolean firstKey = true;
    private Text lastKey = new Text();
    private long lastValue;
    public void reduce(Text key, Iterable<Text> values,
        Context context) throws IOException, InterruptedException  {
      if (ERROR.equals(key)) {
        for (Text val : values) {
          context.write(key, val);
        }
      } else if (CHECKSUM.equals(key)) {
        Unsigned16 tmp = new Unsigned16();
        Unsigned16 sum = new Unsigned16();
        for (Text val : values) {
          tmp.set(val.toString());
          sum.add(tmp);
        }
        context.write(CHECKSUM, new Text(sum.toString()));
      } else {
        Text value = values.iterator().next();
        long longValue = KVGenerator.bytesToLong(value.getBytes());
        
        if (firstKey) {
          firstKey = false;
        } else {
        	if (longValue < lastValue) {
            context.write(ERROR, 
                           new Text("bad key partitioning:\n  file " + 
                                    lastKey + " key " + 
                                    lastValue +
                                    "\n  file " + key + " key " + 
                                    longValue));
          }
        }
        lastKey.set(key);
        lastValue = longValue;;
      }
    }
    
  }

  private static void usage() throws IOException {
    System.err.println("teravalidate <out-dir> <report-dir>");
  }

  public int run(String[] args) throws Exception {
    Job job = Job.getInstance(new Cluster(getConf()), getConf());
    if (args.length < 2) {
      usage();
      return 1;
    }
    Path outputDir = new Path(args[1]);
    if(outputDir.getFileSystem(job.getConfiguration()).exists(outputDir)){
    	outputDir.getFileSystem(job.getConfiguration()).delete(outputDir, true);
    }
    SortDeltaInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, outputDir);
    job.setJobName("TeraValidate");
		job.setJarByClass(ValidateDelta.class);
    job.setMapperClass(ValidateMapper.class);
    job.setReducerClass(ValidateReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    // force a single reducer
    job.setNumReduceTasks(1);
    // force a single split 
    FileInputFormat.setMinInputSplitSize(job, Long.MAX_VALUE);
    job.setInputFormatClass(SortDeltaInputFormat.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new ValidateDelta(), args);
    System.exit(res);
  }

}
