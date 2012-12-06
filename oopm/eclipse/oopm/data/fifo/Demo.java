package data.fifo;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Demo {

	@Test
	public void FifoQueueOk() {
		SimpleIntQueue x = new SimpleIntQueue();
		x.enqueue(1);
		x.enqueue(2);
		assertEquals(1, x.front());
	}
}
