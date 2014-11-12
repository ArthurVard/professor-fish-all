// (C) 2009-2014 Ralf Laemmel

package algorithm.search;

import static algorithm.search.Sortedness.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSortedness {
	
	@Test
	public void testSorted() {
		assertTrue(isSorted(sorted));
	}
	
	@Test
	public void testUnsorted() {
		assertFalse(isSorted(unsorted));
	}	
}
