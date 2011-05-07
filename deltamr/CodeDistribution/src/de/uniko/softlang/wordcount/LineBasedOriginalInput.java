package de.uniko.softlang.wordcount;

import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

import de.uniko.softlang.utils.PairWritable;

public class LineBasedOriginalInput extends FileInputFormat<LongWritable, PairWritable<Text, BooleanWritable>> {   
	
	  
  @Override
  public RecordReader<LongWritable, PairWritable<Text, BooleanWritable>> createRecordReader(InputSplit split,
                                             TaskAttemptContext context
                                             ) throws IOException {
    return new RecordReader<LongWritable, PairWritable<Text, BooleanWritable>>(){
    	private LineRecordReader r;
      private LongWritable key = null;
      private PairWritable<Text, BooleanWritable> value = null;
      private BooleanWritable trueWriteable;

      public void initialize(InputSplit genericSplit,
                             TaskAttemptContext context) throws IOException {
    	  
      	r = new LineRecordReader();
      	r.initialize(genericSplit, context);
      	trueWriteable = new BooleanWritable(true);
        
      }
      
      @Override
      public boolean nextKeyValue() throws IOException {
        
        boolean retVal = r.nextKeyValue();
        
        if(!retVal){
        	key = null;	
        	value = null;
        	return false;
        }else{
        	key = r.getCurrentKey();
        	value = new PairWritable<Text, BooleanWritable>(r.getCurrentValue(), trueWriteable);
        }
         return true;
      	}
      
      @Override
      public LongWritable getCurrentKey() {
      	return key;
      }

      @Override
      public PairWritable<Text, BooleanWritable> getCurrentValue() {
    	  return value;
      }

      @Override
      public float getProgress() throws IOException, InterruptedException {
      	return r.getProgress();	
      }

      @Override
      public void close() throws IOException {
    		r.close();
      }
      
    };
  }
}
