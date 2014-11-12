// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

public class BinarySearchIteratively {

	/*
	 * Find the index of x in an array a.
	 * Return -1 to mean that x was not found.
	 * We implement binary search.
	 */
	public static int search(int[] a, int x) {
		int first = 0; // first index in range
		int last = a.length - 1; // last index in range

		while (first <= last) {
			int middle = first + ((last - first) / 2);
			if (a[middle] < x) {
				first = middle + 1; // go to the right
			} else if (a[middle] > x) {
				last = middle - 1; // go to the left
			} else
				return middle;
		}
		return -1; // not found
	}
}
