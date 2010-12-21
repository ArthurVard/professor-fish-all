// (C) 2010, Ralf Laemmel

package iterable;

import java.util.Iterator;

public class EnumFromToIterator implements Iterator<Integer> {
	private EnumFromTo range;
	private int i;
	public EnumFromToIterator(EnumFromTo range) {
		this.range = range;
		this.i = range.from;
	}
	public boolean hasNext() { return i<=range.to; }
	public Integer next() { return i++; }
	public void remove() { throw new UnsupportedOperationException(); }
}
