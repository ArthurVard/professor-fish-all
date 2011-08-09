package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

/**
 * Empty data store to represent a simple outlink, that was found during
 * fetching. This data structure is used during updating of the
 * <code>CrawlDb</code>, to include new found links into the
 * <code>search-frontier</code>.
 * 
 */
public class SimpleLink extends VersionedSiteData {

	public SimpleLink() {
	}

	public SimpleLink(Text url) {
		super(url, -1);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// super.write(out);
		url.write(out);
		out.writeInt(version);
		// --
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// super.readFields(in);
		url = new Text();
		url.readFields(in);
		version = in.readInt();
		// --
	}
}
