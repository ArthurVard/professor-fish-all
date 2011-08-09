package de.uniko.softlang.test;

import org.apache.hadoop.fs.Path;

import de.uniko.softlang.indexer.Indexer;


public class TestIndexing {
	public static void main(String[] args) throws Exception{
		Indexer i = new Indexer();
		Path[] segments = {

				new Path("testBig/segments/sitedata/1306914553784"),
				new Path("testBig/segments/sitedata/1306914568775"),
				new Path("testBig/segments/sitedata/1306914599005")	
		};
						
						

		Path indexDir = new Path("testBig/index");
		i.indexWithClusterRecompution(segments, indexDir, null);
		
	}
}
