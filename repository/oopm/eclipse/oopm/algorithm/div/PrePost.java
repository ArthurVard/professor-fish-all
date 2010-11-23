package algorithm.div;

// A variation on Plain.java -- with pre- and postconditions
public class PrePost {

	public static void main(String[] args) {

		// Variable declarations and test data
		int x = 6;
		int y = 3;
		int q;
		int r;

		// Precondition
		assert x >= 0 && y > 0;
		
		// The actual program (algorithm)
		q = 0;
		r = x;
		while (r >= y) {
			r = r - y;
			q = q + 1;
		}

		// Postcondition
		assert x == y * q + r  
		    && r < y 
		    && r >= 0 && q >= 0;
		
		// Print the result
		System.out.println(x + " = " + y + " * " + q + " + " + r);
	}
}
