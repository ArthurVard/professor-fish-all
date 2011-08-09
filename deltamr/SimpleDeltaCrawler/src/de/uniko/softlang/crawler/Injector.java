package de.uniko.softlang.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.datastores.CrawlDbData;

/**
 * Inititalize the <code>CrawlDb</code> with a set of seed urls.
 */
public class Injector extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Injector.class);
	public static final String CRAWL_DB = "crawlDb";
	
	
	public static class InjectMapper extends Mapper<Object, Text,Text, CrawlDbData>{
		private int fetchInterval;
		private Text urlKey;
		
		protected void setup(Context context) throws IOException, InterruptedException {
			this.fetchInterval = context.getConfiguration().getInt("db.fetch.interval.default", 0);	//2592000 = 30days
			this.urlKey = new Text();
		}
		
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			 String url = value.toString();
			 
			 if (url != null && url.trim().startsWith("#")) {
		          /* Ignore line that start with # */
		          return;
		      }
			 try{
				 URL u = new URL(url);
				 
			 }catch(MalformedURLException e){
				 LOG.warn("Not a valid URL: " + value);
				 return;
			 }
			 CrawlDbData site = new CrawlDbData(new Text(url),-2, fetchInterval);
			 urlKey.set(url);
			 context.write(urlKey, site);

		 }
	}
	
	public static class InjectReducer extends Reducer<Text, CrawlDbData, Text, CrawlDbData>{
		protected void reduce(Text key, Iterable<CrawlDbData> values, Context context) throws IOException, InterruptedException {
			for(CrawlDbData value: values) {
				context.write(key, value);
				return;		//eliminate duplicates
			}
		}	
	}
	
	public Injector() { }

	public Injector(Configuration conf) {
		setConf(conf);
	}
	  
	public boolean inject(Path baseDir, Path urlDir) throws Exception {
		if (getConf() == null)
			setConf(new Configuration());
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("Injector");
		job.setJarByClass(Injector.class);
		job.setMapperClass(InjectMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(CrawlDbData.class);
		job.setReducerClass(InjectReducer.class);
		job.setNumReduceTasks(Crawl.NUM_REDUCE);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(CrawlDbData.class);
		TextInputFormat.addInputPath(job, urlDir);
		SequenceFileOutputFormat.setOutputPath(job, new Path(baseDir, CRAWL_DB));
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job.waitForCompletion(true);
	}

	public static void usage(){
		System.err.println("Usage: Injector <crawldb> <url_dir>");
  }
	public int run(String[] args) throws Exception {
	    if (args.length < 2) {
	      usage();
	    	return -1;
	    }
	    try {
	      inject(new Path(args[0]), new Path(args[1]));
	      return 0;
	    } catch (Exception e) {
	      LOG.fatal("SimpleInjector: " + StringUtils.stringifyException(e));
	      return -1;
	    }
	  }
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Injector(), args);
	    System.exit(res);
	}

}
