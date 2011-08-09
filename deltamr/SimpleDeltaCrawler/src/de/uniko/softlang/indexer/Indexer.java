package de.uniko.softlang.indexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.datastores.LinkMap;
import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.indexer.InvertedIndex.InvertedIndexMapper;
import de.uniko.softlang.indexer.InvertedIndex.InvertedIndexReducer;
import de.uniko.softlang.indexer.print.PrintIndex;
import de.uniko.softlang.utils.ListWritable;
import de.uniko.softlang.utils.MutableInt;

/**
 * Tool to perform all necessary steps in order to create an index, including
 * cluster computation and inverted index creation.
 */
public class Indexer extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Indexer.class);
	public static final String WORD_COUNT_BASE = "wordCount-";
	public static final String INV_INDEX_BASE = "invIndex";
	public static final String INDEX_DIR = "index";
	public static final String FULL_INDEX_DIR = "fullIndex";

	public Indexer() {
	}

	public Indexer(Configuration conf) {
		setConf(conf);
	}

	public String indexWithClusterRecompution(Path[] segments, Path indexDir,
			Path highestCluster) throws Exception {
		return createIndex(segments, indexDir, true, highestCluster);
	}

	public String indexNoClusterRecompution(Path[] segments, Path indexDir,
			Path highestCluster) throws Exception {
		return createIndex(segments, indexDir, false, highestCluster);
	}

	private String createIndex(Path[] segments, Path indexDir,
			boolean computeClusters, Path highestCluster) throws Exception {
		if (getConf() == null)
			setConf(new Configuration());
		Clusterer c = new Clusterer(getConf());
		long start = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		Path clusteredData;
		if (computeClusters) {
			clusteredData = c.cluster(segments, indexDir, computeClusters,
					highestCluster, time);
		} else {
			if (highestCluster == null)
				throw new IllegalArgumentException("No existing cluster specified!");
			clusteredData = c.cluster(segments, indexDir, computeClusters,
					highestCluster, time);
		}
		Path newIndex = createIndex(segments, indexDir, time, false);
		long end = System.currentTimeMillis();
		long total = end - start;
		System.out.println("Indexing took " + total / 60000 + " min");
		return "" + time;
	}

	private Path createIndex(Path[] in, Path outBase, long time, boolean merge)
			throws Exception {
		Path invIndexBase = new Path(outBase, INV_INDEX_BASE);
		Path invIndexOut = new Path(invIndexBase, "" + time);
		InvertedIndex inv = new InvertedIndex(getConf());
		inv.createInvertedIndex(in, invIndexOut);
		Path fullIndex = new Path(invIndexBase, FULL_INDEX_DIR);
		if (merge)
			mergeIndices(invIndexOut, fullIndex);
		return invIndexOut;

	}

	public static Path findLatesCluster(Path clusterBase) throws IOException {
		FileStatus[] clusters;
		try {
			clusters = clusterBase.getFileSystem(new Configuration()).listStatus(
					clusterBase);
		} catch (FileNotFoundException e) {
			// no clusters exist yet
			return null;
		}
		Path highestCluster = clusterBase;
		int highestId = -1;
		for (int i = 0; i < clusters.length; i++) {
			String folder = clusters[i].getPath().getName();
			int currentId = Integer
					.parseInt(folder.substring(folder.indexOf('-') + 1));
			if (currentId > highestId) {
				highestId = currentId;
				highestCluster = clusters[i].getPath();
			}
		}
		return highestCluster;
	}

	private void mergeIndices(Path invIndexOut, Path fullIndex)
			throws IOException, InterruptedException, ClassNotFoundException {
		// Merge delta-index & complete index
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("InvertedIndexerMerger");

		FileSystem fs = invIndexOut.getFileSystem(getConf());
		if (fs.exists(fullIndex)) {
			Path[] input = new Path[2];
			input[0] = invIndexOut;
			input[1] = fullIndex;
			Path tmpOut = new Path("" + System.currentTimeMillis());
			job.setInputFormatClass(CompositeInputFormat.class);
			job.getConfiguration().set(
					CompositeInputFormat.JOIN_EXPR,
					CompositeInputFormat.compose("outer", SequenceFileInputFormat.class,
							input));
			job.setJarByClass(Indexer.class);
			job.setMapperClass(MergeIndexMapper.class);
			job.setNumReduceTasks(0);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(ListWritable.class);
			SequenceFileOutputFormat.setOutputPath(job, tmpOut);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			boolean success = job.waitForCompletion(true);
			if (success) {
				// replace full-index by new index in tmp-dir
				fs.delete(fullIndex, true);
				fs.delete(invIndexOut, true);
				fs.rename(tmpOut, fullIndex);
			}
		} else {
			fs.rename(invIndexOut, fullIndex);
		}

	}

	public static void usage() {
		System.err
				.println("Usage: Indexer <segment_dir> <out_dir> (recompute|norecompute) [<existing_clusters>]");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			usage();
			return -1;
		}
		Path[] in = { new Path(args[0]) };
		boolean recompute = false;
		// boolean merge = false;
		Path existingClusters = null;
		if (args[2].equals("recompute")) {
			recompute = true;
			// }else if(args[i].equals("merge")){
			// merge = true;
		} else {
			try {
				existingClusters = new Path(args[3]);
			} catch (Exception e) {
				usage();
				System.exit(1);
			}
		}
		if (recompute) {
			indexWithClusterRecompution(in, new Path(args[1]), existingClusters);
		} else {
			if (existingClusters == null) {
				System.err.println("No existing cluster specified!");
				System.exit(1);
			}
			indexNoClusterRecompution(in, new Path(args[1]), existingClusters);
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Indexer(), args);
		System.exit(res);
	}

}
