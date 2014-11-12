// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

import static algorithm.search.Sortedness.*;
import static algorithm.search.LinearSearch.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestLinearSearch {
	
	@Test
	public void testFirst() {
		assertEquals(0,search(unsorted, 1));
	}

	@Test
	public void testSecond() {
		assertEquals(1,search(unsorted, 8));
	}
	
	@Test
	public void testLast() {
		assertEquals(9,search(unsorted, 2));
	}	
	
	@Test
	public void testMissing() {
		assertEquals(-1,search(unsorted, 42));
	}		
}
