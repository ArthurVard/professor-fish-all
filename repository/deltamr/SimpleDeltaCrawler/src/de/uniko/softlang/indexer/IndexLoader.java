package de.uniko.softlang.indexer;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.AbstractCluster;
import org.apache.mahout.clustering.WeightedVectorWritable;
import org.apache.mahout.math.NamedVector;

import de.uniko.softlang.crawler.fetcher.SimpleHttpParser;
import de.uniko.softlang.utils.ListWritable;
import de.uniko.softlang.utils.MutableInt;
import de.uniko.softlang.utils.PairWritable;

/**
 * Tool to load a previously computed index, and to answer queries. 
 * As this implementation is far from being efficient, we propose 
 * the implementation of a more advanced index-loading driver for 
 * distributed scenarios.
 */
public class IndexLoader extends Configured implements Tool {

	private class WordFreq implements Comparable<WordFreq> {
		private String url;
		private Integer freq;

		public WordFreq(String url, int freq) {
			this.url = url;
			this.freq = freq;
		}

		@Override
		public int compareTo(WordFreq that) {
			int retVal = this.freq.compareTo(that.freq);
			if (retVal == 0)
				retVal = this.url.compareTo(that.url);
			return retVal;
		}

	}

	private static final int BOOSTED_CLUSTERS = 2;
	private static final int BOOST_FACTOR = 2;

	private HashMap<String, DocumentMapEntry> docMap;
	private HashMap<Integer, List<String>> clusterMap;
	Path invIndexBase;

	public IndexLoader() {
		setConf(new Configuration());
	}

	/**
	 * Main method that loads an index and waits and handles arriving queries.
	 * TODO make multithreaded TODO enable queries for stop-words - they are
	 * currently being removed from the query.
	 * 
	 * @param indexDir
	 *          the base dir of the index
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void loadIndex(Path indexDir) throws IOException,
			InstantiationException, IllegalAccessException {
		// TODO relative paths create erors!!
		createIndex(indexDir);
		invIndexBase = new Path(indexDir, Indexer.INV_INDEX_BASE);
		while (true) {
			System.out.println("Enter search query: (type $exit$ to quit)");
			Scanner in = new Scanner(System.in);
			String line = in.nextLine();
			if (line.equals("$exit$"))
				System.exit(0);
			SimpleHttpParser parser = new SimpleHttpParser();
			line = parser.cleanText(line);
			String[] searchTerms = line.split(" ");
			String[] matchingUrls = searchNaiveFromDisc(searchTerms);
			for (int i = 0; i < matchingUrls.length; i++) {
				System.out.println(matchingUrls[i]);
			}
		}
	}

	// naively traverse all inverted-index files and check for each word if it is
	// searched for
	private String[] searchNaiveFromDisc(String[] searchTerms)
			throws IOException, InstantiationException, IllegalAccessException {
		HashSet<String> terms = new HashSet<String>();
		for (int i = 0; i < searchTerms.length; i++) {
			terms.add(searchTerms[i]);
		}
		Map<String, QueryMatch> retVal = new HashMap<String, QueryMatch>();
		FileSystem fs = invIndexBase.getFileSystem(getConf());
		FileStatus[] indexDirs = fs.listStatus(invIndexBase);
		for (int i = 0; i < indexDirs.length; i++) {
			long currentSegment = Long.parseLong(indexDirs[i].getPath().getName());
			List<Path> files = listRelevantFiles(indexDirs[i].getPath());
			for (Path file : files) {
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, file,
						getConf());
				Text key = reader.getKeyClass().asSubclass(Text.class).newInstance();
				ListWritable list = reader.getValueClass()
						.asSubclass(ListWritable.class).newInstance();
				while (reader.next(key, list)) {
					if (terms.contains(key.toString())) {
						for (Writable w : list) {
							// for each <url,count> entry do...
							PairWritable<Text, MutableInt> p = (PairWritable<Text, MutableInt>) w;
							String candidateUrl = p.getFirst().toString();
							DocumentMapEntry candidateEntry = docMap.get(candidateUrl);
							if (candidateEntry == null) {
								System.err.println("Unexpected: No entry for potential match "
										+ candidateUrl);
								continue;
							}
							if (candidateEntry.getSegmentNo() != currentSegment) {
								// there is an more up-to-date segment for this
								// TODO handle word-level
								continue;
							} else {
								QueryMatch m = retVal.get(candidateUrl);
								if (m == null) {
									QueryMatch newMatch = new QueryMatch(candidateEntry);
									newMatch.update(key.toString(), p.getSecond());
									retVal.put(candidateUrl, newMatch);
								} else
									m.update(key.toString(), p.getSecond());
							}
						}
					}
				}
			}
		}
		return sortUrlList(retVal);
	}

	/**
	 * Sort the list of matching sites by rank and cluster-membership
	 * 
	 * @param matches
	 *          the list of matching sites
	 * @return a sorted list of matches
	 */
	private String[] sortUrlList(Map<String, QueryMatch> matches) {
		// TODO Auto-generated method stub
		Queue<WordFreq> pq = new PriorityQueue<WordFreq>();
		for (String url : matches.keySet()) {
			int score = calculateScore(matches.get(url));
			pq.add(new WordFreq(url, score));
		}
		modifyOrderingByCluster(pq, matches);
		String[] retVal = new String[pq.size()];
		int i = 0;
		for (WordFreq wf : pq) {
			retVal[i] = wf.url;
			i++;
		}
		return retVal;
	}

	// modify the ordering of the Queue by boosting documents that belong
	// to the same cluster as the top ranking documents
	private void modifyOrderingByCluster(Queue<WordFreq> pq,
			Map<String, QueryMatch> matches) {
		Iterator<WordFreq> it = pq.iterator();
		Set<Integer> clustersToBoost = new HashSet<Integer>();
		int i = 0;
		while (clustersToBoost.size() < BOOSTED_CLUSTERS && it.hasNext()) {
			clustersToBoost.add(matches.get(it.next().url).getEntry()
					.getClusterMembership());
			i++;
		}
		for (WordFreq wf : pq) {
			if (clustersToBoost.contains(clusterMap.get(wf.url))) {
				wf.freq *= BOOST_FACTOR;
			}
		}
	}

	// calculate a documents score by adding up occurences of search-terms
	private int calculateScore(QueryMatch queryMatch) {
		int factor = queryMatch.getWordOcc().keySet().size();
		int total = 0;
		for (MutableInt i : queryMatch.getWordOcc().values()) {
			total += i.get();
		}
		return total * factor;
	}

	/**
	 * Create the index by loading a document map that contains for each url the
	 * last segment the corresponding website's content was changed as well as the
	 * cluster membership of this site.
	 * 
	 * @param indexDir
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void createIndex(Path indexDir) throws IOException,
			InstantiationException, IllegalAccessException {
		Path clusterBase = new Path(indexDir, Clusterer.CLUSTER_BASE);
		Path clusteredPoints = new Path(clusterBase,
				AbstractCluster.CLUSTERED_POINTS_DIR);
		FileSystem fs = indexDir.getFileSystem(getConf());
		FileStatus[] clusters = fs.listStatus(clusteredPoints);
		TreeSet<Path> orderedDirs = new TreeSet<Path>(new Comparator<Path>() {
			// invert ordering, so latest directories are returned first
			@Override
			public int compare(Path p1, Path p2) {
				return p1.compareTo(p2) * -1;
			}
		});
		for (int i = 0; i < clusters.length; i++) {
			// make sure they are odered
			orderedDirs.add(clusters[i].getPath());
		}
		docMap = new HashMap<String, DocumentMapEntry>();
		clusterMap = new HashMap<Integer, List<String>>();
		for (Path p : orderedDirs) {
			List<Path> files = listRelevantFiles(p);
			for (Path file : files) {
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, file,
						getConf());
				IntWritable key = reader.getKeyClass().asSubclass(IntWritable.class)
						.newInstance();
				WeightedVectorWritable vw = reader.getValueClass()
						.asSubclass(WeightedVectorWritable.class).newInstance();
				while (reader.next(key, vw)) {
					if (!(vw.getVector() instanceof NamedVector)) {
						throw new IllegalArgumentException(
								"clustered data was not instance of NamedVector");
					}
					NamedVector nv = (NamedVector) vw.getVector();
					if (!docMap.containsKey(nv.getName())) {
						docMap.put(nv.getName(),
								new DocumentMapEntry(nv.getName(), Long.parseLong(p.getName()),
										key.get()));
						List<String> l = clusterMap.get(key.get());
						if (l == null) {
							l = new LinkedList<String>();
							clusterMap.put(key.get(), l);
						}
						l.add(nv.getName());
					}
				}
			}
		}

	}

	// Helper method to find all relevant "part-xxxxx" files; omitting the
	// "_SUCCESS" files.
	private List<Path> listRelevantFiles(Path p) throws IOException {
		List<Path> result = new LinkedList<Path>();

		// filter out the files
		PathFilter clusterFileFilter = new PathFilter() {
			@Override
			public boolean accept(Path path) {
				return path.getName().startsWith("part");
			}
		};
		FileSystem fs = p.getFileSystem(getConf());
		FileStatus[] matches = fs.listStatus(p, clusterFileFilter);

		for (FileStatus match : matches) {
			result.add(fs.makeQualified(match.getPath()));
		}
		return result;
	}

	public static void usage() {
		System.err.println("Usage: LoadIndex <index_dir>");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
			return -1;
		}
		loadIndex(new Path(args[0]));
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new IndexLoader(), args);
		System.exit(res);
	}
}
