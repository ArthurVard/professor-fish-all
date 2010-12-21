// (C) 2010, Ralf Laemmel

package bananas;

public interface Cata<R> {
	R ifZero();
	R ifNotZero(R r);
}
