// (C) 2009 Ralf Laemmel

package data.lifo;

public interface Stack<T> {
	
	/**
	 * Add an item on top of the stack
	 */
	void push(T item);
	
	/**
	 * Test a stack to be empty
	 */
	boolean isEmpty();
	
	/**
	 * Return the item at the top of the stack
	 */
	T top();

	/**
	 * Pop the top of the stack
	 */
	void pop();
	
}
