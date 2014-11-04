// (C) 2009 Ralf Laemmel

/**
 * Fast exponentiation; exponentiation by squaring
 * http://en.wikipedia.org/wiki/Exponentiation_by_squaring
 */

package algorithm.power;

/**
 * This variation modifies argument variables.
 */
public class VariationOfEfficient {

	public static int power(int x, int n) {
		int y = 1;
		while (n>0) 
			if (n % 2 == 0) {
				x = x * x;
				n = n / 2;
			}
			else {
				y = y * x;
				n = n - 1;
			}
		return y;
	}
	
	public static void main(String[] args) {
		System.out.println(power(2,1)); // prints 2
		System.out.println(power(2,2)); // prints 4
		System.out.println(power(2,3)); // prints 8
		System.out.println(power(2,4)); // prints 16
		System.out.println(power(3,1)); // prints 3
		System.out.println(power(3,2)); // prints 9
		System.out.println(power(3,3)); // prints 27
		System.out.println(power(3,4)); // prints 81
	}

}
