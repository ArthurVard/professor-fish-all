package de.uniko.softlang.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.SiteVersion;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;
import de.uniko.softlang.crawler.fetcher.Differ;
import de.uniko.softlang.utils.Pair;
import de.uniko.softlang.utils.PairWritable;


public class TestDiffer {
	TreeMap<Integer, SiteVersion> oldData;
	String content;
	
	@Before
	public void setUp(){
		content = "DDDDDDDDDDD";
		oldData = new TreeMap<Integer, SiteVersion>();
		Text urlA = new Text("http://www.a.com");
		DeltaLink[] links1 = {new DeltaLink(new Text("http://www.b.com"), new BooleanWritable(true), new Text("site B"))};
		oldData.put(0, new SiteVersion(new SiteMetadata(urlA,0, links1),new SiteContent(urlA, 0, new DocumentLevelDelta("AAAAAAAAAA"))));
		
		DeltaLink[] links2 = {new DeltaLink(new Text("http://www.c.com"), new BooleanWritable(true), new Text("site C"))};
		oldData.put(1, new SiteVersion(new SiteMetadata(urlA,1, links2),new SiteContent(urlA, 1, new DocumentLevelDelta("BBBBBBBBBB"))));

		DeltaLink[] links3 = {new DeltaLink(new Text("http://www.d.com"), new BooleanWritable(true), new Text("site D")), new DeltaLink(new Text("http://www.b.com"), new BooleanWritable(false), new Text("site B"))};
		oldData.put(2, new SiteVersion(new SiteMetadata(urlA,2, links3),new SiteContent(urlA, 2, new DocumentLevelDelta("CCCCCCCCCC"))));
	}
	
	@Test
	public void TestCreateContentDiff(){
		DocumentLevelDelta result = (DocumentLevelDelta) Differ.createContentDiff(content, oldData, DeltaType.DOC_LVL);
		assertTrue(result.getContent().toString().equals(content.toString()));
		Text urlA = new Text("http://www.a.com");
		
		//insert 'content' into oldData -> now the delta should become emtpy
		oldData.put(3, new SiteVersion(new SiteMetadata(urlA,3, new DeltaLink[0]),new SiteContent(urlA, 3, new DocumentLevelDelta(content))));
		result = (DocumentLevelDelta) Differ.createContentDiff(content, oldData, DeltaType.DOC_LVL);
		assertTrue(result.getContent().toString().equals(""));
		
		oldData = new TreeMap<Integer, SiteVersion>();
		result = (DocumentLevelDelta) Differ.createContentDiff(content, oldData, DeltaType.DOC_LVL);
		assertTrue(result.getContent().toString().equals(content.toString()));
	}
	
	@Test
	public void testCreateLinkDiff(){
		List<Pair<String, String>> links = new LinkedList<Pair<String,String>>();
		links.add(new Pair("http://www.c.com", "site C"));
		links.add(new Pair("http://www.d.com", "site D"));
		List<DeltaLink> result = Differ.createLinkDiff(links, oldData);
		assertEquals(0, result.size());
		
		DeltaLink eSite = new DeltaLink(new Text("http://www.e.com"), new BooleanWritable(true), new Text("site E"));
		links.add(new Pair(eSite.getUrl().toString(), eSite.getAnchor().toString()));
		result = Differ.createLinkDiff(links, oldData);
		
		assertEquals(eSite.getUrl().toString(), result.get(0).getUrl().toString());
		assertEquals(eSite.getAnchor().toString(), result.get(0).getAnchor().toString());
		assertEquals(eSite.getSign().get(), result.get(0).getSign().get());
		
		
		
	}
	//public static List<DeltaLink> createLinkDiff(List<Pair<String,String>> links, TreeMap<Integer, PairWritable<VersionedSiteData,SiteContent>> oldData) {
		
}
