package de.uniko.softlang.crawler;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.AbstractCluster;
import org.apache.mahout.utils.clustering.ClusterDumper;

import de.uniko.softlang.crawler.delta.ContentDelta;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.crawler.fetcher.Fetcher;
import de.uniko.softlang.crawler.fetcher.SimpleFetcher;
import de.uniko.softlang.crawler.print.PrintCrawlDb;
import de.uniko.softlang.crawler.print.PrintLinkDb;
import de.uniko.softlang.crawler.print.PrintSegment;
import de.uniko.softlang.indexer.Clusterer;
import de.uniko.softlang.indexer.Indexer;
import de.uniko.softlang.indexer.print.PrintIndex;

/**
 * Tool to re-perform the crawl step in order to discover changed websites, by
 * performing a specified number of iterations of all steps in the crawl-cycle.
 */
public class ReCrawl extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(ReCrawl.class);

	public String[] reCrawlWithDetlas(Path baseDir, int iterations,
			boolean prettyPrint, boolean index, boolean inclNewLinks,
			boolean wordLvl, boolean merge, int deltaSize) throws Exception {
		return crawl(baseDir, iterations, prettyPrint, index, inclNewLinks, true,
				wordLvl, merge, deltaSize);
	}

	public String[] reCrawlNoDetlas(Path baseDir, int iterations,
			boolean prettyPrint, boolean index, boolean inclNewLinks, boolean merge,
			int deltaSize) throws Exception {
		return crawl(baseDir, iterations, prettyPrint, index, inclNewLinks, false,
				false, merge, deltaSize);
	}

	private String[] crawl(Path baseDir, int iterations, boolean prettyPrint,
			boolean index, boolean inclNewLinks, boolean useDeltas, boolean wordLvl,
			boolean merge, int deltaSize) throws Exception {
		long start = System.currentTimeMillis();
		int dpPrinterCounter = 0;
		Configuration conf = new Configuration();
		ContentDelta.DeltaType deltaType = ContentDelta.DeltaType.WORD_LVL;
		if (wordLvl)
			conf.setInt(DeltaFetcher.DELTA_LVL,
					ContentDelta.DeltaType.encode(deltaType));

		Path crawlDb = new Path(baseDir, Injector.CRAWL_DB);
		Path linkDb = new Path(baseDir, LinkInverter.LINKDB);
		Path segmentBase = new Path(baseDir, Generator.SEGMENTS_DIR);
		Path indexDir = new Path(baseDir, Indexer.INDEX_DIR);
		boolean success;
		long jobStart, jobEnd, genTime, fetchTime, updateTime, linkDbTime, indexTime;
		genTime = fetchTime = updateTime = linkDbTime = indexTime = 0;

		List<Path> segments = new LinkedList<Path>();
		for (int i = 0; i < iterations; i++) {

			// generate
			System.out.println("Generate");
			Generator generator = new Generator(conf);
			jobStart = System.currentTimeMillis();
			success = generator.generate(crawlDb, segmentBase, Long.MIN_VALUE,
					inclNewLinks);
			jobEnd = System.currentTimeMillis();
			genTime = jobEnd - jobStart;
			if (!success)
				return null;

			// fetch
			System.out.println("Fetch");
			Fetcher fetcher;
			if (useDeltas)
				fetcher = new DeltaFetcher(conf);
			else
				fetcher = new SimpleFetcher(conf);
			jobStart = System.currentTimeMillis();
			Path segment;
			if (useDeltas && deltaSize < 100)
				segment = ((DeltaFetcher) fetcher).fetch(segmentBase, deltaSize);
			else
				segment = fetcher.fetch(segmentBase);
			jobEnd = System.currentTimeMillis();
			fetchTime = jobEnd - jobStart;
			if (segment == null)
				return null;
			else
				segments.add(segment);

			// update
			System.out.println("update");
			jobStart = System.currentTimeMillis();
			CrawlDbUpdater updater = new CrawlDbUpdater(conf);
			jobEnd = System.currentTimeMillis();
			updateTime = jobEnd - jobStart;
			success = updater.update(crawlDb, segmentBase, segment.getName());
			if (!success)
				return null;

			// build LinkDb
			System.out.println("LinkDb");
			LinkInverter inv = new LinkInverter(conf);
			Path[] recentSeg = { segment };
			jobStart = System.currentTimeMillis();
			Path newDb = inv.invert(linkDb, recentSeg);
			jobEnd = System.currentTimeMillis();
			linkDbTime = jobEnd - jobStart;
			if (newDb == null)
				return null;

			if (prettyPrint) {
				// print segment & crawlDb
				System.out.println("PrintSegment");
				PrintSegment segPrinter = new PrintSegment(conf);
				success = segPrinter.print(segment, new Path(segment, "pretty"));
				if (!success)
					return null;

				System.out.println("PrintCrawlDb");
				PrintCrawlDb dbPrinter = new PrintCrawlDb(conf);
				success = dbPrinter.print(crawlDb, new Path(baseDir, "pretty_crawlDb_"
						+ dpPrinterCounter));

				System.out.println("PrintLinkDb");
				PrintLinkDb ldbPrinter = new PrintLinkDb(conf);
				success = ldbPrinter.print(newDb, new Path(baseDir, "pretty_LinkDb_"
						+ dpPrinterCounter++));
				if (!success)
					return null;
			}
		}
		if (index) {
			System.out.println("Index");
			Indexer i = new Indexer(conf);
			Path clusterBase = new Path(indexDir, Clusterer.CLUSTER_BASE);
			Path clusters = new Path(clusterBase, Clusterer.CLUSTER_DIR);
			Path highesCluster = Indexer.findLatesCluster(clusters);
			jobStart = System.currentTimeMillis();
			String indexSubdirName = i.indexNoClusterRecompution(
					segments.toArray(new Path[segments.size()]), indexDir, highesCluster);
			jobEnd = System.currentTimeMillis();
			indexTime = jobEnd - jobStart;
			if (prettyPrint) {
				// dump cluster
				ClusterDumper dump = new ClusterDumper();
				Path highestCluster = Indexer.findLatesCluster(clusters); // need to
																																	// call this
																																	// again, as
																																	// the highest
																																	// cluster
																																	// might have
																																	// changed due
																																	// to
																																	// recomputing
				Path dumpOut = new Path(indexDir, "prettyClusters-"
						+ System.currentTimeMillis());
				Path clusteredData = new Path(clusterBase,
						AbstractCluster.CLUSTERED_POINTS_DIR + "/" + indexSubdirName);
				String[] args = { "-s", highestCluster.toString(), "-o",
						dumpOut.toString(), "-p", clusteredData.toString() };
				dump.run(args);

				PrintIndex p = new PrintIndex(conf);
				Path invIndexBase = new Path(indexDir, Indexer.INV_INDEX_BASE);
				Path newIndex = new Path(invIndexBase, indexSubdirName);
				p.print(newIndex,
						new Path(indexDir, "prettyIndex-" + newIndex.getName()));
			}

		}

		String[] retVal = new String[segments.size()];
		int i = 0;
		for (Path p : segments) {
			retVal[i] = p.toString();
			i++;
		}
		long end = System.currentTimeMillis();
		long total = end - start;
		System.out.println("crawling took " + total / 60000 + " min (" + total
				/ 1000 + " sec)");
		System.out.println("\t Generate: " + genTime / 1000 + " sec");
		System.out.println("\t Fetch: " + fetchTime / 1000 + " sec (" + fetchTime
				/ 60000 + " min)");
		System.out.println("\t Update: " + updateTime / 1000 + " sec");
		System.out.println("\t LinkDb: " + linkDbTime / 1000 + " sec");
		System.out.println("\t Index: " + indexTime / 1000 + " sec (" + indexTime
				/ 60000 + " min)");

		return retVal;
	}

	public static void usage() {
		System.err
				.println("Usage: ReCrawl <base_dir> <numIterations> [prettyPrint] [index] [useDeltas (wordLvl | docLvl)] [noAdditions] [merge] [deltaSize <size>]");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			int iterations = Integer.parseInt(args[1]);
			boolean pretty = false;
			boolean index = false;
			boolean inclNewLinks = true;
			boolean useDeltas = false;
			boolean wordLvl = false;
			boolean merge = false;
			int deltaSize = 100;
			for (int i = 2; i < args.length; i++) {
				if (args[i].equals("prettyPrint"))
					pretty = true;
				else if (args[i].equals("index"))
					index = true;
				else if (args[i].equals("noAdditions"))
					inclNewLinks = false;
				else if (args[i].equals("merge"))
					merge = true;
				else if (args[i].equals("deltaSize")) {
					deltaSize = Integer.parseInt(args[++i]);
				} else if (args[i].equals("useDeltas")) {
					useDeltas = true;
					if (args.length > i + 1 && args[i + 1].equals("wordLvl")) {
						wordLvl = true;
						i++;
					}
				}
			}
			crawl(new Path(args[0]), iterations, pretty, index, inclNewLinks,
					useDeltas, wordLvl, merge, deltaSize);
			return 0;
		} catch (Exception e) {
			LOG.fatal("Crawler: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new ReCrawl(), args);
		System.exit(res);
	}
}
