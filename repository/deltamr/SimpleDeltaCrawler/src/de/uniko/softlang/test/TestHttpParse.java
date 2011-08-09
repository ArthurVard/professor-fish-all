package de.uniko.softlang.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

import de.uniko.softlang.crawler.fetcher.HttpParser;
import de.uniko.softlang.crawler.fetcher.SimpleHttpParser;
import de.uniko.softlang.utils.Pair;

public class TestHttpParse {

	private static String exampleWebsite = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\"http://www.w3.org/TR/html4/strict.dtd\"><html><head><title>Example for Links</title><style type=\"text/css\" media=\"screen\">a style text to remove</style><script>a java-script to remove</script></head><body><h1><!--comment1-->A small <style> pretty </style> link collection<!--comment2--></h1><p><a href=\"http://localhost/A.html\">CNN</a> News<br><a href=\"http://localhost/B.html\">The register</a> IT-News<br><a class=\"unselected\" href=\"http://hadoop.apache.org/mapreduce/\">MapReduce</a></p></body></html>";
	private static String[] exampleLinks = { "http://localhost/A.html",
			"http://localhost/B.html", "http://hadoop.apache.org/mapreduce/" };
	private static String[] exampleAnchors = { "CNN", "The register", "MapReduce" };
	private static String exampleText = "In computing, stop words are words which are filtered out prior to, or after, processing of natural language data (text). It is controlled by human input and not automated. There is not one definite list of stop words which all tools use, if even used. Some tools specifically avoid using them to support phrase search. Any group of words can be chosen as the stop words for a given purpose. For some search machines, these are some of the most common, short function words, such as the, is, at, which and on. In this case, stop words can cause problems when searching for phrases that include them, particularly in names such as 'The Who', 'The The', or 'Take That'. Other search engines remove some of the most common words—including lexical words, such as \"want\"—from query in order to improve performance.[1] Hans Peter Luhn, one of the pioneers in information retrieval, is credited with coining the phrase and using the concept in his design.";
	
	@Test
	public void testParseLinks() {
		HttpParser parser = new SimpleHttpParser();
		List<Pair<String, String>> links = parser.parseLinks(exampleWebsite);
		assertEquals(exampleLinks.length, links.size());
		for (int i = 0; i < links.size(); i++) {
			assertEquals(links.get(i).getFirst(), exampleLinks[i]);
			assertEquals(links.get(i).getSecond(), exampleAnchors[i]);
		}
	}

	@Test
	public void testParseContent() {
		HttpParser parser = new SimpleHttpParser();
		String content = parser.removeHtmlElements(exampleWebsite);
		String expectedResult = "Example for Links A small link collection CNN News The register IT-News MapReduce";
		StringTokenizer tokResult = new StringTokenizer(content);
		StringTokenizer tokExpected = new StringTokenizer(expectedResult);
		while(tokResult.hasMoreTokens() && tokExpected.hasMoreElements()){
			assertEquals(tokExpected.nextToken(),tokResult.nextToken());
		}
		assertFalse(tokResult.hasMoreElements());
		assertFalse(tokExpected.hasMoreElements());
		while(tokResult.hasMoreElements()){
			System.out.println("No match in expected: " + tokResult.nextElement());
		}
		while(tokExpected.hasMoreElements()){
			System.out.println("No match in result: " + tokExpected.nextElement());
		}
		assertFalse(tokExpected.hasMoreTokens());
		
	}

	@Test
	public void testRemoveStopWords() {
		HttpParser parser = new SimpleHttpParser();
		String content = parser.cleanText(exampleText);
		String expectedResult = "computing stop words words filtered out prior processing natural language data text controlled human input automated one definite list stop words tools use even used tools specifically avoid using support phrase search group words chosen stop words given purpose search machines common short function words such case stop words cause problems searching phrases include particularly names such take search engines remove common words including lexical words such want query order improve performance hans peter luhn one pioneers information retrieval credited coining phrase using concept design";
		StringTokenizer tokResult = new StringTokenizer(content);
		StringTokenizer tokExpected = new StringTokenizer(expectedResult);
		while(tokResult.hasMoreTokens()){
			assertEquals(tokExpected.nextToken(),tokResult.nextToken());
		}
		assertFalse(tokExpected.hasMoreTokens());
		assertEquals(expectedResult, content.trim());
	}
}
