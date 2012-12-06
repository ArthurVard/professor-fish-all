// (C) 2009 Ralf Laemmel

package data.what;

import data.list.singlylinked.*;

public class SimpleIntQueue implements IntContainer {
	
	private IntListEntry first = null;
	private IntListEntry last = null;
	
	// Let's confuse Stack and Queue.
	public void push(int item) {
		enqueue(item);
	}
	
	public void enqueue(int item) {
		IntListEntry e = new IntListEntry();
		e.item = item;
		e.next = null;
		if (first==null)
			first = e;
		if (last!=null)
			last.next = e;
		last = e;
	}
	
	public boolean isEmpty() {
		return first == null;
	}
	
	// Let's confuse Stack and Queue.
	public int top() {
		return front();
	}
	
	public int front() {
		return first.item;
	}
	
	// Let's confuse Stack and Queue.
	public void pop() {
		dequeue();
	}
	
	public void dequeue() {
		if (first==last)
			last = null;
		first = first.next;
	}
}
