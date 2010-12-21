// (C) 2010, Ralf Laemmel

package peano;

public interface Nat {
	boolean isZero();
	Nat pred();
	Nat succ();
}
