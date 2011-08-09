package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.utils.PairWritable;

/**
 * Map that is used for signed aware inlink maintenance for a website, that is a
 * sign is stored along with a URL, where the sign indicates, whether the link
 * was added or deleted. If a link is being added, and the same link with the
 * inverse sign is already present in the Map, the entry for this link is being
 * removed (since <code>add -> delete</code> and <code>delete -> add</code> is a
 * no-op).
 */
public class LinkMap implements Writable {
	private HashSet<DeltaLink> inlinks = new HashSet<DeltaLink>(1);

	public LinkMap() {
	}

	/**
	 * Adds the given link to the {@link Set}. If the same link exists with the
	 * opposite sign, they cancel each other out.
	 * 
	 * @param link
	 */
	public void add(DeltaLink link) {
		DeltaLink contrary = new DeltaLink(link.getUrl(), new BooleanWritable(!link
				.getSign().get()), link.getAnchor());
		if (inlinks.contains(contrary))
			inlinks.remove(contrary);
		else
			inlinks.add(link);
	}

	public int size() {
		return inlinks.size();
	}

	public Iterator<DeltaLink> iterator() {
		return inlinks.iterator();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(inlinks.size());
		Iterator<DeltaLink> it = inlinks.iterator();
		while (it.hasNext()) {
			it.next().write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int length = in.readInt();
		inlinks.clear();
		for (int i = 0; i < length; i++) {
			DeltaLink link = new DeltaLink();
			link.readFields(in);
			inlinks.add(link);
		}
	}

	public Text prettyPrint() {
		String retVal = "";
		for (DeltaLink l : inlinks) {
			retVal = retVal.concat("\n\t" + l.prettyPrint());

		}
		return new Text(retVal);
	}
}
