package de.uniko.softlang.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import de.uniko.softlang.crawler.Crawl;
import de.uniko.softlang.crawler.ReCrawl;

public class CrawlAndUpdate extends Configured implements Tool {

	public static void usage(){
		System.err.println("Usage: CrawlAndUpdate <base_dir> <url_dir> <numIterations> [prettyprint] [index] [useDeltas (wordLvl | docLvl)] [merge]");
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
			boolean merge = false;
			for (int i = 3; i < args.length; i++) {
				if(args[i].equals("prettyprint"))
					pretty = true;
				else if(args[i].equals("index"))
						index = true;
				else if(args[i].equals("merge"))
					merge = true;
				else if(args[i].equals("useDeltas")){
					useDeltas = true;
					if(args.length > i+1 && args[i+1].equals("wordLvl")){
						wordLvl = true;
						i++;
					}
				}
			}
			Crawl c = new Crawl();
			ReCrawl re = new ReCrawl();
			if(useDeltas){
				c.crawlWithDeltas(new Path(args[0]), new Path(args[1]), iterations, pretty, index, wordLvl);
				re.reCrawlWithDetlas(new Path(args[0]), 1, pretty, index, false, wordLvl, merge, 100);
			}else{
				c.crawlNoDeltas(new Path(args[0]), new Path(args[1]), iterations, pretty, index);
				re.reCrawlNoDetlas(new Path(args[0]), 1, pretty, index, false, merge, 100);
			}
			
			return 0;
		} catch (Exception e) {
			return -1;
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Crawl(), args);
		System.exit(res);
	}
}
