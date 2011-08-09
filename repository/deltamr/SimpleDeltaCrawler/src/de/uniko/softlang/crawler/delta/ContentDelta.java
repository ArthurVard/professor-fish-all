package de.uniko.softlang.crawler.delta;

import java.util.Map;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.common.StringTuple;

import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.utils.MutableInt;

public interface ContentDelta extends Writable, Comparable<ContentDelta> {
	public enum DeltaType {
		DOC_LVL, WORD_LVL, NONE;
		public static int encode(DeltaType code) {
			if (code == DOC_LVL)
				return 0;
			else
				return 1;
		}

		public static DeltaType decode(int code) {
			if (code == 0)
				return DOC_LVL;
			else
				return WORD_LVL;
		}

		public static ContentDelta getInstance(DeltaType type) {
			if (type == DOC_LVL)
				return new DocumentLevelDelta();
			else
				return new WordLevelDelta();
		}
	};

	public String getAsString();
	
	public StringTuple getAsTuple();
	
	public OccurrenceMap<String> getAsMap();
	
	public MapWritable getAsMapWritable();
	
	public boolean isEmpty();
	
	public Text prettyPrint();
	
	
}
