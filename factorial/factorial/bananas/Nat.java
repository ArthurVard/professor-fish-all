// (C) 2010, Ralf Laemmel

package bananas;

public interface Nat {
	boolean isZero();
	Nat pred();
	Nat succ();
	<R> R cata(Cata<R> c);
	<R> R para(Para<R> p);
}
