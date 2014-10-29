// (C) 2009 Ralf Laemmel

package data.lifo;

import data.list.singlylinked.*;

public class UnboundedIntStack implements IntStack {
	
	private IntListEntry top = null;
	
	public void push(int item) {
		IntListEntry e = new IntListEntry();
		e.item = item;
		e.next = top;
		top = e;
	}
	
	public boolean isEmpty() {
		return top == null;
	}
	
	public int top() {
		return top.item;
	}
	
	public void pop() {
		top = top.next;
	}
	
	// This code should not be part of the ADT class!
	public static void main(String[] args) {
		UnboundedIntStack s = new UnboundedIntStack();
		s.push(1); System.out.println(s.top());
		s.push(2); System.out.println(s.top());
		s.push(3); System.out.println(s.top());
		while (!s.isEmpty()) {
			System.out.println(s.top());
			s.pop();
		}
	}
}
