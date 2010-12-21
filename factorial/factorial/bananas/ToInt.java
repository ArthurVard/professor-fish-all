// (C) 2010, Ralf Laemmel

package bananas;

public class ToInt implements Cata<Integer> {
	public Integer ifZero() { return 0; }
	public Integer ifNotZero(Integer r) { return r+1; }
}
