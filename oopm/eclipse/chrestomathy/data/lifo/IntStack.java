// (C) 2009 Ralf Laemmel

package data.lifo;

public interface IntStack {
	
	/**
	 * Add an item on top of the stack
	 */
	void push(int item);
	
	/**
	 * Test a stack to be empty
	 */
	boolean isEmpty();
	
	/**
	 * Return the item at the top of the stack
	 */
	int top();

	/**
	 * Pop the top of the stack
	 */
	void pop();
	
}
