package de.uniko.softlang.benchmarks.teraSort.merge;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
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


public class MergeByJoinInputFormat extends FileInputFormat<LongWritable,PairWritable<Text,BooleanWritable>>{

	public static final int KEY_LENGTH = 10;
  public static final int VALUE_LENGTH = 90;
  public static final int RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH;
  public static final int SIGN_LENGTH = GenerateDelta.POS_STR.getBytes().length;
  public static final int DELTA_RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH + SIGN_LENGTH;
  
  
  static class MergeRecordReader extends RecordReader<LongWritable,PairWritable<Text,BooleanWritable>> {
  	private FSDataInputStream in;
    private long offset;
    private long length;
    private byte[] buffer = new byte[DELTA_RECORD_LENGTH];
    private PairWritable<Text,BooleanWritable> value;
    private Text tmp = new Text();
    private Text sign = new Text();
    private LongWritable key;
  	private Text fileName;
  	private boolean deltaFile;
    
  	public void initialize(InputSplit split,TaskAttemptContext context) throws IOException, InterruptedException {
  		Path p = ((FileSplit)split).getPath();
      FileSystem fs = p.getFileSystem(context.getConfiguration());
      in = fs.open(p);
      long start = ((FileSplit)split).getStart();
      // find the offset to start at a record boundary
      length = ((FileSplit)split).getLength();
      this.fileName = new Text(((FileSplit)split).getPath().toString());	
    	Collection<String> origFiles = context.getConfiguration().getStringCollection(GenerateDelta.ORIG_FILES_SET);
			if(origFiles.contains(this.fileName.toString())){
				deltaFile = false; 
			}else{
				deltaFile = true;
			}
			int recordL = deltaFile ? DELTA_RECORD_LENGTH : RECORD_LENGTH;
    	offset = (recordL - (start % recordL)) % recordL;
    	in.seek(start + offset);
      
      
  	}
  	
  	public void close() throws IOException {
      in.close();
    }

    public LongWritable getCurrentKey() {
      return key;
    }

    public PairWritable<Text,BooleanWritable> getCurrentValue() {
      return value;
    }
    
    public float getProgress() throws IOException {
    	return (float) offset / length;
    }
  	
    public boolean nextKeyValue() throws IOException {
    	int recordL = deltaFile ? DELTA_RECORD_LENGTH : RECORD_LENGTH;
    	if(key == null){
    		value = new PairWritable<Text, BooleanWritable>(new Text(), new BooleanWritable());
    		sign = new Text();
    		if(!deltaFile)
    			value.getSecond().set(true);
    		key = new LongWritable();
    	}
    	if (offset >= length) {
        return false;
      }
      int read = 0;
      while (read < recordL) {
        long newRead = in.read(buffer, read, recordL - read);
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
      if(deltaFile){
      	sign.set(buffer, KEY_LENGTH+VALUE_LENGTH, SIGN_LENGTH);
      	if(sign.toString().equals("-")){
      		value.getSecond().set(false);
      	}else if(sign.toString().equals("+")){
      		value.getSecond().set(true);
      	}else{
      		try{throw new Exception("Invalid sign: " + sign.toString());}catch (Exception e) {e.printStackTrace();}
      	}
      }
      offset += recordL;
      return true;
    }
    
  }
  public RecordReader<LongWritable,PairWritable<Text,BooleanWritable>> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
  	return new MergeRecordReader();
}
}
