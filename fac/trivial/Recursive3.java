// (C) 2010, Ralf Laemmel

package trivial;

public class Recursive3 {
	public static int factorial(int n) {
		if (n<2) 
			return 1;
		else 
			return n * factorial(n-1);
	}
	public static void main(String[] args) {
		System.out.println("5! = " + factorial(5));
	}
}
