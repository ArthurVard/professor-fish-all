package data.lifo;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestUnboundedIntStack {

	/*
	 * A newly created stack is empty.
	 */
	@Test
	public void testEmpty() {
		IntStack s = new UnboundedIntStack();
		assertTrue(s.isEmpty());		
	}
	
	/*
	 * An empty stack throws on pop.
	 */
	@Test(expected=java.lang.NullPointerException.class)
	public void testPopWhenEmpty() {
		IntStack s = new UnboundedIntStack();
		s.pop();
	}

	/*
	 * An empty stack throws on top.
	 */
	@Test(expected=java.lang.NullPointerException.class)
	public void testTopWhenEmpty() {
		IntStack s = new UnboundedIntStack();
		s.top();
	}	
		
	/*
	 * A stack is non-empty after push.
	 * Also, top returns the pushed item.
	 */
	@Test()
	public void testPush() {
		IntStack s = new UnboundedIntStack();
		int item = 1;
		s.push(item);
		assertFalse(s.isEmpty());
		assertEquals(item,s.top());
	}
	
	/*
	 * When an item is popped off the stack, 
	 * then the previously pushed item becomes top-of-stack.
	 */
	@Test()
	public void testPop() {
		IntStack s = new UnboundedIntStack();
		int item1 = 1;
		int item2 = 2;
		s.push(item1);
		s.push(item2);
		assertEquals(item2,s.top());
		s.pop();
		assertEquals(item1,s.top());
	}	
}
