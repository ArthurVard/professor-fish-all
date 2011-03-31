package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.EOFException;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import de.uniko.softlang.utils.PairWritable;



public class CreateDeltaInputFormat extends FileInputFormat<Text,PairWritable<LongWritable,Text>>{

	public static final int KEY_LENGTH = 10;
  public static final int VALUE_LENGTH = 90;
  public static final int RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH;
  
  static class MySortDeltaRecordReader extends RecordReader<Text,PairWritable<LongWritable,Text>> {
  	private FSDataInputStream in;
    private long offset;
    private long length;
    private byte[] buffer = new byte[RECORD_LENGTH];
    private Text key;
    private Text tmp = new Text();
  	private PairWritable<LongWritable,Text> value;
  	private Text line = new Text();
  	private Text fileName;
    
  	public void initialize(InputSplit split,TaskAttemptContext context) throws IOException, InterruptedException {
  		Path p = ((FileSplit)split).getPath();
      FileSystem fs = p.getFileSystem(context.getConfiguration());
      in = fs.open(p);
      long start = ((FileSplit)split).getStart();
      // find the offset to start at a record boundary
      offset = (RECORD_LENGTH - (start % RECORD_LENGTH)) % RECORD_LENGTH;
      in.seek(start + offset);
      length = ((FileSplit)split).getLength();
      this.fileName = new Text(((FileSplit)split).getPath().toString());	
  	}
  	
  	public void close() throws IOException {
      in.close();
    }

    public Text getCurrentKey() {
      return key;
    }

    public PairWritable<LongWritable,Text> getCurrentValue() {
      return value;
    }
    
    public float getProgress() throws IOException {
    	return (float) offset / length;
    }
  	
    public boolean nextKeyValue() throws IOException {
    	if(key == null){
    		key = new Text(fileName);
    		value = new PairWritable<LongWritable,Text>(new LongWritable(), new Text());
    	}
    	if (offset >= length) {
        return false;
      }
      int read = 0;
      while (read < RECORD_LENGTH) {
        long newRead = in.read(buffer, read, RECORD_LENGTH - read);
        if (newRead == -1) {
          if (read == 0) {
            return false;
          } else {
            throw new EOFException("read past eof");
          }
        }
        read += newRead;
      }
      tmp.set(buffer, 0, KEY_LENGTH);
      value.getFirst().set(KVGenerator.bytesToLong(tmp.getBytes()));
      value.getSecond().set(buffer, KEY_LENGTH, VALUE_LENGTH);
      offset += RECORD_LENGTH;
      return true;
    }
    
  }
  public RecordReader<Text, PairWritable<LongWritable,Text>> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
  	return new MySortDeltaRecordReader();
}
}
