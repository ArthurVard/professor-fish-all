// (C) 2010, Ralf Laemmel

package peano;

public class Zero implements Nat {
	public boolean isZero() { return true; }
	public Nat pred() { throw new AssertionError(); }
	public Nat succ() { return new Succ(this); }
}
