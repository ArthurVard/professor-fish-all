package de.uniko.softlang.indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.datastores.OccurrenceMap;
import de.uniko.softlang.utils.ListWritable;
import de.uniko.softlang.utils.MutableInt;
import de.uniko.softlang.utils.PairWritable;

/**
 * Tool to merge Indices. This class aims at proving means to merge multiple
 * indices in user scenarios. In the context of this web crawler, however, we
 * discourage usage of this class, as the <code>IndexLoader</code> assumes
 * multiple unique deltas of inverted indices.
 */
@Deprecated
public class MergeInvertedIndices extends Configured implements Tool {

	public static class MergeReducer extends
			Reducer<Text, ListWritable, Text, ListWritable> {
		protected void reduce(Text key, Iterable<ListWritable> values,
				Context context) throws IOException, InterruptedException {
			OccurrenceMap<Text> map = new OccurrenceMap<Text>();
			for (ListWritable val : values) {
				for (Writable w : val) {
					PairWritable<Text, MutableInt> p = (PairWritable<Text, MutableInt>) w;
					map.put(p.getFirst(), p.getSecond());
				}
			}
			ListWritable list = new ListWritable();
			for (Text url : map.keySet()) {
				list.add(new PairWritable<Text, MutableInt>(url, map.get(url)));
			}
			context.write(key, list);
		}
	}

	public MergeInvertedIndices() {
		setConf(new Configuration());
	}

	public MergeInvertedIndices(Configuration conf) {
		setConf(conf);
	}

	public boolean mergeIndices(Path[] in, Path out) throws Exception {
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("MergeInvertedIndices");
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		for (int i = 0; i < in.length; i++) {
			SequenceFileInputFormat.addInputPath(job, in[i]);
		}

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setJarByClass(MergeInvertedIndices.class);
		job.setMapperClass(Mapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(ListWritable.class);
		job.setReducerClass(MergeReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ListWritable.class);

		SequenceFileOutputFormat.setOutputPath(job, out);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job.waitForCompletion(true);
	}

	public static void usage() {
		System.err
				.println("Usage: MergeInvertedIndices <out_dir> <indexdir1> <indexdir2> (<indexdir>*)");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			usage();
			return -1;
		}
		Path[] in = new Path[args.length - 1];
		for (int i = 0; i < args.length - 1; i++) {
			in[i] = new Path(args[i + 1]);
		}
		mergeIndices(in, new Path(args[0]));
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MergeInvertedIndices(),
				args);
		System.exit(res);
	}

}
