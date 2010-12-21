// (C) 2010, Ralf Laemmel

package iterator;

public class Demo {	
	public static int factorial(int n) {
		return Math.product(Math.enumFromTo(1,n));
	}
	public static void main(String[] args) {
		System.out.println("5! = " + factorial(5));
	}
}
