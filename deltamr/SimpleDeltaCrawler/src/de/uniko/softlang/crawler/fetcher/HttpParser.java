package de.uniko.softlang.crawler.fetcher;

import java.util.List;

import de.uniko.softlang.utils.Pair;

public interface HttpParser {

	/**
	 * Parse a website's content String and return all links together with their anchors.
	 * @param content The website's content. 
	 * @return A {@link List} of urls together with their corresponding anchors.
	 */
	public List<Pair<String,String>> parseLinks(String content);
	
	/**
	 * Remove all HTML-tags from a given website content.
	 * @param content The content to remove tags from 
	 * @return The 'cleaned' content
	 */
	public String removeHtmlElements(String content);
	
	/**
	 * Remove all alpha-numeric characters and stop-words from a given website content.
	 * @param content The content to remove characters and stop words from
	 * @return The 'cleaned' content
	 */
	public String cleanText(String content);
}
