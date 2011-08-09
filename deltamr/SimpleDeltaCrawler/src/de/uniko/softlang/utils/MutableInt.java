package de.uniko.softlang.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

/**
 * Container class for a mutable <code>Integer</code>.
 */
public class MutableInt implements Writable{

  private int value;
  
  public MutableInt() {
  	value = 0;
  }
  
  public MutableInt(int value){
  	this.value = value;
  }
  
  public int inc() { 
  	return ++value; 
  }
  
  public int incBy(int amount) { 
  	value += amount;
  	return value;
  }
  
  public int dec() { 
  	return --value; 
  }
  
  public int decBy(int amount) { 
  	value-= amount;
  	return value; 
  }
  
  public int get() { 
  	return value; 
  }
  
  public void set(int newVal) { 
  	value = newVal; 
  }
  
  public String toString(){
  	return ""+value;
  }

	@Override
	public void write(DataOutput out) throws IOException {
		IntWritable w = new IntWritable(value);
		w.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		IntWritable w = new IntWritable();
		w.readFields(in);
		value = w.get();
	}

}
