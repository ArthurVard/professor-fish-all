package data.list.singlylinked.variation;
public class IsSorted {

	public static boolean isSortedIteratively0(IntListEntry i) {
		if (i==null)
			return true;
		int max = i.item;
		while ((i=i.next)!=null)
			if (i.item < max)
				return false;
			else
				max = i.item;
		return true;
	}	
	
	public static boolean isSortedIteratively1(IntListEntry i) {
		if (i==null)
			return true;
		while (i.next!=null) {
			if (i.item > i.next.item)
				return false;
			i = i.next;
		}
		return true;
	}
		
	public static boolean isSortedRecursively1(IntListEntry i) {
		if (i==null)
			return true;
		if (i.next == null)
			return true;
		if (i.item > i.next.item)
			return false;
		return isSortedRecursively1(i.next);
	}

	public static boolean p(IntListEntry i) {
		return i == null || i.next == null || i.item > i.next.item;
	}

	public static boolean g(IntListEntry i) {
		return i == null || i.next == null;
	}	

	public static IntListEntry r(IntListEntry i) {
		return i.next;
	}	
		
	public static boolean isSortedRecursively2(IntListEntry i) {
		if (p(i))
			return g(i);
		else
			return isSortedRecursively2(r(i));
	}

	public static boolean isSortedIteratively2(IntListEntry i) {
		while (!p(i))
			i = r(i);
		return g(i);
	}	
	
	public static boolean isSortedIteratively3(IntListEntry i) {
		while (!(i == null || i.next == null || i.item > i.next.item))
			i = i.next;
		return i == null || i.next == null;
	}	
		
}
