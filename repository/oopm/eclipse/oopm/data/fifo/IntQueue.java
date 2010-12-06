// (C) 2009 Ralf Laemmel

package data.fifo;

public interface IntQueue {
	
	/**
	 * Add an item to the queue
	 */
	public void enqueue(int item);

	/**
	 * Test a queue to be empty
	 */
	public boolean isEmpty();

	/**
	 * Return the item at the front of the queue
	 */
	public int front();

	/**
	 * Remove an item from the queue
	 */
	public void dequeue();
		
}
