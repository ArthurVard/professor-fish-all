// (C) 2009 Ralf Laemmel

package data.list.singlylinked;

import java.lang.Iterable;
import java.util.Iterator;

/**
 * A very simple list interface.
 * See java.util collections for the full story.
 */
public interface List<T> extends Iterable<T> {
	void add(T item);
	void remove(T item);
	Iterator<T> iterator();
}
