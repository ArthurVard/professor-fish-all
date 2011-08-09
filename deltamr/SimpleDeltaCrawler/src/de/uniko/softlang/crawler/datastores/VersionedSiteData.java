package de.uniko.softlang.crawler.datastores;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * General class to hold data about a version of a website. The version is
 * identified by the <code>url</code> and the <code>version</code>. This class
 * is not intended for direct instantiation, but should be extended by more
 * concrete implementations with additional data fields.
 */
public class VersionedSiteData extends GenericWritable implements
		Comparable<VersionedSiteData> {

	public static final Log LOG = LogFactory.getLog(VersionedSiteData.class);
	private static Class<? extends Writable>[] CLASSES = null;

	static {
		CLASSES = new Class[] {
				de.uniko.softlang.crawler.datastores.VersionedSiteData.class,
				de.uniko.softlang.crawler.datastores.CrawlDbData.class,
				de.uniko.softlang.crawler.datastores.SiteContent.class,
				de.uniko.softlang.crawler.datastores.SiteMetadata.class,
				de.uniko.softlang.crawler.datastores.SimpleLink.class };
	}

	@Override
	protected Class<? extends Writable>[] getTypes() {
		return CLASSES;
	}

	Text url;
	int version;

	public VersionedSiteData() {
		set(this);
	}

	public VersionedSiteData(Writable instance) {
		set(instance);
	}

	public VersionedSiteData(Text url, int version) {
		set(this);
		this.url = url;
		this.version = version;
	}

	@Override
	public int compareTo(VersionedSiteData that) {
		if (this.url.compareTo(that.url) != 0)
			return this.url.compareTo(that.url);
		if (this.version != that.version)
			return (this.version - that.version) > 0 ? 1 : -1;
		return 0;
	}

	public void updateWithDelta(VersionedSiteData currentValue) {
		if (currentValue.version <= this.version) {
			LOG.error("Skipping attempt to update version " + this.version
					+ " with lower version " + currentValue.version + " (" + url + ")");
			return;
		}
		this.version = currentValue.version;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setUrl(Text url) {
		this.url = url;
	}

	public Text getUrl() {
		return url;
	}

	public Text prettyPrint() {
		String outString = "URL = " + url + ", Version = " + version;
		return new Text(outString);
	}

}
