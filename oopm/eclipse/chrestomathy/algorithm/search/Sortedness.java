// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

public class Sortedness {

	public static int[] sorted = {1,2,3,4,5,6,7,8,9,10};	
	public static int[] unsorted = {1,8,10,4,6,3,7,5,9,2};	

	/*
	 * Test an array to be sorted (in ascending order)
	 */
	public static boolean isSorted(int[] a) {
		for (int i=1; i<a.length; i++)
			if (a[i-1] > a[i])
				return false;
		return true;
	}
}
