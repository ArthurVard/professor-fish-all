// (C) 2010, Ralf Laemmel

package bananas;

public class Zero implements Nat {
	public boolean isZero() { return true; }
	public Nat pred() { throw new AssertionError(); }
	public Nat succ() { return new Succ(this); }
	public <R> R cata(Cata<R> c) {
		return c.ifZero();
	}	
	public <R> R para(Para<R> p) {
		return p.ifZero();
	}	
}
