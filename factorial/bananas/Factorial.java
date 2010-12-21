// (C) 2010, Ralf Laemmel

package bananas;

public class Factorial implements Para<Nat> {
	public Nat ifZero() { return new Zero().succ(); }
	public Nat ifNotZero(Nat pred, Nat r) {
		return Math.times(pred.succ(),r); 
	}
}
