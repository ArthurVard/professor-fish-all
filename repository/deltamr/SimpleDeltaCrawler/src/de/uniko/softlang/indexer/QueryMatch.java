package de.uniko.softlang.indexer;

import java.util.Map;

import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.utils.MutableInt;

/**
 * Represents a candidate (stored as a <code>DocumentMapEntry</code>) along with
 * the number of occurrences for each given term of the search query.
 */
public class QueryMatch {

	DocumentMapEntry entry;
	OccurrenceMap<String> wordOcc;

	public QueryMatch(DocumentMapEntry entry) {
		this.entry = entry;
		wordOcc = new OccurrenceMap<String>();
	}

	public void update(String word, MutableInt count) {
		MutableInt currCount = wordOcc.get(word);
		if (currCount == null) {
			wordOcc.put(word, count);
		} else {
			currCount.incBy(count.get());
		}
	}

	public DocumentMapEntry getEntry() {
		return entry;
	}

	public void setEntry(DocumentMapEntry entry) {
		this.entry = entry;
	}

	public OccurrenceMap<String> getWordOcc() {
		return wordOcc;
	}

	public void setWordOcc(OccurrenceMap<String> wordOcc) {
		this.wordOcc = wordOcc;
	}

}
