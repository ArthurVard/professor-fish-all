// (C) 2008 Ralf Laemmel

package data.range;

/**
 * A data type for ranges of float. Upon construction a min/max range is
 * established. Clearly, it is required that min<=max as the invariant. There
 * are setters for the min/max components that preserve the invariant. Invariant
 * and assertion checking relies on Java's assert. Hence, one must turn assert
 * to get enforcement.
 */
public class FloatRange {
	private Float min, max;

	/**
	 * The invariant of this class
	 */
	private void invariant() {
		assert this.min <= this.max;
	}

	/**
	 * Constructor that establishes the invariant
	 */
	public FloatRange(Float min, Float max) {

		// Precondition
		assert min < max;

		this.min = min;
		this.max = max;

		// Postcondition
		assert getMin() == min && getMax() == max;

		invariant();
	}

	/**
	 * Getter for min
	 */
	public Float getMin() {
		// invariant(); // Not needed!
		return min;
	}

	/**
	 * Setter for min
	 */
	public void setMin(Float min) {
		// invariant(); // Not needed!

		// Precondition
		assert min <= getMax();

		this.min = min;

		invariant();
		// Postcondition
		assert getMin() == min;
	}

	/**
	 * Getter for max
	 */
	public Float getMax() {
		// invariant(); // Not needed!
		return max;
	}

	/**
	 * Setter for max
	 */
	public void setMax(Float max) {
		// invariant(); // Not needed!
		// Precondition
		assert getMin() <= max;

		this.max = max;

		invariant();
		// Postcondition
		assert getMax() == max;
	}
}
