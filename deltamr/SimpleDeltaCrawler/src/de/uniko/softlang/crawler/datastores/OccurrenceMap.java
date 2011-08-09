package de.uniko.softlang.crawler.datastores;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uniko.softlang.utils.MutableInt;

/**
 * An facade for a <code>HashMap</code>, that basically realizes a Bag-type, and
 * allows update of the count for an element with single access to the
 * underlying <code>HashMap</code>.
 * 
 * @param <T>
 *          the type of the objects to be counted-
 */
public class OccurrenceMap<T> extends HashMap<T, MutableInt> {

	public OccurrenceMap() {
		super();
	}

	public OccurrenceMap(int initialCapacity) {
		super(initialCapacity);
	}

	public int put(T elem) {
		return put(elem, 1);
	}

	public int put(T elem, int occurences) {
		MutableInt currentCount = get(elem);
		if (currentCount == null) {
			currentCount = new MutableInt(0);
			put(elem, currentCount);
		}
		currentCount.incBy(occurences);
		if (currentCount.get() == 0) {
			remove(elem);
		}
		return currentCount.get();
	}

}
