package de.uniko.softlang.indexer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.AbstractCluster;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.common.PartialVectorMerger;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.delta.ContentDelta.DeltaType;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;

/**
 * Tool to perform all necessary steps to cluster a set of webistes using
 * Mahout.
 */
public class Clusterer extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Clusterer.class);
	public static final String STRING_TUPLE_DIR = "inVectorize";
	public static final String CLUSTER_BASE = "clustering";
	public static final String CLUSTER_DIR = "clusters";
	public static final int VECTOR_SIZE = 30;
	private ContentDelta.DeltaType deltaLvl;

	private final int MIN_NECC_OCC = 1; // the minimimum number of occurences for
																			// a term to be present in the vector

	public Clusterer() {
	}

	public Clusterer(Configuration conf) {
		setConf(conf);
	}

	public Path cluster(Path[] in, Path outDir, boolean clustering, long time)
			throws Exception {
		return cluster(in, outDir, clustering, null, time);
	}

	protected Path cluster(Path[] in, Path indexDir, boolean clustering,
			Path existingClusters, long time) throws Exception {
		Path clusterBase = new Path(indexDir, CLUSTER_BASE);
		if (!clustering && existingClusters == null) {
			LOG.error("No input cluster specified and cluster computation turned off: no cluster to assign to.....aborting!");
			return null;
		}
		if (getConf() == null)
			setConf(new Configuration());
		getConf().setInt("mapreduce.job.reduces", Crawl.NUM_REDUCE);
		this.deltaLvl = DeltaType.decode(getConf()
				.getInt(DeltaFetcher.DELTA_LVL, 0));
		// TODO find correct values here
		getConf().setInt(PartialVectorMerger.DIMENSION, VECTOR_SIZE);
		int numReducers = getConf().getInt("mapred.reduce.tasks", 1);
		int chunckSizeInMegabytes = 64;
		String convergenceDelta = Double.toString(0.5);

		setConf(getConf());
		Path clusterDir;

		Path vectorDir;
		int numClusters = 0;
		vectorDir = new Path(clusterBase, TermFreqVectorCreator.VECTOR_BASE_DIR
				+ "/" + time);
		Path out = new Path(clusterBase, TermFreqVectorCreator.VECTOR_BASE_DIR);
		TermFreqVectorCreator vectorCreat = new TermFreqVectorCreator();
		boolean wordLvlDelta = deltaLvl == ContentDelta.DeltaType.WORD_LVL ? true
				: false;
		long numVectors = vectorCreat.createTermFreqVectors(in, out, time,
				wordLvlDelta);
		numClusters = (int) numVectors / 3;

		// use SquaredEuclideanDistanceMeasure as distance measure
		String measureClass = SquaredEuclideanDistanceMeasure.class.getName();
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		DistanceMeasure measure = ccl.loadClass(measureClass)
				.asSubclass(DistanceMeasure.class).newInstance();

		// calculate clusters iteratively if required
		if (clustering) {
			// in case no previous cluster is specified, randomly chose some of the
			// input vectors as initial clusters
			if (existingClusters == null) {
				existingClusters = RandomSeedGenerator.buildRandom(vectorDir, new Path(
						clusterBase, "randomSeed"), numClusters, measure);
			}
			clusterDir = new Path(clusterBase, CLUSTER_DIR);
			clusterDir = KMeansDriver.buildClusters(getConf(), vectorDir,
					existingClusters, clusterDir, measure, 2, convergenceDelta, false);
		} else {
			clusterDir = existingClusters;
		}

		// assign the input vectors to the clusters
		// writes <IntWritable,WeightedVectorWritable> pairs (initial weight = 1)
		Path clusteredData = new Path(clusterBase,
				AbstractCluster.CLUSTERED_POINTS_DIR + "/" + time);

		KMeansDriver.clusterData(getConf(), vectorDir, clusterDir, clusteredData,
				measure, convergenceDelta, false);
		return clusteredData;
	}

	public static void usage() {
		System.err
				.println("Usage: Cluster <content_dir> <out_dir> [-noClustering]");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		boolean clustering = true;
		if (args.length > 2 && args[2].equals("-noClustering"))
			clustering = false;
		Path[] in = { new Path(args[0]) };
		cluster(in, new Path(args[1]), clustering, System.currentTimeMillis()); // 10,
																																						// 20
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Clusterer(), args);
		System.exit(res);
	}
}
