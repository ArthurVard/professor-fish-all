// (C) 2010, Ralf Laemmel

package bananas;

public class Times implements Cata<Nat> {
	private Nat m;
	public Times(Nat m) { this.m = m; }
	public Nat ifZero() { return new Zero(); }
	public Nat ifNotZero(Nat r) { return Math.add(m,r); }
}
