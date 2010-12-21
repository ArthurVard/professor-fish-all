// (C) 2010, Ralf Laemmel

package iterator;

import java.util.Iterator;

public class EnumFromToIterator implements Iterator<Integer> {
	private int i;
	private int to;
	public EnumFromToIterator(int from, int to) {
		this.i = from;
		this.to = to;
	}
	public boolean hasNext() { return i<=to; }
	public Integer next() { return i++; }
	public void remove() { throw new UnsupportedOperationException(); }
}
