//done
package de.uniko.softlang.utils;

/**
 * This class is used to build a 2-tuple for any kind of objects
 * 
 * @param <A>
 *            Object of type A
 * @param <B>
 *            Object of type B
 */
public class Pair<A, B> {
	private A first;
	private B second;

	public Pair() {
		super();
	}

	/**
	 * Creates a 2-tuple for two objects
	 * 
	 * @param first
	 *            Object of type A
	 * @param second
	 *            Object of type B
	 */
	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		if (other instanceof Pair<?, ?>) {
			Pair<A, B> otherPair = (Pair<A, B>) other;

			return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first
					.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
					&& otherPair.second != null && this.second.equals(otherPair.second))));
		}

		return false;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}
}
