// (C) 2010 Ralf Laemmel

package data.list.singlylinked;

/**
 * A simple list of integers.
 * We can add items in constant time.
 * We can iterate over the list.
 * (For simplicity, we do not use iterators at this point.)
 */
public class SimpleIntList {

	private IntListEntry first;
	private IntListEntry last;
	private IntListEntry current;
	private int length;
	
	/**
	 * Return the length of the list.
	 * All mutations must maintain the length.
	 * (This is a constant-time operation.)
	 */
	public int getLength() {
		return length; 
	}
	
	/**
	 * Add (append) an element to the list.
	 * (This is a constant-time operation.)
	 */
	public void add(int item) {
		IntListEntry e = new IntListEntry();
		e.item = item;
		if (last!=null)
			last.next = e;
		last = e;
		if (first==null)
			first = e;	
		length++;
	}

	/**
	 * Append a list to the current list.
	 */
	public void append(SimpleIntList l) {
		if (last!=null)
			last.next = l.first;
		if (l.last!=null)
			last = l.last;
		if (first==null)
			first = l.first;	
		length += l.length;
	}
	
	/** 
	 * Reset the iterator pointer to the beginning of the list.
	 */
	public void reset() { 
		current = first; 
	}
	
	/**
	 * Check whether there is still an element to process.
	 */
	public boolean hasNext() {
		return current != null; 
	}

	/**
	 * Retrieve the next element (if any; throw otherwise).
	 */
	public int next() {
		int item = current.item;
		current = current.next;
		return item;
	}
}
