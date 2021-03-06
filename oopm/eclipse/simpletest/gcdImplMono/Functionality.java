import static org.junit.Assert.*;

/**
 * @author Ralf Lämmel
 * Functionality for GCD algorithm
 * Monolithic approach (functionality and tests in one class)
 */
public class Functionality {

	/**
	 * Compute greatest common denominator.
	 * Use successive subtraction.
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
	
	/**
	 * Assert-based tests
	 */
	public static void main(String[] args) {		
		assertEquals(1, gcd(6,5));
		assertEquals(2, gcd(6,4));
		assertEquals(3, gcd(9,6));
		assertEquals(4, gcd(12,8));
	}
}