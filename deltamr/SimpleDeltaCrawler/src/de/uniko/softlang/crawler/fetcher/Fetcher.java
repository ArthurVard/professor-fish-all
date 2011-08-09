package de.uniko.softlang.crawler.fetcher;

import org.apache.hadoop.fs.Path;

public interface Fetcher {
	public static final String FETCH_TIME = "uniko.fetch.time";
	public static final String DATA_DIR = "sitedata";
	public static final String CONTENT_DIR = "content";
	public static final String META_DIR = "metadata";
	public static final String LINK_DIR = "links";
	public static final String DELTA_LVL = "uniko.delta.level";
	
	public Path fetch(Path segmentBase) throws Exception; 
}
