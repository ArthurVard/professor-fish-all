package de.uniko.softlang.crawler.delta;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.StringTuple;

import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.utils.MutableInt;

/**
 * Data store for a delta of a version of the content of a website. This class
 * implements document level deltas, meaning that all previous content versions
 * are considered deprecated, and this instance contains the current content in
 * its entirety. 
 * For detailed deltas use <code>WordLevelDelta</code>.
 */
public class DocumentLevelDelta implements ContentDelta {

	private Text content;

	public DocumentLevelDelta() {
	}

	public DocumentLevelDelta(Text content) {
		this.content = content;
	}

	public DocumentLevelDelta(String content) {
		this.content = new Text(content);
	}

	public Text getContent() {
		return content;
	}

	public void setContent(Text content) {
		this.content = content;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		content.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		content = new Text();
		content.readFields(in);
	}

	@Override
	public int compareTo(ContentDelta that) {
		return this.content.compareTo(((DocumentLevelDelta) that).content);
	}

	@Override
	public String getAsString() {
		return content.toString();
	}

	@Override
	public StringTuple getAsTuple() {
		StringTuple doc = new StringTuple();
		StringTokenizer tok = new StringTokenizer(content.toString());
		while (tok.hasMoreTokens()) {
			doc.add(tok.nextToken());
		}
		return doc;
	}

	@Override
	public OccurrenceMap<String> getAsMap() {
		OccurrenceMap<String> output = new OccurrenceMap<String>();
		StringTokenizer tok = new StringTokenizer(content.toString());
		while (tok.hasMoreTokens()) {
			output.put(tok.nextToken());
		}
		return output;
	}

	@Override
	public MapWritable getAsMapWritable() {
		MapWritable output = new MapWritable();
		StringTokenizer tok = new StringTokenizer(content.toString());
		OccurrenceMap<Text> map = new OccurrenceMap<Text>();
		while (tok.hasMoreTokens()) {
			map.put(new Text(tok.nextToken()));
		}
		for (Text key : map.keySet()) {
			output.put(key, map.get(key));
		}
		return output;
	}

	@Override
	public boolean isEmpty() {
		return content.getLength() == 0;
	}

	@Override
	public Text prettyPrint() {
		return new Text("Document-level delta: Content = " + content.toString());
	}

}
