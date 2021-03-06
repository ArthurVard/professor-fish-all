/**
 * @author Ralf Lämmel
 * Functionality for GCD algorithm
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
	
	public static void main(String[] args) {		
		System.out.println(gcd(6,5)); // 1
		System.out.println(gcd(6,4)); // 2
		System.out.println(gcd(9,6)); // 3
		System.out.println(gcd(12,8)); // 4
	}
}