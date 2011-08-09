package de.uniko.softlang.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.IOUtils;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.delta.DocumentLevelDelta;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.utils.ListWritable;
import de.uniko.softlang.utils.MutableInt;
import de.uniko.softlang.utils.PairWritable;

/**
 * Tool to create an inverted index, that maps from words to websites.
 */
public class InvertedIndex extends Configured implements Tool {
	
	public static class InvertedIndexMapper extends Mapper<Text, TupleWritable, Text, ListWritable>{
		private Text word = new Text();
		private boolean useDocMap = false;
		
		
		protected void map(Text key, TupleWritable values, Context context) throws IOException, InterruptedException {
			OccurrenceMap<String> contentMap;
			
			//check delta level
			SiteContent first = (SiteContent)values.iterator().next();
			if(first != null && first.getDelta() instanceof DocumentLevelDelta){
				SiteContent latestVersion = null;
				for (Writable val : values) {
					SiteContent content = (SiteContent) val;
					if(latestVersion == null || content.getVersion() > latestVersion.getVersion())
						latestVersion = content;
				}
				contentMap = latestVersion.getDelta().getAsMap();
			}else{
				contentMap = new OccurrenceMap<String>();
				for(Writable w : values){
					SiteContent c = (SiteContent)w;
					inserIntoMap(c.getDelta().getAsMap(),contentMap);	
				}
			}
			
			
			for(String w : contentMap.keySet()){
				ListWritable l = new ListWritable();
				l.add(new PairWritable<Text, MutableInt>(key, contentMap.get(w)));
				context.write(new Text(w), l);
			}
			
		}

		private void inserIntoMap(OccurrenceMap<String> sourceMap,
				OccurrenceMap<String> targetMap) {
			for (String s : sourceMap.keySet()) {
				targetMap.put(s, sourceMap.get(s));
			}
		}

		
		
	}
	
	public static class InvertedIndexReducer extends Reducer<Text, ListWritable, Text, ListWritable>{
		protected void reduce(Text key, Iterable<ListWritable> values, Context context) throws IOException, InterruptedException {
			ListWritable list = new ListWritable();
			for(ListWritable val : values){
				list.addAll(val);
			}
			context.write(key, list);
		}
	}
	
	public InvertedIndex(){
		setConf(new Configuration());
	}
	
	public InvertedIndex(Configuration conf){
		setConf(conf);
	}

	
	public boolean createInvertedIndex(Path[] in, Path out) throws Exception {
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("InvertedIndexer");
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		
		Path[] inputContent = new Path[in.length];
		for(int i = 0; i < in.length; i++){
			inputContent[i] = new Path(in[i], DeltaFetcher.CONTENT_DIR);
		}

		job.setInputFormatClass(CompositeInputFormat.class);
		job.getConfiguration().set(
				CompositeInputFormat.JOIN_EXPR,
				CompositeInputFormat.compose("outer", SequenceFileInputFormat.class, inputContent));

//		job.setInputFormatClass(SequenceFileInputFormat.class);
//	  for(int i = 0; i < in.length; i++){
//	  	SequenceFileInputFormat.addInputPath(job, new Path(in[i], BruteFetcher.CONTENT_DIR));
//	  }
//		
		job.setJarByClass(InvertedIndex.class);
	  job.setMapperClass(InvertedIndexMapper.class);
	  job.setMapOutputKeyClass(Text.class);
	  job.setMapOutputValueClass(ListWritable.class);
	  job.setCombinerClass(InvertedIndexReducer.class);
	  job.setReducerClass(InvertedIndexReducer.class);
	  job.setOutputKeyClass(Text.class);
	  job.setOutputValueClass(ListWritable.class);

	  SequenceFileOutputFormat.setOutputPath(job, out);
	  job.setOutputFormatClass(SequenceFileOutputFormat.class);
	  return job.waitForCompletion(true);
	}
	

	
	public static void usage(){
		System.err.println("Usage: InvertedIndexer <segment_dir> <out_dir> ");
  }
	
	public int run(String[] args) throws Exception {
	    if (args.length < 2) {
	      usage();
	    	return -1;
	    }	
	    Path[] in = {new Path(args[0])};
			createInvertedIndex(in, new Path(args[1]));
			return 0;
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new InvertedIndex(), args);
	    System.exit(res);
	}

}
