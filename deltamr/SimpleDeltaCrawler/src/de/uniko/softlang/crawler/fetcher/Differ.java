package de.uniko.softlang.crawler.fetcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;

import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.SiteVersion;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.delta.WordLevelDelta;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;
import de.uniko.softlang.utils.Pair;
import de.uniko.softlang.utils.PairWritable;

public class Differ {

	public static final Log LOG = LogFactory.getLog(Differ.class);

	public static ContentDelta createContentDiff(String content,
			TreeMap<Integer, SiteVersion> oldData, DeltaType deltaLvl) {
		if (deltaLvl == DeltaType.DOC_LVL)
			return createDocLvlDelta(content, oldData);
		else
			return createWordLvlDelta(content, oldData);
	}

	public static ContentDelta createDocLvlDelta(String content,
			TreeMap<Integer, SiteVersion> oldData) {
		if (oldData.size() == 0) {
			return new DocumentLevelDelta(new Text(content));
		}
		String lastContent = extractLatestContent(oldData, DeltaType.DOC_LVL);
		if (content.trim().equals(lastContent.trim())) {
			return new DocumentLevelDelta(new Text(""));
		} else {
			// System.out.println(content.trim());
			// System.out.println("----------");
			// System.out.println(lastContent.trim());
			return new DocumentLevelDelta(new Text(content));
		}
	}

	/**
	 * Use multiple deltas of a websites content to extract the latest version of
	 * the content. This in awareness of the granularity of the delta (i.e. word
	 * or document level).
	 * 
	 * @param oldData
	 *          All old versions for a specific site.
	 * @param deltaLevel
	 *          The level of the delta, either <code>DeltaType.DOC_LVL</code> or
	 *          <code>DeltaType.WORD_LVL</code>.
	 * @return
	 */
	public static String extractLatestContent(
			TreeMap<Integer, SiteVersion> oldData, DeltaType deltaLevel) {
		if (deltaLevel == DeltaType.DOC_LVL) {
			String retVal = "";
			Integer lastKey = oldData.lastKey();
			while (lastKey != null && (retVal == null || retVal.equals(""))) {
				SiteVersion currentVersion = oldData.get(lastKey);
				if (currentVersion == null) {
					LOG.error("Unexpected missing old-data 1!");
					System.out.println("Unexpected missing old-data 1!");
				} else {
					SiteContent content = currentVersion.getContent();
					if (content == null) {
						LOG.error("Unexpected missing old-data 2!");
						System.out.println("Unexpected missing old-data 2!");
					} else {
						DocumentLevelDelta delta = (DocumentLevelDelta) content.getDelta();
						if (delta == null) {
							LOG.error("Unexpected missing old-data 3!");
							System.out.println("Unexpected missing old-data 3!");
						} else {
							retVal = delta.getAsString();
						}
					}
				}
				lastKey = oldData.lowerKey(lastKey);
			}
			return retVal;
		} else {
			WordLevelDelta retVal = new WordLevelDelta();
			for (SiteVersion p : oldData.values()) {
				if (p.getContent() != null)
					retVal.apply(p.getContent().getDelta());
			}
			return retVal.getAsString();
		}
	}

	private static ContentDelta createWordLvlDelta(String content,
			TreeMap<Integer, SiteVersion> oldData) {
		if (oldData.size() == 0) {
			return new WordLevelDelta(content);
		}
		WordLevelDelta retVal = new WordLevelDelta();
		for (SiteVersion p : oldData.values()) {
			if (p.getContent() != null)
				retVal.apply(p.getContent().getDelta());
		}
		retVal.invert(); // all old counts need to be negated, as they are 'removed'
		retVal.apply(content);
		return retVal;
	}

	public static List<DeltaLink> createLinkDiff(
			List<Pair<String, String>> links, TreeMap<Integer, SiteVersion> oldData) {
		// calculate totals of all old version
		Map<Pair<String, String>, Integer> totalLinks = extractLatestLinks(oldData);

		// update with current version
		for (Pair<String, String> l : links) {
			if (!totalLinks.containsKey(l)) {
				totalLinks.put(l, 0);
			}
			totalLinks.put(l, totalLinks.get(l) + 1);
		}
		// extract changes
		List<DeltaLink> retVal = new LinkedList<DeltaLink>();
		for (Pair<String, String> t : totalLinks.keySet()) {
			int count = totalLinks.get(t);
			// multiple occurences of the same link are only saved once
			if (count > 0) {
				retVal.add(new DeltaLink(new Text(t.getFirst()), new BooleanWritable(
						true), new Text(t.getSecond())));
			} else if (count < 0) {
				retVal.add(new DeltaLink(new Text(t.getFirst()), new BooleanWritable(
						false), new Text(t.getSecond())));
			} else {
				// canceled out
			}
		}

		return retVal;
	}

	/**
	 * Use multiple deltas to extract the latest state of a sites outlinks.
	 * @param oldData A <code>Map</code> of multiple deltas of a website. 
	 * @return Current set of outlink.
	 */
	public static Map<Pair<String, String>, Integer> extractLatestLinks(
			Map<Integer, SiteVersion> oldData) {
		Map<Pair<String, String>, Integer> totalLinks = new HashMap<Pair<String, String>, Integer>();
		for (SiteVersion v : oldData.values()) {
			DeltaLink[] linkPairs = ((SiteMetadata) v.getMeta()).getLinks();
			for (int i = 0; i < linkPairs.length; i++) {
				String link = linkPairs[i].getUrl().toString();
				String anchor = linkPairs[i].getAnchor().toString();
				Pair<String, String> pair = new Pair<String, String>(link, anchor);
				if (!totalLinks.containsKey(pair)) {
					totalLinks.put(pair, 0);
				}
				int operand = linkPairs[i].getSign().get() ? -1 : 1; // has to be
																															// inverted
				totalLinks.put(pair, totalLinks.get(pair) + operand);
			}
		}
		return totalLinks;
	}

}
