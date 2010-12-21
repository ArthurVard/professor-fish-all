// (C) 2010, Ralf Laemmel

package peano;

public class Math {
	public static Nat add(Nat n, Nat m) {
		return (n.isZero()) ?
				  m
				: add(n.pred(),m).succ();
	}
	public static Nat times(Nat n, Nat m) {
		return (n.isZero()) ?
				  new Zero()
				: add(m,times(n.pred(),m));
	}
	public static Nat factorial(Nat n) {
		return (n.isZero()) ? 
				  new Succ(new Zero())
				: times(n,factorial(n.pred()));
	}
}
