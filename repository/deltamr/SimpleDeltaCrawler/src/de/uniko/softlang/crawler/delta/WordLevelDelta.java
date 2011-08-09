package de.uniko.softlang.crawler.delta;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.StringTuple;

import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.utils.MutableInt;

/**
 * Data store for a delta of a version of the content of a website. This class
 * implements word level deltas, meaning that a deltailed delta of all added and
 * deleted words is being stored. In order to recreated the current state of the
 * website, older deltas are needed as well. For deltas at a more abstract level
 * use <code>DocumentLevelDelta</code>.
 */
public class WordLevelDelta implements ContentDelta {

	private OccurrenceMap<String> content = new OccurrenceMap<String>();

	public WordLevelDelta() {
	}

	public WordLevelDelta(OccurrenceMap<String> content) {
		this.content = content;
	}

	public WordLevelDelta(String contentStr) {
		insertIntoMap(contentStr, content);
	}

	private static void insertIntoMap(String contentStr,
			OccurrenceMap<String> targetMap) {
		StringTokenizer tok = new StringTokenizer(contentStr);
		while (tok.hasMoreTokens()) {
			targetMap.put(tok.nextToken());
		}
	}

	private static void insertIntoMap(OccurrenceMap<String> sourceMap,
			OccurrenceMap<String> targetMap) {
		for (String s : sourceMap.keySet()) {
			targetMap.put(s, sourceMap.get(s));
		}
	}

	public OccurrenceMap<String> getContent() {
		return content;
	}

	public void setContent(OccurrenceMap<String> content) {
		this.content = content;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(content.size());
		Text key = new Text();
		IntWritable value = new IntWritable();
		for (String s : content.keySet()) {
			key.set(s);
			key.write(out);
			value.set(content.get(s).get());
			value.write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		content = new OccurrenceMap<String>(size);
		for (int i = 0; i < size; i++) {
			Text key = new Text();
			IntWritable value = new IntWritable();
			key.readFields(in);
			value.readFields(in);
			content.put(key.toString(), value.get());
		}

	}

	@Override
	public int compareTo(ContentDelta that) {
		OccurrenceMap<String> thisContent = this.content;
		OccurrenceMap<String> thatContent = ((WordLevelDelta) that).content;
		if (thisContent.keySet().size() != thatContent.keySet().size()) {
			return thisContent.keySet().size() > thatContent.keySet().size() ? 1 : -1;
		} else {
			for (String s : thisContent.keySet()) {
				if (!thatContent.containsKey(s))
					return 1;
				else {
					if (thisContent.get(s) != thatContent.get(s)) {
						return thisContent.get(s).get() > thatContent.get(s).get() ? 1 : -1;
					}
				}
			}
		}

		return 0;
	}

	@Override
	public String getAsString() {
		String retVal = "";
		for (String word : content.keySet()) {
			for (int i = 0; i < content.get(word).get(); i++)
				retVal += word + " ";
		}
		return retVal.trim();
	}

	@Override
	public StringTuple getAsTuple() {
		StringTuple doc = new StringTuple();
		for (String word : content.keySet()) {
			for (int i = 0; i < content.get(word).get(); i++)
				doc.add(word);
		}
		return doc;
	}

	@Override
	public OccurrenceMap<String> getAsMap() {
		return content;
	}

	@Override
	public MapWritable getAsMapWritable() {
		MapWritable output = new MapWritable();
		OccurrenceMap<Text> map = new OccurrenceMap<Text>();
		for (String s : content.keySet()) {
			map.put(new Text(s));
		}
		return output;
	}

	@Override
	public boolean isEmpty() {
		return content.size() == 0;
	}

	@Override
	public Text prettyPrint() {
		String outString = "Word-level delta: Content =  ";
		for (String s : content.keySet()) {
			outString += "\t<" + s + "," + content.get(s) + "> ";
		}
		return new Text(outString);
	}

	public void apply(ContentDelta otherContent) {
		if (otherContent instanceof WordLevelDelta) {
			insertIntoMap(((WordLevelDelta) otherContent).getContent(), content);
		} else {
			insertIntoMap(
					((DocumentLevelDelta) otherContent).getContent().toString(), content);
		}

	}

	public void apply(String otherContent) {
		insertIntoMap(otherContent, content);
	}

	public void invert() {
		for (String s : content.keySet()) {
			MutableInt i = content.get(s);
			i.set(i.get() * -1);
		}
	}
}
