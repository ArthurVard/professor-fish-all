// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

public class BinarySearchRecursively {

	/*
	 * Find the index of x in an array a.
	 * Return -1 to mean that x was not found.
	 * We implement binary search.
	 */
	public static int search(int[] a, int x) {
		return search(a, x, 0, a.length-1);
	}
	
	public static int search(int[] a, int x, int first, int last) {
		if (first>last)
			return -1; // not found
		int middle = first + ((last - first) / 2);
		if (a[middle] < x)
			return search(a, x, middle+1, last); // right half
		else if (a[middle] > x)
			return search(a, x, first, middle-1); // left half
		else
			return middle;
	}
}
