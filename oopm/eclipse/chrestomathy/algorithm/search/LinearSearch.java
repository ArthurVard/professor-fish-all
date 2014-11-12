// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

public class LinearSearch {

	/*
	 * Find the index of x in an array a.
	 * Return -1 to mean that x was not found.
	 * We implement linear search.
	 */
	public static int search(int[] a, int x) {
		for (int i=0; i<a.length; i++)
			if (a[i] == x) 
				return i;
		return -1;
	}
}
