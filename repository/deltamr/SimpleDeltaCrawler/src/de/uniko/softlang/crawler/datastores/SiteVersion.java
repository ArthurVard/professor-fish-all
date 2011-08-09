package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * Container class, that represents the version of a website by storing a
 * version of its content, as well as a version of its metadata.
 */
public class SiteVersion implements Writable, Comparable<SiteVersion> {
	private VersionedSiteData meta;
	private SiteContent content;

	public SiteVersion() {
	}

	public SiteVersion(VersionedSiteData meta, SiteContent content) {
		this.meta = meta;
		this.content = content;
	}

	public VersionedSiteData getMeta() {
		return meta;
	}

	public void setMeta(VersionedSiteData meta) {
		this.meta = meta;
	}

	public SiteContent getContent() {
		return content;
	}

	public void setContent(SiteContent content) {
		this.content = content;
	}

	@Override
	public int compareTo(SiteVersion that) {
		if (this.meta.compareTo(that.meta) != 0)
			return this.meta.compareTo(that.meta);
		return this.content.compareTo(that.content);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		if (meta instanceof SimpleLink)
			out.writeInt(0);
		else
			out.writeInt(1);
		meta.write(out);
		content.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int metaType = in.readInt();
		if (metaType == 0)
			meta = new SimpleLink();
		else
			meta = new SiteMetadata();
		meta.readFields(in);
		content = new SiteContent();
		content.readFields(in);
	}

}
