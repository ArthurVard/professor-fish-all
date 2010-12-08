// (C) 2009 Ralf Laemmel

package data.fifo;

public interface Queue<T> {
	
	/**
	 * Add an item to the queue
	 */
	void enqueue(T item);

	/**
	 * Test a queue to be empty
	 */
	boolean isEmpty();

	/**
	 * Return the item at the front of the queue
	 */
	T front();

	/**
	 * Remove an item from the queue
	 */
	void dequeue();
		
}
