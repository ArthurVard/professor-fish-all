package de.uniko.softlang.wordcount;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class FileNameLineInputFormat extends FileInputFormat<Text, Text> {   
	
	public static final String ORIG_FILES_SET = "orig.files.names";
	  
  @Override
  public RecordReader createRecordReader(InputSplit split,
                                             TaskAttemptContext context
                                             ) throws IOException {
    return new RecordReader<Text,Text>(){
    	private LineRecordReader r;
      private Text key = null;
      private Text value = null;
      private String fileName;
     

      public void initialize(InputSplit genericSplit,
                             TaskAttemptContext context) throws IOException {
    	  
      	this.fileName = ((FileSplit)genericSplit).getPath().toString();	
      	r = new LineRecordReader();
      	r.initialize(genericSplit, context);
        
      }
      
      @Override
      public boolean nextKeyValue() throws IOException {
      	if (key == null) {
          key = new Text(fileName);
        }
        if (value == null) {
          value = new Text();
        }
        
        boolean retVal = r.nextKeyValue();
        
        if(!retVal){
        	key = null;	
        	value = null;
        	return false;
        }else{
        	value = r.getCurrentValue();
        }
         return true;
      	}
      
      @Override
      public Text getCurrentKey() {
      	return key;
      }

      @Override
      public Text getCurrentValue() {
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
