package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;
import de.uniko.softlang.utils.PairWritable;


/**
 * An output format that writes the key and value appended together.
 */
public class DeltaOutputFormat extends TextOutputFormat<LongWritable,PairWritable<Text,Text>> {
  
  static class MySortDeltaRecordWriter extends RecordWriter<LongWritable,PairWritable<Text,Text>> {
    private byte[] buffer = new byte[SortInputFormat.KEY_LENGTH];
    Text key = new Text();
    private boolean finalSync = true;
    private FSDataOutputStream out;

    
    public MySortDeltaRecordWriter(FSDataOutputStream fileOut, JobContext job){
    	this.out = fileOut;
    }
   
    public synchronized void write(LongWritable longKey, PairWritable<Text,Text> value) throws IOException {
    	
    	KVGenerator.longToBytes(buffer, longKey.get());
    	for (int i = Generate.LONG_SIZE; i < SortInputFormat.KEY_LENGTH; i++) {
				buffer[i] = 0;
			}
    	key.set(buffer, 0, SortInputFormat.KEY_LENGTH);
    	
    	out.write(key.getBytes(), 0, key.getLength());
      out.write(value.getFirst().getBytes(), 0, value.getFirst().getLength());
      out.write(value.getSecond().getBytes(), 0, value.getSecond().getLength());
    }
    
    public void close(TaskAttemptContext context) throws IOException {
    	if (finalSync) {
        out.sync();
      }
      out.close();
    }
  }

  @Override
  public void checkOutputSpecs(JobContext job
                              ) throws InvalidJobConfException, IOException {
    // Ensure that the output directory is set
    Path outDir = getOutputPath(job);
    if (outDir == null) {
      throw new InvalidJobConfException("Output directory not set in JobConf.");
    }
  }

  public RecordWriter<LongWritable,PairWritable<Text,Text>> getRecordWriter(TaskAttemptContext job
                                                 ) throws IOException {
    Path file = getDefaultWorkFile(job, "");
    FileSystem fs = file.getFileSystem(job.getConfiguration());
    FSDataOutputStream fileOut = fs.create(file);
    return new MySortDeltaRecordWriter(fileOut, job);
  }
}
