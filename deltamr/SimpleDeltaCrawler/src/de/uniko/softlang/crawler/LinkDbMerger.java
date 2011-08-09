package de.uniko.softlang.crawler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.LinkInverter.InvertMapper;
import de.uniko.softlang.crawler.LinkInverter.InvertReducer;
import de.uniko.softlang.crawler.datastores.LinkMap;

/**
 * Tool to merge multiple <code>LinkDb</code>s.
 */
public class LinkDbMerger extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(LinkDbMerger.class);
	
	public boolean merge(Path linkDb) throws Exception{
		Configuration conf = new Configuration();
		setConf(conf);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
	  job.setJarByClass(LinkDbMerger.class);
	  job.setMapOutputKeyClass(Text.class);
	  job.setMapOutputValueClass(LinkMap.class);
//	  job.setCombinerClass(LinkInverter.InvertReducer.class);
	  job.setReducerClass(LinkInverter.InvertReducer.class);
	  job.setOutputKeyClass(Text.class);
	  job.setOutputValueClass(LinkMap.class);
	  job.setNumReduceTasks(Crawl.NUM_REDUCE);

	  FileSystem fs = linkDb.getFileSystem(getConf());
	  FileStatus[] linkDbs= fs.listStatus(linkDb);
	  for (int i = 0; i < linkDbs.length; i++) {
		  SequenceFileInputFormat.addInputPath(job, linkDbs[i].getPath());
		}
	  job.setInputFormatClass(SequenceFileInputFormat.class);
	  String tmpDbName = LinkInverter.LINKDB_BASE;
	  tmpDbName = tmpDbName.concat("" + System.currentTimeMillis());
	  Path tmpDb = new Path(linkDb, tmpDbName);
	  SequenceFileOutputFormat.setOutputPath(job, tmpDb);
	  job.setOutputFormatClass(SequenceFileOutputFormat.class);
	  boolean success = job.waitForCompletion(true);
	  
	  if(success){
	  	Path completeDb = new Path(linkDb, LinkInverter.LINKDB_BASE + LinkInverter.COMPLETE_DB);
	  	fs.delete(completeDb, true);
	  	fs.rename(tmpDb, completeDb);
	  }
	  return success;
	}
	
	public int run(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: LinkDbMerger <linkdb>");
      return -1;
    }	
    try {
    	merge(new Path(args[0]));
      return 0;
    } catch (Exception e) {
      LOG.fatal("SimpleInvert: " + StringUtils.stringifyException(e));
      return -1;
    }
  }

public static void main(String[] args) throws Exception {
	int res = ToolRunner.run(new Configuration(), new LinkDbMerger(), args);
    System.exit(res);
}
}
