// (C) 2010, Ralf Laemmel

package iterable;

public class Math {
	public static Iterable<Integer> enumFromTo(int from, int to) {
		return new EnumFromTo(from, to); 
	}
	public static int product(Iterable<Integer> list) {
		int result = 1;
		for (int x : list) result *= x;
		return result;
	}
}
