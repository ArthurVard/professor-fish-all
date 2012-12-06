// (C) 2009 Ralf Laemmel

package data.what;

public interface IntQueue {
	
	/**
	 * Add an item to the queue
	 */
	void enqueue(int item);

	/**
	 * Test a queue to be empty
	 */
	boolean isEmpty();

	/**
	 * Return the item at the front of the queue
	 */
	int front();

	/**
	 * Remove an item from the queue
	 */
	void dequeue();
		
}
