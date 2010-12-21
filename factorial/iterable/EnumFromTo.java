// (C) 2010, Ralf Laemmel

package iterable;

import java.util.Iterator;

public class EnumFromTo implements Iterable<Integer> {
    int from;
	int to;
	public EnumFromTo(int from, int to) {
		this.from = from;
		this.to = to;
	}
	public Iterator<Integer> iterator() {
		return new EnumFromToIterator(this);
	}
}
