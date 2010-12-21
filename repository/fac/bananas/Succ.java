// (C) 2010, Ralf Laemmel

package bananas;

public class Succ implements Nat {
	private Nat pred;
	public Succ(Nat pred) { this.pred = pred; }
	public boolean isZero() { return false; }
	public Nat pred() { return pred; }
	public Nat succ() { return new Succ(this); }
	public <R> R cata(Cata<R> c) {
		return c.ifNotZero(pred().cata(c));
	}	
	public <R> R para(Para<R> p) {
		return p.ifNotZero(pred(), pred().para(p));
	}	
}
