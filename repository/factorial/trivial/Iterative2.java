// (C) 2010, Ralf Laemmel

package trivial;

public class Iterative2 {
	public static int factorial(int n) {
		int result = 1; 
		for (; n>1; n--) result *= n;
		return result;
	}
	public static void main(String[] args) {
		System.out.println("5! = " + factorial(5));
	}
}
