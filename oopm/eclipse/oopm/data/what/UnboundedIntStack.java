// (C) 2009 Ralf Laemmel

package data.what;

import data.list.singlylinked.*;

public class UnboundedIntStack implements IntContainer {
	
	private IntListEntry top = null;
	
	public void push(int item) {
		IntListEntry e = new IntListEntry();
		e.item = item;
		e.next = top;
		top = e;
	}

	// Let's confuse Stack and Queue.
	public void enqueue(int item) {
		push(item);
	}
	
	public boolean isEmpty() {
		return top == null;
	}
	
	public int top() {
		return top.item;
	}
	
	// Let's confuse Stack and Queue.
	public int front() {
		return top();
	}
	
	public void pop() {
		top = top.next;
	}
	
	// Let's confuse Stack and Queue.
	public void dequeue() {
		pop();
	}
}
