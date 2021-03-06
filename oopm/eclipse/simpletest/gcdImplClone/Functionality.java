/**
 * @author Iam TheCloner
 * Functionality for GCD algorithm
 */
public class Functionality {

	/**
	 * Compute greatest common denominator.
	 * THE VERY CREATIVE APPROACH.
	 * Assume positive operands.
	 */
	public static int gcd(int x, int y) {
		while (x != y) {
			if (x > y)
				x = x - y;
			else
				y = y - x;
		}
		return x;
	}
}