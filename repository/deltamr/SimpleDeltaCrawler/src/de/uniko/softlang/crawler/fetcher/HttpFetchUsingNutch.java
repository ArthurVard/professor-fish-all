package de.uniko.softlang.crawler.fetcher;

import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.protocol.ProtocolOutput;
import org.apache.nutch.protocol.ProtocolStatus;
import org.apache.nutch.protocol.httpclient.Http;

public class HttpFetchUsingNutch {
	public static final Log LOG = LogFactory.getLog(HttpFetchUsingNutch.class);

	public ProtocolOutput fetchContentUsingNutchJar(Text url, long lastModified, Configuration conf) throws MalformedURLException{
		System.out.println(url);
		Http protocol = new Http();
		Configuration.addDefaultResource("nutch-default.xml");
		protocol.setConf(conf);
		protocol.getConf().set("mime.types.file", "tika-mimetypes.xml");
//		if(Thread.currentThread().getContextClassLoader().getResource("tika-mimetypes.xml") == null){
//			System.out.println("TIKA file not on classpath");
//			LOG.error("TIKA file not on classpath");
//		}else{
//			System.out.println("TIKA file is on classpath");
//			LOG.error("TIKA file is on classpath");
//		}
		ProtocolOutput output = protocol.getProtocolOutput(url, new CrawlDatum(0,0));
		return output;
	}
	
	public boolean shouldRefetch(ProtocolStatus status){
		switch(status.getCode()) {
    
    case ProtocolStatus.WOULDBLOCK:
      return true;
    case ProtocolStatus.SUCCESS:        // got a page
      return true;
    case ProtocolStatus.MOVED:         // redirect
    case ProtocolStatus.TEMP_MOVED:
    	//new URL is handled in Fetcher
      return false;
    case ProtocolStatus.EXCEPTION:
     return false;
    case ProtocolStatus.RETRY:          // retry
    case ProtocolStatus.BLOCKED:
    	return true;
    case ProtocolStatus.GONE:           // gone
    case ProtocolStatus.NOTFOUND:
    case ProtocolStatus.ACCESS_DENIED:
    case ProtocolStatus.ROBOTS_DENIED:
      return false;
     case ProtocolStatus.NOTMODIFIED:
      return true;
    default:
      if (LOG.isWarnEnabled()) {
        LOG.warn("Unknown ProtocolStatus: " + status.getCode());
      }
      return true;
    }
	}

	
}
