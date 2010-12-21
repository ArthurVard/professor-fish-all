// (C) 2010, Ralf Laemmel

package peano;

public class Demo {
	public static int toInt(Nat n) {
		int result = 0;
		while (!n.isZero()) {
			n = n.pred();
			result++;
		}
		return result;
	}	
	public static void main(String[] args) {
		Nat c0 = new Zero();
		Nat c1 = c0.succ();
		Nat c2 = c1.succ();
		Nat c3 = c2.succ();
		Nat c4 = c3.succ();
		Nat c5 = c4.succ();
		System.out.println("5! = " + Math.factorial(c5));
		System.out.println("5! = " + toInt(Math.factorial(c5)));
	}
}
