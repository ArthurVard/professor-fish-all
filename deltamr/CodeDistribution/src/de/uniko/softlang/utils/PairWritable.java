

package de.uniko.softlang.utils;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.record.Record;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.bloom.Key;

/** A WritableComparable for ints. */
public class PairWritable<T extends Writable, V extends Writable> implements WritableComparable, Configurable {
  private Pair<T, V> value;
  private Class fstClass;
  private Class sndClass;
  private AtomicReference<Configuration> conf;
   

  public PairWritable() {
	  value = new Pair();
	  this.conf = new AtomicReference<Configuration>();
  }

  public PairWritable(T fst, V snd) { 
	  value = new Pair(fst, snd);
	  fstClass = value.getFirst().getClass();
	  sndClass = value.getSecond().getClass();
	  this.conf = new AtomicReference<Configuration>();
  }
  
  public void setFirst(T val){
	  fstClass = val.getClass();
	  value.setFirst(val);
  }
  
  public void setSecond(V val){
  	sndClass = val.getClass();
	  value.setSecond(val);
  }

  public T getFirst(){
	  return value.getFirst();  
	}
  
  public V getSecond(){
	  return value.getSecond();
  }


  /** Set the value of this PairWritable. */
  public void set(Pair<T, V> value) { 
	  this.value = value; 
  }

  /** Return the value of this PairWritable. */
  public Pair<T, V> get() { return value; }

  public void readFields(DataInput in) throws IOException {
  	
  	//first, read in the class-names
  	try {
			fstClass = Class.forName(in.readUTF());
			sndClass = Class.forName(in.readUTF());
  	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	
  	//second, read in the values
  	Writable fst = (Writable) ReflectionUtils.newInstance(fstClass, getConf());
  	fst.readFields(in);
    
  	Writable snd = (Writable) ReflectionUtils.newInstance(sndClass, getConf());
  	snd.readFields(in);
    
  	value.setFirst((T)fst);
  	value.setSecond((V)snd);
  	
  }
  
 

  public void write(DataOutput out) throws IOException {
  	
  	//first, write out the classes of the Pair-elements
  	out.writeUTF(fstClass.getName());
  	out.writeUTF(sndClass.getName());
  	//then the values
  	value.getFirst().write(out);
  	value.getSecond().write(out);
  }
  

  /** Returns true iff <code>o</code> is a PairWritable with the same value. */
  public boolean equals(Object o) {
    if (!(o instanceof PairWritable))
      return false;
    PairWritable other = (PairWritable)o;
    if(this.compareTo(other) == 0)
    	return true;
    else 
    	return false;
  }

  public int hashCode() {
    return value.getFirst().hashCode() + value.getSecond().hashCode();
  }

  /** Compares two PairWritables. */
  public int compareTo(Object o) {
  	WritableComparable thisValue1 = (WritableComparable) this.value.getFirst();
  	WritableComparable thisValue2 = (WritableComparable)this.value.getSecond();
    WritableComparable thatValue1 = (WritableComparable)((PairWritable)o).value.getFirst();
    WritableComparable thatValue2 = (WritableComparable)((PairWritable)o).value.getSecond();
    if(thisValue1.compareTo(thatValue1) == 0){
    	return thisValue2.compareTo(thatValue2);
    	
    }else{
    	return thisValue1.compareTo(thatValue1);
    }
    
  }

  public String toString() {
    return value.toString();
  }

  /** A Comparator optimized for PairWritable. */ 
  public static class PairWritableComparator extends WritableComparator {
    public PairWritableComparator() {
      super(PairWritable.class);
    }

    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
    	// TODO correct?
    	return compareBytes(b1, s1, l1, b2, s2, l2);    }
  }

  static {                                        // register this comparator
    WritableComparator.define(PairWritable.class, new PairWritableComparator());
  }

	@Override
	public Configuration getConf() {
		return conf.get();
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf.set(conf);
	}
}

