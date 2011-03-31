package de.uniko.softlang.benchmarks.teraSort.sort;

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

import de.uniko.softlang.benchmarks.teraSort.generate.Generate;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;

/**
 * An output format that writes the key and value appended together.
 */
public class SortOutputFormat extends TextOutputFormat<LongWritable,Text> {
  
  static class MySortRecordWriter extends RecordWriter<LongWritable,Text> {
    private byte[] buffer = new byte[SortInputFormat.KEY_LENGTH];
    Text key = new Text();
    private boolean finalSync = true;
    private FSDataOutputStream out;

    
    public MySortRecordWriter(FSDataOutputStream fileOut, JobContext job){
    	this.out = fileOut;
    }
   
    public synchronized void write(LongWritable longKey, Text value) throws IOException {
    	
    	KVGenerator.longToBytes(buffer, longKey.get());
    	for (int i = Generate.LONG_SIZE; i < SortInputFormat.KEY_LENGTH; i++) {
				buffer[i] = 0;
			}
    	key.set(buffer, 0, SortInputFormat.KEY_LENGTH);
    	try{if(value.getLength() != 90){throw new Exception("Supposed to write value with size " + value.getLength());}}catch (Exception e) {e.printStackTrace();}
  		
    	out.write(key.getBytes(), 0, key.getLength());
      out.write(value.getBytes(), 0, value.getLength());
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

  public RecordWriter<LongWritable,Text> getRecordWriter(TaskAttemptContext job
                                                 ) throws IOException {
    Path file = getDefaultWorkFile(job, "");
    FileSystem fs = file.getFileSystem(job.getConfiguration());
    FSDataOutputStream fileOut = fs.create(file);
    return new MySortRecordWriter(fileOut, job);
  }
}
