// (C) 2010, Ralf Laemmel

package iterator;

import java.util.Iterator;

public class Math {
	public static Iterator<Integer> enumFromTo(int from, int to) {
		return new EnumFromToIterator(from, to); 
	}
	public static int product(Iterator<Integer> list) {
		int result = 1;
		while (list.hasNext()) result *= list.next();
		return result;		
	}
}
