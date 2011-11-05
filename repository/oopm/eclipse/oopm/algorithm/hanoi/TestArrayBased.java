// (C) 2009 Ralf Laemmel

package algorithm.hanoi;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestArrayBased {
	@Test
	public void testNormal() {
		Move[] expected = {
			 	new Move(1,"A","C"),
			 	new Move(2,"A","B"),
			 	new Move(1,"C","B"),
			 	new Move(3,"A","C"),
			 	new Move(1,"B","A"),
			 	new Move(2,"B","C"),
			 	new Move(1,"A","C")  };
		Move[] actual = new Move[expected.length];
		ArrayBased.move(actual, 3,"A","B","C");
		assertArrayEquals(expected, actual);
	}
}
