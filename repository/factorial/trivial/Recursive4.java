// (C) 2010, Ralf Laemmel

package trivial;

public class Recursive4 {
	public static int factorial(int n) {
		return (n==0) ? 
				  1
				: n * factorial(n-1);
	}
	public static void main(String[] args) {
		System.out.println("5! = " + factorial(5));
	}
}
