// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

import static algorithm.search.Sortedness.*;
import static algorithm.search.BinarySearchRecursively.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestBinarySearchRecursively {
	
	@Test
	public void testFirst() {
		assertEquals(0,search(sorted, 1));
	}

	@Test
	public void testSecond() {
		assertEquals(1,search(sorted, 2));
	}
	
	@Test
	public void testLast() {
		assertEquals(9,search(sorted, 10));
	}	
	
	/**
	 * The searched element is genuinely missing
	 */
	@Test
	public void testMissing() {
		assertEquals(-1,search(sorted, 42));
	}		

	/**
	 * The searched element is missed because sortnedness is violated
	 */
	@Test
	public void testMissed() {
		assertEquals(-1,search(unsorted, 5));
	}	
}
