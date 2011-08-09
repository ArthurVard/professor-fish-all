package de.uniko.softlang;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Baseline;
import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.CrawlDbUpdater;
import de.uniko.softlang.crawler.Generator;
import de.uniko.softlang.crawler.Injector;
import de.uniko.softlang.crawler.LinkInverter;
import de.uniko.softlang.crawler.ReCrawl;
import de.uniko.softlang.crawler.fetcher.BaselineFetcher;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.crawler.fetcher.SimpleFetcher;
import de.uniko.softlang.crawler.print.ExtractSeedList;
import de.uniko.softlang.crawler.print.FilterSeedList;
import de.uniko.softlang.crawler.print.PrintCrawlDb;
import de.uniko.softlang.crawler.print.PrintLinkDb;
import de.uniko.softlang.crawler.print.PrintSegment;
import de.uniko.softlang.indexer.Clusterer;
import de.uniko.softlang.indexer.Indexer;
import de.uniko.softlang.indexer.InvertedIndex;
import de.uniko.softlang.indexer.IndexLoader;
import de.uniko.softlang.indexer.MergeClusters;
import de.uniko.softlang.indexer.MergeInvertedIndices;
import de.uniko.softlang.test.CrawlAndUpdate;

public class Driver {
	
	private Map<String, Class<? extends Tool>> registeredTools;
	
	public Driver(){
		registeredTools = new HashMap<String, Class<? extends Tool>>();
		registeredTools.put("Injector", Injector.class);
		registeredTools.put("Generator", Generator.class);
		registeredTools.put("SimpleFetcher", SimpleFetcher.class);
		registeredTools.put("DeltaFetcher", DeltaFetcher.class);
		registeredTools.put("CrawlDbUpdater", CrawlDbUpdater.class);
		registeredTools.put("LinkInverter", LinkInverter.class);
		registeredTools.put("PrintSegment", PrintSegment.class);
		registeredTools.put("PrintCrawlDb", PrintCrawlDb.class);
		registeredTools.put("PrintLinkDb", PrintLinkDb.class);
		registeredTools.put("Indexer", Indexer.class);
		registeredTools.put("Clusterer", Clusterer.class);
		registeredTools.put("Crawl", Crawl.class);
		registeredTools.put("ReCrawl", ReCrawl.class);
		registeredTools.put("LoadIndex", IndexLoader.class);
		registeredTools.put("CrawlAndUpdate", CrawlAndUpdate.class);
		registeredTools.put("Baseline", Baseline.class);
		registeredTools.put("BaselineFetcher", BaselineFetcher.class);
		registeredTools.put("ExtractSeedList", ExtractSeedList.class);
		registeredTools.put("FilterSeedList", FilterSeedList.class);
		registeredTools.put("InvertedIndex", InvertedIndex.class);
		registeredTools.put("MergeInvertedIndices", MergeInvertedIndices.class);
		registeredTools.put("MergeClusters", MergeClusters.class);
	}
		
	private Tool getToolInstance(String className) {
			if(registeredTools.containsKey(className)){
				Class c = registeredTools.get(className);
				Tool t = ReflectionUtils.newInstance(c, new Configuration());
				return t;
			}else{
				printUsage();
				return null;
			}
	}
		
	
	public static void printUsage() {
		Crawl.usage();
		ReCrawl.usage();
		CrawlAndUpdate.usage();
		Injector.usage();
		Generator.usage();
		SimpleFetcher.usage();
		DeltaFetcher.usage();
		CrawlDbUpdater.usage();
		LinkInverter.usage();
		PrintSegment.usage();
		PrintCrawlDb.usage();
		PrintLinkDb.usage();
		Indexer.usage();
		Clusterer.usage();
		IndexLoader.usage();
		Baseline.usage();
		BaselineFetcher.usage();
		ExtractSeedList.usage();
		FilterSeedList.usage();
		InvertedIndex.usage();
		MergeInvertedIndices.usage();
		MergeClusters.usage();
		System.exit(1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 1){
			printUsage();
		}
		Driver driver = new Driver();
		String className = args[0];
		Tool tool = driver.getToolInstance(className);
		
		String[] remainingArgs = new String[args.length-1];
		for (int i = 0; i < remainingArgs.length; i++) {
			remainingArgs[i] = args[i+1];
		}
		try {
			ToolRunner.run(tool, remainingArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}
