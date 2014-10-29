package data.list.singlylinked.variation;

/**
 * MergeSort algorithm on singly-linked lists
 */
public class MergeSort {

	/**
	 * Main function of MergeSort.
	 */
	public static IntListEntry sort(IntListEntry i) {
		if (i==null || i.next==null)
			return i;
		// This is a divide-and-conquer algorithm.
		PairOfIntLists pair = split(i);
		IntListEntry left = sort(pair.left);
		IntListEntry right = sort(pair.right);
		return merge(left, right);
	}

	/**
	 * Unstable split function for MergeSort
	 */
	private static PairOfIntLists split(IntListEntry i) {
		PairOfIntLists pair = new PairOfIntLists();
		IntListEntry j;
		while (i!=null) {
			// Prepend next element from i to left.
			j = i;
			i = i.next;
			j.next = pair.left;
			pair.left = j;
			if (i!=null) {
				// Prepend next element from i to right.
				j = i;
				i = i.next;
				j.next = pair.right;
				pair.right = j;				
			}
		}
		return pair;
	}
	
	/**
	 * Merge function on two singly-linked lists
	 */
	private static IntListEntry merge(IntListEntry left, IntListEntry right) {
		IntListEntry i = null; // Beginning of merged list
		IntListEntry j = null; // Last element of merged list
		while (left!=null || right!=null) {
			IntListEntry k; // Helper
			// Determine whether to take an element from the left
			if (left!=null && (right==null || left.item <= right.item)) {
				k = left;
				left = left.next;
			}
			// ... or from the right
			else {
				k = right;
				right = right.next;
			}	
			k.next = null; // unlink new element from rest
			if (i==null) i = k; // first element is head of result
			if (j!=null) j.next = k; // append element to result
			j = k; // update last element of list
		}
		return i;
	}
		
}
