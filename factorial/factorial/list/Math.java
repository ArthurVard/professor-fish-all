// (C) 2010, Ralf Laemmel

package list;

import java.util.*;

public class Math {
	public static List<Integer> enumFromTo(int from, int to) {
		List<Integer> result = new LinkedList<Integer>();
		for (int i=from; i<=to; i++) result.add(i);
		return result;
	}
	public static int product(List<Integer> list) {
		int result = 1;
		for (int x : list) result *= x;
		return result;
	}
}
