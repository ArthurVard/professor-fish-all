package de.uniko.softlang.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

public class ListWritable extends AbstractListWritable implements Iterable<Writable>{
	List<Writable> instance;
	
	
	public ListWritable() {
		super();
		this.instance = new LinkedList<Writable>();
	}
		
	/** {@inheritDoc} */
	public boolean add(Writable value){
    addToMap(value.getClass());
		return instance.add(value);
	}
	
	public void add(int index, Writable value){
    addToMap(value.getClass());
		instance.add(index, value);
	}
	
	public void addAll(ListWritable list){
		Iterator<Writable> it = list.iterator();
		while(it.hasNext())
			add(it.next());
	}
	  
	public void clear(){
		instance.clear();
	}
	
	public boolean contains(Object value){
		return instance.contains(value);
	}
	
	public Writable get(int index){
		return instance.get(index);
	}
	
	public int indexOf(Writable value){
		return instance.indexOf(value);
	}
	
	public boolean isEmpty(){
		return instance.isEmpty();
	}
	
	public Iterator<Writable> iterator(){
		return instance.iterator();
	}
	
	public Writable remove(int index){
		return instance.remove(index);
	}
	
	public boolean remove(Writable value){
		return instance.remove(value);
	}
	
	public Writable set(int index, Writable value){
    addToMap(value.getClass());
		return instance.set(index, value);
	}
	
	public int size(){
		return instance.size();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		
		// Write out the number of entries in the map
    out.writeInt(instance.size());
    
    // Then write out each value
    for(Writable w : instance){
			out.writeByte(getId(w.getClass()));
      w.write(out);
		}
	}

	/** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		
		// First clear the map.  Otherwise we will just accumulate
    // entries every time this method is called.
    instance.clear();
		
    // Read the number of entries in the map
    int entries = in.readInt();
		
    // Then read each value pair
    for (int i = 0; i < entries; i++) {
			Writable value = (Writable) ReflectionUtils.newInstance(getClass(
          in.readByte()), getConf());
			value.readFields(in);
			instance.add(value);
		}
	}
  
  public Text prettyPrint(){
  	String retVal = "\t\t[";
  	for(Writable w : instance){
  		retVal += w.toString() + ", ";
  	}
  	retVal = retVal.substring(0, retVal.lastIndexOf(','));
  	retVal += "]";
  	return new Text(retVal);
  }
}
