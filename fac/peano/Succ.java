// (C) 2010, Ralf Laemmel

package peano;

public class Succ implements Nat {
	private Nat pred;
	public Succ(Nat pred) { this.pred = pred; }
	public boolean isZero() { return false; }
	public Nat pred() { return pred; }
	public Nat succ() { return new Succ(this); }
}
