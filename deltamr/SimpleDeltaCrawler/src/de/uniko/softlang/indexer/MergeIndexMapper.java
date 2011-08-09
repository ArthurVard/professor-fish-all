package de.uniko.softlang.indexer;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;

import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.utils.ListWritable;
import de.uniko.softlang.utils.MutableInt;
import de.uniko.softlang.utils.PairWritable;

public class MergeIndexMapper extends Mapper<Text, TupleWritable, Text, ListWritable> {
	DeltaType deltaLevel;
	
	@Override
  protected void setup(Context context) throws IOException, InterruptedException {
		deltaLevel = DeltaType.decode(context.getConfiguration().getInt(DeltaFetcher.DELTA_LVL, 0));
	}
	
	public void map(Text key, TupleWritable values, Context context) throws InterruptedException, IOException {
		if(!values.has(0) || !values.has(1)){
			//since there are at most two inputs to be joined (the single delta and the full index),
			// and one of them has not data for this key, we can immediately write the single value
			ListWritable singleList = (ListWritable)values.iterator().next();
			context.write(key, singleList);
		}
		//merge
		OccurrenceMap<Text> map = new OccurrenceMap<Text>();
		for(Writable wList : values){
			ListWritable l = (ListWritable)wList;
			for(Writable wPair : l){
				PairWritable<Text, MutableInt> p = (PairWritable<Text, MutableInt>)wPair;
				map.put(p.getFirst(), p.getSecond());
			}
		}
		ListWritable list = new ListWritable();
		for(Text url : map.keySet()){
			list.add(new PairWritable<Text, MutableInt>(url, map.get(url)));
		}
		context.write(key, list);
	}

}
