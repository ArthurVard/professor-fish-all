package de.uniko.softlang.crawler.fetcher;

import java.util.HashSet;

public class StopWords {
	private static String[] words = { "a", "able", "about", "across", "after",
			"all", "almost", "also", "am", "among", "an", "and", "any", "are", "as",
			"at", "be", "because", "been", "but", "by", "can", "cannot", "could",
			"dear", "did", "do", "does", "either", "else", "ever", "every", "for",
			"from", "get", "got", "had", "has", "have", "he", "her", "hers", "him",
			"his", "how", "however", "i", "if", "in", "into", "is", "it", "its",
			"just", "least", "let", "like", "likely", "may", "me", "might", "most",
			"must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on",
			"only", "or", "other", "our", "own", "rather", "said", "say", "says",
			"she", "should", "since", "so", "some", "than", "that", "the", "their",
			"them", "then", "there", "these", "they", "this", "tis", "to", "too",
			"twas", "us", "wants", "was", "we", "were", "what", "when", "where",
			"which", "while", "who", "whom", "why", "will", "with", "would", "yet",
			"you", "your" };
	private final HashSet<String> stopWords;
	private static StopWords theInstance;

	private StopWords() {
		stopWords = new HashSet<String>(words.length);
		for (int i = 0; i < words.length; i++) {
			stopWords.add(words[i]);
		}
	}

	public static StopWords getInstance() {
		if (theInstance == null)
			theInstance = new StopWords();
		return theInstance;
	}

	public boolean contains(String word) {
		return stopWords.contains(word);
	}

}
