// (C) 2010, Ralf Laemmel

package bananas;

public class Add implements Cata<Nat> {
	private Nat m;
	public Add(Nat m) { this.m = m; }
	public Nat ifZero() { return m; }
	public Nat ifNotZero(Nat r) { return r.succ(); }
}
