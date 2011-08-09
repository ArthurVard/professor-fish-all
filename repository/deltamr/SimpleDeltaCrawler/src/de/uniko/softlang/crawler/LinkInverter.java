package de.uniko.softlang.crawler;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.datastores.LinkMap;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.utils.PairWritable;

/**
 * Tool to use a specified set of website metadata to create a version of a
 * <code>LinkDb</code>, that maps a URL to associated inlinks.
 */
public class LinkInverter extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(LinkInverter.class);
	public static final String LINKDB = "linkDb";
	public static final String LINKDB_BASE = "linkDb-";
	public static final String COMPLETE_DB = "complete";

	public static class InvertMapper extends
			Mapper<Text, SiteMetadata, Text, LinkMap> {
		protected void map(Text key, SiteMetadata value, Context context)
				throws IOException, InterruptedException {
			for (int i = 0; i < value.getLinks().length; i++) {
				LinkMap links = new LinkMap();
				links.add(new DeltaLink(key, value.getLinks()[i].getSign(), value
						.getLinks()[i].getAnchor()));
				context.write(value.getLinks()[i].getUrl(), links);
			}
		}
	}

	public static class InvertReducer extends
			Reducer<Text, LinkMap, Text, LinkMap> {
		protected void reduce(Text key, Iterable<LinkMap> values, Context context)
				throws IOException, InterruptedException {
			LinkMap result = new LinkMap();

			for (LinkMap value : values) {
				Iterator<DeltaLink> it = value.iterator();

				while (it.hasNext()) {
					result.add(it.next());
				}
			}

			if (result.size() == 0)
				return;
			context.write(key, result);
		}
	}

	public LinkInverter() {
	}

	public LinkInverter(Configuration conf) {
		setConf(conf);
	}

	public Path invert(Path linkDb, Path[] segmentDirs) throws Exception {
		if (getConf() == null)
			setConf(new Configuration());
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("LinkInverter");
		job.setJarByClass(LinkInverter.class);
		job.setMapperClass(InvertMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LinkMap.class);
		job.setCombinerClass(InvertReducer.class);
		job.setReducerClass(InvertReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LinkMap.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);

		for (int i = 0; i < segmentDirs.length; i++) {
			SequenceFileInputFormat.addInputPath(job, new Path(segmentDirs[i],
					DeltaFetcher.META_DIR));
		}
		job.setInputFormatClass(SequenceFileInputFormat.class);

		FileSystem fs = linkDb.getFileSystem(getConf());
		int maxNo = -1;
		if (fs.exists(linkDb)) {
			FileStatus[] otherLinkDbs = fs.listStatus(linkDb);
			for (int i = 0; i < otherLinkDbs.length; i++) {
				// find the existing LinkDb with the highest version number
				String dbName = otherLinkDbs[i].getPath().getName();
				if (dbName.endsWith(COMPLETE_DB))
					continue;
				int dbNo = Integer
						.parseInt(dbName.substring(dbName.lastIndexOf('-') + 1));
				if (dbNo > maxNo)
					maxNo = dbNo;
			}
		}
		String newDbName = LINKDB_BASE;
		newDbName = newDbName.concat("" + (++maxNo));
		Path output = new Path(linkDb, newDbName);
		SequenceFileOutputFormat.setOutputPath(job, output);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		if (job.waitForCompletion(true))
			return output;
		else
			return null;
	}

	public static void usage() {
		System.err
				.println("Usage: LinkInverter <linkdb> <segment_dir> [<segment_dir>]*");
	}

	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return -1;
		}
		try {
			Path[] segments = new Path[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				segments[i - 1] = new Path(args[i]);
			}
			invert(new Path(args[0]), segments);
			return 0;
		} catch (Exception e) {
			LOG.fatal("SimpleInvert: " + StringUtils.stringifyException(e));
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new LinkInverter(), args);
		System.exit(res);
	}

}
