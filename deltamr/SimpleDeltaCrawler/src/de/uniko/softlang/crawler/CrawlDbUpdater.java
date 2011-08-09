package de.uniko.softlang.crawler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.datastores.CrawlDbData;
import de.uniko.softlang.crawler.datastores.SimpleLink;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
/**
 * Tool to update the <code>CrawlDb</code> with data accumulated in a fetch step.
 */
public class CrawlDbUpdater extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(CrawlDbUpdater.class);
	public static final int DEFAULT_INTERVAL = 0;
	public static final int BLOCKED = Integer.MIN_VALUE+2;
	

	public static class UpdateMapper extends
			Mapper<Text, VersionedSiteData, Text, VersionedSiteData> {
		protected void map(Text key, VersionedSiteData value, Context context)
				throws IOException, InterruptedException {
			context.write(key, new VersionedSiteData(value));
		}
	}

	public static class UpdateReducer extends
			Reducer<Text, VersionedSiteData, Text, CrawlDbData> {

		protected void reduce(Text key, Iterable<VersionedSiteData> values,
				Context context) throws IOException, InterruptedException {
			CrawlDbData dbEntry = null;
			SiteMetadata data = null;
			SimpleLink link = null;

			for (VersionedSiteData value : values) {
				final Writable val = value.get(); // unwrap
				if (val instanceof CrawlDbData) {
					dbEntry = (CrawlDbData) val;
					if(dbEntry.getStatus() == CrawlDbData.SiteStatus.BLOCKED){
						//dismiss site; it was previously associated with an error
						context.write(key, dbEntry);
						return;
					}
				} else if (val instanceof SiteMetadata) {
					if(((SiteMetadata) val).getVersion() == BLOCKED){
						//An exception occured during fetching of this site. 
						//Site was marked as not to fetch anymore
						CrawlDbData blockedSite = new CrawlDbData(key, -1, DEFAULT_INTERVAL);
						blockedSite.setStatus(CrawlDbData.SiteStatus.BLOCKED);
						context.write(key, blockedSite);
						return;
					}
					if (data != null) {
						// one is probably a link
						if (data.getVersion() == -1) {
							data = (SiteMetadata) val;
						} else if (((SiteMetadata) val).getVersion() == -1) {
							continue;
						} else {
							LOG.warn("Unexpected: Two SiteMetadata objects for the same URL:"
									+ key + " (none is a link)");
							// take the data with the larger version
							data = data.getVersion() > ((SiteMetadata) val).getVersion() ? data
									: ((SiteMetadata) val);
						}

					} else {
						data = (SiteMetadata) val;
					}
				} else if (val instanceof SimpleLink) {
					link = (SimpleLink) val;
				} else {
					LOG.warn("Unexpected input type for key " + key);
				}
			}

			if (dbEntry == null) {
				if (link != null) {
					if (data != null) {
						LOG.error("Unexpected error: SiteMetadata but no db-entry available for url "
								+ key);
						return;
					} else {
						// it's a newly discovered site
						context.write(key, new CrawlDbData(key, -1, DEFAULT_INTERVAL));
					}
				} else {
					if (data != null) {
						LOG.error("Unexpected error: SiteMetadata but no db-entry available for url "
								+ key);
						return;
					} else {
						// only unexpected input
						LOG.warn("No valid input");
						return;
					}
				}
			} else if (data == null) {
				// entry was not fetched
				context.write(key, dbEntry);
			} else {
				// update dbEntry with data if data is not a link
				if (data.getVersion() != -1) {
					dbEntry.update(data);
				}
				context.write(key, dbEntry);
			}
		}
	}

	public CrawlDbUpdater() { }

	public CrawlDbUpdater(Configuration conf) {
		setConf(conf);
	}
	public boolean update(Path crawlDb, Path segmentBase, String segmentNo)
			throws Exception {
		if(getConf() == null)
			setConf(new Configuration());
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("CrawlDbUpdater");
		job.setJarByClass(CrawlDbUpdater.class);
		job.setMapperClass(UpdateMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(VersionedSiteData.class);
		job.setReducerClass(UpdateReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		
		// input
		Path oldDb = crawlDb;
		Path segmentDir = new Path(segmentBase, DeltaFetcher.DATA_DIR + "/"
				+ segmentNo);
		SequenceFileInputFormat.addInputPath(job, new Path(segmentDir,
				DeltaFetcher.META_DIR));
		SequenceFileInputFormat.addInputPath(job, new Path(segmentDir,
				DeltaFetcher.LINK_DIR));
		SequenceFileInputFormat.addInputPath(job, oldDb);
		job.setInputFormatClass(SequenceFileInputFormat.class);

		// out
		Path newDb = new Path(crawlDb.toString() + "-" + System.currentTimeMillis());
		SequenceFileOutputFormat.setOutputPath(job, newDb);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(CrawlDbData.class);

		boolean success = job.waitForCompletion(true);

		if (success) {
			// replace old crawlDb with new one
			FileSystem fs = oldDb.getFileSystem(getConf());
			fs.delete(oldDb, true);
			fs.rename(newDb, oldDb);

			// remove generate folder
			fs.delete(new Path(segmentBase, Generator.GENERATOR_DIR), true);
		}
		return success;

	}

	public static void usage(){
		System.err.println("Usage: CrawlDbUpdater <crawldb> <base_dir> <segment_no>");
	}
	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			update(new Path(args[0]), new Path(args[1]), args[2]);
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleCrawlDbUpdate: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new CrawlDbUpdater(), args);
		System.exit(res);
	}

}
