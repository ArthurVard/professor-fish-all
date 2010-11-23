package algorithm.div;

public class Plain {

	public static void main(String[] args) {

		// Variable declarations and test data
		int x = 6;
		int y = 3;
		int q;
		int r;

		// The actual program (algorithm)
		q = 0;
		r = x;
		while (r >= y) {
			r = r - y;
			q = q + 1;
		}

		// Print the result
		System.out.println(x + " = " + y + " * " + q + " + " + r);
	}
}
