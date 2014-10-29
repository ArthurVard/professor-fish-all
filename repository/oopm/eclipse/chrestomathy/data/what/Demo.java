package data.what;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Demo {

	private void testLIFO(IntContainer x) {
		x.push(1);
		x.push(2);
		assertEquals(2, x.top());
	}

	private void testFIFO(IntContainer x) {
		x.enqueue(1);
		x.enqueue(2);
		assertEquals(1, x.front());
	}
		
	@Test
	public void LifoStackOk() {
		testLIFO(new UnboundedIntStack());
	}
	
	@Test(expected=java.lang.AssertionError.class)
	public void FifoStackFail() {
		testFIFO(new UnboundedIntStack());
	}
	
	@Test
	public void FifoQueueOk() {
		testFIFO(new SimpleIntQueue());
	}
	
	@Test(expected=java.lang.AssertionError.class)
	public void LifoQueueFail() {
		testLIFO(new SimpleIntQueue());
	}
}
