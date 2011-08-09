package de.uniko.softlang.crawler;

import java.net.InetAddress;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * Partition urls by host, domain name or IP depending on the value of the
 * parameter 'partition.url.mode' which can be 'byHost', 'byDomain' or 'byIP'
 */
public class URLPartitioner extends Partitioner<Text,Writable> implements Configurable {
  private static final Log LOG = LogFactory.getLog(URLPartitioner.class);

  public static final String PARTITION_MODE_KEY = "partition.url.mode";

  public static final String PARTITION_MODE_HOST = "byHost";
  public static final String PARTITION_MODE_DOMAIN = "byDomain";
  public static final String PARTITION_MODE_IP = "byIP";

  private int seed;
  private String mode = PARTITION_MODE_HOST;

private Configuration conf;

   /** Hash by domain name. */
  public int getPartition(Text key, Writable value, int numReduceTasks) {
    String urlString = key.toString();
    URL url = null;
    int hashCode = urlString.hashCode();
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      LOG.warn("Malformed URL: '" + urlString + "'");
    }

    if (mode.equals(PARTITION_MODE_DOMAIN) && url != null){
    	//TODO
    	hashCode = url.getHost().hashCode();
  	}else if (mode.equals(PARTITION_MODE_IP)) {
      try {
        InetAddress address = InetAddress.getByName(url.getHost());
        hashCode = address.getHostAddress().hashCode();
      } catch (UnknownHostException e) {
        Generator.LOG.info("Couldn't find IP for host: " + url.getHost());
      }
    }else{
    	//PARTITION_MODE_HOST
    	hashCode = url.getHost().hashCode();
    }

    // make hosts wind up in different partitions on different runs
    hashCode ^= seed;

    return (hashCode & Integer.MAX_VALUE) % numReduceTasks;
  }

  @Override
  public void setConf(Configuration conf) {
	this.conf = conf;
	seed = conf.getInt("partition.url.seed", 0);
    mode = conf.get(PARTITION_MODE_KEY, PARTITION_MODE_HOST);
    // check that the mode is known
    if (!mode.equals(PARTITION_MODE_IP) && !mode.equals(PARTITION_MODE_DOMAIN)
        && !mode.equals(PARTITION_MODE_HOST)) {
      LOG.error("Unknown partition mode : " + mode + " - forcing to byHost");
      mode = PARTITION_MODE_HOST;
    }
	
  }

  @Override
  public Configuration getConf() {
	return conf;
  }

}
