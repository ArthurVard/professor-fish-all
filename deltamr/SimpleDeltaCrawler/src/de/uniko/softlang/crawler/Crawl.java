package de.uniko.softlang.crawler;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
 * Tool to perform a specified number of iterations of all steps in the crawl-cycle.
 */
public class Crawl extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Crawl.class);
	public static final int NUM_REDUCE = 48;
	public static final boolean BASELINE = false;
	public static final boolean DELTA_BASELINE = false;
	
	
	public String[] crawlWithDeltas(Path baseDir, Path seedList, int iterations, boolean prettyPrint, boolean index, boolean wordLvl) throws Exception {
		return crawl(baseDir, seedList, iterations, prettyPrint, index, true, wordLvl);
	}
	
	public String[] crawlNoDeltas(Path baseDir, Path seedList, int iterations,boolean prettyPrint, boolean index) throws Exception {
		return crawl(baseDir, seedList, iterations, prettyPrint, index, false, false);
	}
	
	
	private String[] crawl(Path baseDir, Path seedList, int iterations,
			boolean prettyPrint, boolean index, boolean useDeltas, boolean wordLvl) throws Exception {
		Configuration conf = new Configuration();
		ContentDelta.DeltaType deltaLvl = ContentDelta.DeltaType.WORD_LVL;
		
		if(wordLvl)
			conf.setInt(DeltaFetcher.DELTA_LVL, ContentDelta.DeltaType.encode(deltaLvl));
		
		setConf(conf);
		long startTime = System.currentTimeMillis();
		int dpPrinterCounter = 0;
		Path crawlDb = new Path(baseDir, Injector.CRAWL_DB);
		Path linkDb = new Path(baseDir, LinkInverter.LINKDB);
		Path segmentBase = new Path(baseDir, Generator.SEGMENTS_DIR);
		Path indexDir = new Path(baseDir, Indexer.INDEX_DIR);

		Injector injector = new Injector(getConf());
		boolean success = injector.inject(baseDir, seedList);
		
		
		List<Path> segments = new LinkedList<Path>();
		for (int i = 0; i < iterations; i++) {

			// generate
			System.out.println("Generate");
			Generator generator = new Generator(getConf());
			success = generator.generate(crawlDb, segmentBase, Long.MIN_VALUE, true);
			if (!success)
				return null;

			// fetch
			System.out.println("Fetch");
			Fetcher fetcher; 
			if(useDeltas)
				fetcher = new DeltaFetcher(getConf());
			else
				fetcher = new SimpleFetcher(getConf());
			Path segment = fetcher.fetch(segmentBase);
			if (segment == null)
				return null;
			else
				segments.add(segment);

			// update
			System.out.println("update");
			CrawlDbUpdater updater = new CrawlDbUpdater(getConf());
			success = updater.update(crawlDb, segmentBase, segment.getName());
			if (!success)
				return null;

			
			if (prettyPrint) {
				// print segment & crawlDb
				System.out.println("PrintSegment");
				PrintSegment segPrinter = new PrintSegment(getConf());
				success = segPrinter.print(segment, new Path(segment, "pretty"));
				if (!success)
					return null;

				System.out.println("PrintCrawlDb");
				PrintCrawlDb dbPrinter = new PrintCrawlDb(getConf());
				success = dbPrinter.print(crawlDb, new Path(baseDir, "pretty_crawlDb_"
						+ dpPrinterCounter));
				if (!success)
					return null;
			}
		}
		
		// build LinkDb
		System.out.println("LinkDb");
		LinkInverter inv = new LinkInverter(getConf());
		Path[] recentSeg = segments.toArray(new Path[segments.size()]);
		Path newDb = inv.invert(linkDb, recentSeg);
		if (newDb == null)
			return null;
		if (prettyPrint) {
			System.out.println("PrintLinkDb");
			PrintLinkDb ldbPrinter = new PrintLinkDb(getConf());
			success = ldbPrinter.print(newDb, new Path(baseDir, "pretty_LinkDb-" + startTime));
			if (!success)
				return null;
		}

		if(index){
			System.out.println("Index");
			Indexer i = new Indexer(getConf());
			String indexSubdirName = i.indexWithClusterRecompution(segments.toArray(new Path[segments.size()]), indexDir, null);
			if(prettyPrint){
				//dump cluster
				ClusterDumper dump = new ClusterDumper();
				Path clusterBase = new Path(indexDir,Clusterer.CLUSTER_BASE);
				Path clusters = new Path(clusterBase, Clusterer.CLUSTER_DIR);
				Path highestCluster = Indexer.findLatesCluster(clusters);	// need to call this again, as the highest cluster might have changed due to recomputing
				Path dumpBase = new Path(indexDir, "prettyClusters");
				FileSystem fs = indexDir.getFileSystem(getConf());
				if(!fs.exists(dumpBase)){
					fs.mkdirs(dumpBase);
				}
				Path dumpOut = new Path(dumpBase, ""+startTime);
				Path clusteredData = new Path(clusterBase, AbstractCluster.CLUSTERED_POINTS_DIR + "/" + indexSubdirName);
				String[] args = {"-s", highestCluster.toString(), "-o", dumpOut.toString(), "-p", clusteredData.toString()};
				//dump.run(args);
				
				PrintIndex p = new PrintIndex();
				Path invIndexBase = new Path(indexDir,Indexer.INV_INDEX_BASE);
				Path newIndex = new Path(invIndexBase,indexSubdirName);
				p.print(newIndex, new Path(indexDir,"prettyIndex-" + newIndex.getName()));
			}
				
		}

		String[] retVal = new String[segments.size()];
		int i = 0;
		for(Path p : segments){
			retVal[i] = p.toString();
			i++;
		}
		long end = System.currentTimeMillis();
		long total = end-startTime;
		System.out.println("crawling took " + total/60000 +" min (" + total/1000 + "sec)");
		return retVal;
	}

	public static void usage(){
		System.err.println("Usage: Crawl <base_dir> <url_dir> <numIterations> [prettyPrint] [index] [useDeltas (wordLvl | docLvl)]");
	}
	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			usage();
			return -1;
		}
		try {
			int iterations = Integer.parseInt(args[2]);
			boolean pretty = false;
			boolean index = false;
			boolean useDeltas = false;
			boolean wordLvl = false;
			for (int i = 3; i < args.length; i++) {
				if(args[i].equals("prettyPrint"))
					pretty = true;
				else if(args[i].equals("index"))
						index = true;
				else if(args[i].equals("useDeltas")){
					useDeltas = true;
					if(args.length > i+1 && args[i+1].equals("wordLvl")){
						wordLvl = true;
						i++;
					}
				}
			}
			crawl(new Path(args[0]), new Path(args[1]), iterations, pretty, index, useDeltas, wordLvl);
			return 0;
		} catch (Exception e) {
			LOG.fatal("Crawler: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Crawl(), args);
		System.exit(res);
	}
}
