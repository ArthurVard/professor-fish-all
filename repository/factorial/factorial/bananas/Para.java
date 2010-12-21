// (C) 2010, Ralf Laemmel

package bananas;

public interface Para<R> {
	R ifZero();
	R ifNotZero(Nat pred, R r);
}
