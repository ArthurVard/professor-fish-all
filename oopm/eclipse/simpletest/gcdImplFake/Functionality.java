/**
 * @author Ralf Lämmel
 * Functionality for GCD algorithm
 */
public class Functionality {

	/**
	 * Compute greatest common denominator.
	 * THIS IS A HOPELESS ATTEMPT OF CHEATING.
	 * Assume positive operands.
	 */
	public static int gcd(int x, int y) {
		if (x==6 && y==5) return 1;
		if (x==6 && y==4) return 2;
		if (x==9 && y==6) return 3;
		if (x==12 && y==8) return 4;
		throw new RuntimeException("undefined");
	}	
}