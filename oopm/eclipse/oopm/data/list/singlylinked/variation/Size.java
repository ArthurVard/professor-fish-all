package data.list.singlylinked.variation;

/**
 * Different iterative and recursive formulations of a size function for singly-linked lists.
 */
public class Size {

	public static int sizeIteratively1(IntListEntry i) {
		int j = 0;
		while (i!=null) {
			j++;
			i=i.next;
		}
		return j;
	}

	public static int sizeRecursively1(IntListEntry i) {
		return i==null ?
			0
			: sizeRecursively1(i.next) + 1;
	}

	public static int sizeRecursively2(IntListEntry i) {
		return sizeRecursively2(0, i.next);
	}

	private static int sizeRecursively2(int s, IntListEntry i) {
		return i==null ?
			s
			: sizeRecursively2(s+1,i.next);
	}	

	public static int sizeIteratively2(int s, IntListEntry i) {
		while (i!=null) {
			s = s + 1;
			i = i.next;
		}
		return s;
	}	
		
}
