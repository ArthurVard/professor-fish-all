package data.list.singlylinked.variation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static data.list.singlylinked.variation.IsSorted.*;

import org.junit.Test;

/**
 * Test sorting test
 */
public class TestIsSorted {

	public static IntListEntry sortedList() {
		IntListEntry e1 = new IntListEntry();
		IntListEntry e2 = new IntListEntry();
		IntListEntry e3 = new IntListEntry();
		e1.item = 42;
		e1.next = e2;
		e2.item = 77;
		e2.next = e3;
		e3.item = 88;
		return e1;
	}

	public static IntListEntry unsortedList() {
		IntListEntry e1 = new IntListEntry();
		IntListEntry e2 = new IntListEntry();
		IntListEntry e3 = new IntListEntry();
		e1.item = 42;
		e1.next = e2;
		e2.item = 1;
		e2.next = e3;
		e3.item = 88;
		return e1;
	}

	@Test
	public void testIsSortedIteratively0() {
		assertTrue(isSortedIteratively0(sortedList()));
	}
	
	@Test
	public void testIsNotSortedIteratively0() {
		assertFalse(isSortedIteratively0(unsortedList()));
	}		
	
	@Test
	public void testIsSortedIteratively1() {
		assertTrue(isSortedIteratively1(sortedList()));
	}
	
	@Test
	public void testIsNotSortedIteratively1() {
		assertFalse(isSortedIteratively1(unsortedList()));
	}	

	@Test
	public void testIsSortedIteratively2() {
		assertTrue(isSortedIteratively2(sortedList()));
	}
	
	@Test
	public void testIsNotSortedIteratively2() {
		assertFalse(isSortedIteratively2(unsortedList()));
	}	
	
	@Test
	public void testIsSortedRecursively1() {
		assertTrue(isSortedRecursively1(sortedList()));
	}
	
	@Test
	public void testIsNotSortedRecursively1() {
		assertFalse(isSortedRecursively1(unsortedList()));
	}	

	@Test
	public void testIsSortedRecursively2() {
		assertTrue(isSortedRecursively2(sortedList()));
	}
	
	@Test
	public void testIsNotSortedRecursively2() {
		assertTrue(!isSortedRecursively2(unsortedList()));
	}	
		
}
