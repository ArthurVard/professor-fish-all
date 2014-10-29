package data.list.singlylinked.variation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static data.list.singlylinked.variation.IsSorted.*;
import static data.list.singlylinked.variation.MergeSort.*;
import static data.list.singlylinked.variation.Size.*;

import org.junit.Test;

/**
 * Test MergeSort
 */
public class TestMergeSort {
	
	public static IntListEntry unsortedList() {
		IntListEntry e1 = new IntListEntry();
		IntListEntry e2 = new IntListEntry();
		IntListEntry e3 = new IntListEntry();
		IntListEntry e4 = new IntListEntry();
		e1.item = 42;
		e1.next = e2;
		e2.item = 1;
		e2.next = e3;
		e3.item = 88;
		e3.next = e4;
		e4.item = 2;
		return e1;
	}

	@Test
	public void testMergeSort() {
		IntListEntry sorted = sort(unsortedList());
		assertTrue(isSortedIteratively0(sorted));
		assertEquals(4,sizeIteratively1(sorted));
		assertEquals(1,sorted.item);
		assertEquals(2,sorted.next.item);
		assertEquals(42,sorted.next.next.item);
		assertEquals(88,sorted.next.next.next.item);
	}		
}
