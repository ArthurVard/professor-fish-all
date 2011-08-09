package de.uniko.softlang.crawler.fetcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uniko.softlang.utils.Pair;

public class SimpleHttpParser implements HttpParser{
	private HashSet<String> validFileTypes = new HashSet<String>();
	private static String[] basicEndings = {"htm", "html"};
	
	public SimpleHttpParser(){
		this(basicEndings);
	}
		
	public SimpleHttpParser(String[] validEndings){
		this.validFileTypes = new HashSet<String>();
		for (int i = 0; i < validEndings.length; i++) {
			this.validFileTypes.add(validEndings[i]);
		}
	}
	
	/**
	 * Returns only absolute links.
	 * @param content
	 * @return
	 */
	@Override
	public List<Pair<String,String>> parseLinks(String content){
		List<Pair<String,String> >candidates = new ArrayList<Pair<String,String>>();
		Pattern p = Pattern.compile("href=\"http[^>]*\">[^<]*</a>");
//		Pattern p = Pattern.compile("href=\"[^>]*\">");
		Matcher m = p.matcher(content);
		int start = 0;
		boolean found = true;
		while(found){
			found = m.find(start);
			if(found){
				start = m.end();
				String candidate = m.group().replaceFirst("href=\"", "");
				int urlEnd = candidate.indexOf("\">");
				String candidateUrl = candidate.substring(0, urlEnd);
				int anchorEnd = candidate.indexOf("</a>");
				String candidateAnchor = candidate.substring(urlEnd+2, anchorEnd);
				
				try {
					URL u = new URL(candidateUrl);
					if(validEnding(candidateUrl.trim()))
						candidates.add(new Pair<String,String>(candidateUrl.trim(), candidateAnchor.trim()));
				} catch (MalformedURLException e) {
					//no valid URL -> skip it
				}
				
			}
		}
		return candidates;
	}
	
	private boolean validEnding(String url) {
		if(url.endsWith("/"))
			return true;
		int indexOfDot = url.lastIndexOf('.');
		if(indexOfDot >= 0 && indexOfDot < url.length()-1){
			String suffix = url.substring(url.lastIndexOf('.')+1);
			if(validFileTypes.contains(suffix))
				return true;
		}
		return false;
	}

	@Override
	public String removeHtmlElements(String content){
		content = removeHtmlComments(content);
		content = removeFormatInfo(content);
		content = removeScripts(content);
		content = removeHtmlTags(content);
		content = removePredefinedWords(content);
		return content.trim();
	}

	private String removeHtmlComments(String content) {
		//return content.replaceAll("<!--(.*?)-->", " ").trim();
		return Pattern.compile("<!--(.*?)-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(content).replaceAll(" ");
	}

	private String removeFormatInfo(String content) {
		//return content.replaceAll("<style(.*?)</style>", " ").trim();
		return Pattern.compile("<style(.*?)</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(content).replaceAll(" ");
	}

	private String removeScripts(String content) {
		//return content.replaceAll("<script(.*?)</script>", " ").trim();
		return Pattern.compile("<script(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(content).replaceAll(" ");
	}
	
	private String removePredefinedWords(String content) {
		//return content.replaceAll("<script(.*?)</script>", " ").trim();
		return Pattern.compile("&[^;]*;").matcher(content).replaceAll(" ");
	}

	private String removeHtmlTags(String content) {
		String oldContent = "";
		String newContent = content;
		while(newContent != oldContent){
			oldContent = newContent;
			newContent = newContent.replaceAll("<[^>]*>", " ");
			newContent = newContent.replaceAll("</[^>]*>", " ");
		}
		return newContent.trim();
	}

	@Override
	public String cleanText(String content) {
		StopWords stopWords = StopWords.getInstance();
		content = content.replaceAll("[^a-zA-Z ]", " ");
		StringBuilder builder = new StringBuilder(content.length()/2);
		StringTokenizer tok = new StringTokenizer(content);
		while(tok.hasMoreTokens()){
			String next = tok.nextToken();
			if(!stopWords.contains(next.toLowerCase()))
				builder.append(" " + next.toLowerCase());
		}
		return builder.toString().trim();
	}
}
