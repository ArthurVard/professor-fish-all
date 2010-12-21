// (C) 2010, Ralf Laemmel

package bananas;

public class Math {
	public static Nat add(Nat n, Nat m) {
		return n.cata(new Add(m));
	}
	public static Nat times(Nat n, Nat m) {
		return n.cata(new Times(m));
	}	
	public static Nat factorial(Nat n) {
		return n.para(new Factorial());
	}		
	public static int toInt(Nat n) {
		return n.cata(new ToInt());
	}
}
