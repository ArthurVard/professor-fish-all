// (C) 2009--2014 Ralf Laemmel

package algorithm.sqrt;

/**
 * 
 * Compute square root based on divide-and-average method.
 * See class DivideAndAverage.
 * We add assertions to represent some knowledge about the algorithm's internals.
 * 
 */
public class AssertedDivideAndAverage {

	public static double sqrt(double x, double epsilon) {
		if (x < 0)
			return Double.NaN;
		assert x >= 0; // we just established that
		double r = x;
		while (Math.abs(x - r * r) > epsilon) {
			double olddelta = Math.abs(x - r * r);
			assert olddelta > epsilon; // we just tested the condition
			r = (x / r + r) / 2.0;
			double newdelta = Math.abs(x - r * r);
			assert newdelta < olddelta; // we converge
		}
		assert Math.abs(x - r * r) <= epsilon; // the while loop is finished
		return r;
	}

	public static void main(String[] args) {
		System.out.println("sqrt(9) = " + sqrt(9, 1e-14));
	}
}
