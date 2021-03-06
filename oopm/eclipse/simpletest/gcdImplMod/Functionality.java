/**
 * @author Ralf Lämmel
 * Functionality for GCD algorithm
 */
public class Functionality {

	/**
	 * Compute greatest common denominator.
	 * Use modulo arithmetics.
	 * Assume positive operands.
	 */
	public static int gcd(int x, int y) {
		while (true) { 
			if (x < y) {
				// swap x and y
				int t = x;
				x = y;
				y = t; 
			}
		    int r = x % y;
		    if (r == 0)
		    	return y;
		    else
		    	x = r;
	    }
	}	
}