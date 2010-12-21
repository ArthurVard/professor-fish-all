// (C) 2010, Ralf Laemmel

package oo;

public class Math {
	public static Int add(Int n, Int m) {
		return (n.isZero()) ?
				  m
				: add(n.pred(),m).succ();
	}
	public static Int times(Int n, Int m) {
		return (n.isZero()) ?
				  new Int(0)
				: add(m,times(n.pred(),m));
	}
	public static Int factorial(Int n) {
		return (n.isZero()) ? 
				  new Int(1)
				: times(n,factorial(n.pred()));
	}
}
