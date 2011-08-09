package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.delta.WordLevelDelta;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;

/**
 * Data structure to store a version of the content of a website. The version
 * can be the full content, or a delta relative to earlier versions.
 * 
 */
public class SiteContent extends VersionedSiteData {

	private ContentDelta delta;

	public SiteContent() {
	}

	public SiteContent(Text url, int version, ContentDelta delta) {
		super(url, version);
		this.delta = delta;
	}

	public ContentDelta getDelta() {
		return delta;
	}

	public void setDelta(ContentDelta delta) {
		this.delta = delta;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// super.write(out);
		url.write(out);
		out.writeInt(version);
		// --
		DeltaType type = (delta instanceof DocumentLevelDelta) ? DeltaType.DOC_LVL
				: DeltaType.WORD_LVL;
		out.writeInt(DeltaType.encode(type));
		delta.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// super.readFields(in);
		url = new Text();
		url.readFields(in);
		version = in.readInt();
		// --
		int typeCode = in.readInt();
		DeltaType type = DeltaType.decode(typeCode);
		delta = DeltaType.getInstance(type);
		delta.readFields(in);
	}

	@Override
	public int compareTo(VersionedSiteData that) {
		int retVal = super.compareTo(that);
		if (retVal == 0) {
			if (this.delta instanceof DocumentLevelDelta) {
				if (((SiteContent) that).delta instanceof DocumentLevelDelta) {
					return ((DocumentLevelDelta) this.delta)
							.compareTo((DocumentLevelDelta) ((SiteContent) that).delta);
				} else {
					return -1;
				}
			} else if (this.delta instanceof WordLevelDelta) {
				if (((SiteContent) that).delta instanceof WordLevelDelta) {
					return ((WordLevelDelta) this.delta)
							.compareTo((WordLevelDelta) ((SiteContent) that).delta);
				} else {
					return -1;
				}
			} else {
				return 1;
			}
		}
		return retVal;
	}

	public Text prettyPrint() {
		return delta.prettyPrint();
	}

}
