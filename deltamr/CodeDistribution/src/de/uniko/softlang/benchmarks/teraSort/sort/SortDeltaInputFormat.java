package de.uniko.softlang.benchmarks.teraSort.sort;

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

import de.uniko.softlang.benchmarks.teraSort.generate.GenerateDelta;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;
import de.uniko.softlang.utils.PairWritable;



public class SortDeltaInputFormat extends FileInputFormat<LongWritable,PairWritable<Text,Text>>{

	public static final int KEY_LENGTH = 10;
  public static final int VALUE_LENGTH = 90;
  public static final int SIGN_LENGTH = GenerateDelta.POS_STR.getBytes().length;
  
  public static final int RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH + SIGN_LENGTH;
  
  static class SortDeltaRecordReader extends RecordReader<LongWritable,PairWritable<Text,Text>> {
  	private FSDataInputStream in;
    private long offset;
    private long length;
    private byte[] buffer = new byte[RECORD_LENGTH];
    private LongWritable key;
    private Text tmp = new Text();
  	private PairWritable<Text,Text> value;
     
  	public void initialize(InputSplit split,TaskAttemptContext context) throws IOException, InterruptedException {
  		Path p = ((FileSplit)split).getPath();
      FileSystem fs = p.getFileSystem(context.getConfiguration());
      in = fs.open(p);
      long start = ((FileSplit)split).getStart();
      // find the offset to start at a record boundary
      offset = (RECORD_LENGTH - (start % RECORD_LENGTH)) % RECORD_LENGTH;
      in.seek(start + offset);
      length = ((FileSplit)split).getLength();
  	}
  	
  	public void close() throws IOException {
      in.close();
    }

    public LongWritable getCurrentKey() {
      return key;
    }

    public PairWritable<Text,Text> getCurrentValue() {
      return value;
    }
    
    public float getProgress() throws IOException {
    	return (float) offset / length;
    }
  	
    public boolean nextKeyValue() throws IOException {
    	if(key == null){
    		key = new LongWritable();
    		value = new PairWritable<Text, Text>(new Text(), new Text());
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
      key.set(KVGenerator.bytesToLong(tmp.getBytes()));
      value.getFirst().set(buffer, KEY_LENGTH, VALUE_LENGTH);
      value.getSecond().set(buffer, KEY_LENGTH+VALUE_LENGTH, SIGN_LENGTH);
      offset += RECORD_LENGTH;
      return true;
    }
    
  }
  public RecordReader<LongWritable, PairWritable<Text,Text>> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
  	return new SortDeltaRecordReader();
  }
}	
