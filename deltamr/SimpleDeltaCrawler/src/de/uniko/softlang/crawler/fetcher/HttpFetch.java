package de.uniko.softlang.crawler.fetcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.protocol.ProtocolOutput;

import de.uniko.softlang.crawler.fetcher.nutch.Content;
import de.uniko.softlang.crawler.fetcher.nutch.HttpResponse;
import de.uniko.softlang.crawler.fetcher.nutch.ProtocolStatus;
import de.uniko.softlang.crawler.fetcher.nutch.Response;

public class HttpFetch {
	public static final Log LOG = LogFactory.getLog(HttpFetch.class);
//	private ProtocolFactory protocolFactory;
	private Configuration conf;
	
	public HttpFetch(Configuration conf){
//		this.protocolFactory = new ProtocolFactory(conf);
		this.conf = conf;
	}
	
	public String fetchContent(Text url){
		try {
			URL u = new URL(url.toString());
			//TODO check robots.txt
			 if (!"http".equals(u.getProtocol())){
				 LOG.error("Not a valid URL: " + url);
				 return "";
			 }
			 if (LOG.isTraceEnabled()) {
			     LOG.trace("fetching " + url);
			 }
			 
			 BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
			 String inputLine;
			 StringBuilder content = new StringBuilder();
 			 while ((inputLine = in.readLine()) != null)
				 content.append(inputLine);
			 in.close();
			 return content.toString();
		} catch (Exception e) {
			return "";
		}
		
	}
	
	public Content fetchContentUsingNutch(Text url, long lastModified) throws MalformedURLException{
		String urlString = url.toString();
    try {
      URL u = new URL(urlString);
      
      
      String host = null;
      
      Response response;
      response = new HttpResponse(u, lastModified, false); // make a request
     
      int code = response.getCode();
      byte[] content = response.getContent();
      Content c = new Content(content);
      
      if (code == 200) { // got a good response
        return c; // return it
        
      } else if (code == 410) { // page is gone
      	ProtocolStatus status = new ProtocolStatus(ProtocolStatus.GONE, "Http: " + code + " url=" + url);
      	c.setStatus(status);
        return c;
        
      } else if (code >= 300 && code < 400) { // handle redirect
        String location = response.getHeader("Location");
        // some broken servers, such as MS IIS, use lowercase header name...
        if (location == null) location = response.getHeader("location");
        if (location == null) location = "";
        u = new URL(u, location);
        int protocolStatusCode;
        switch (code) {
          case 300:   // multiple choices, preferred value in Location
            protocolStatusCode = ProtocolStatus.MOVED;
            break;
          case 301:   // moved permanently
          case 305:   // use proxy (Location is URL of proxy)
            protocolStatusCode = ProtocolStatus.MOVED;
            break;
          case 302:   // found (temporarily moved)
          case 303:   // see other (redirect after POST)
          case 307:   // temporary redirect
            protocolStatusCode = ProtocolStatus.TEMP_MOVED;
            break;
          case 304:   // not modified
            protocolStatusCode = ProtocolStatus.NOTMODIFIED;
            break;
          default:
            protocolStatusCode = ProtocolStatus.MOVED;
        }
        // handle this in the higher layer.
        c.setStatus(new ProtocolStatus(protocolStatusCode, u));
        return c;
      } else if (code == 400) { // bad request, mark as GONE
        if (LOG.isTraceEnabled()) { LOG.trace("400 Bad request: " + u); }
        c.setStatus(new ProtocolStatus(ProtocolStatus.GONE, u));
        return c;
      } else if (code == 401) { // requires authorization, but no valid auth provided.
        if (LOG.isTraceEnabled()) { LOG.trace("401 Authentication Required"); }
        c.setStatus(new ProtocolStatus(ProtocolStatus.ACCESS_DENIED, "Authentication required: "+ urlString));
        return c;
      } else if (code == 404) {
        c.setStatus(new ProtocolStatus(ProtocolStatus.NOTFOUND, u));
      	return c;
      } else if (code == 410) { // permanently GONE
      	c.setStatus(new ProtocolStatus(ProtocolStatus.GONE, u));
        return c;
      } else {
      	c.setStatus(new ProtocolStatus(ProtocolStatus.EXCEPTION, "Http code=" + code + ", url=" + u));
        return c;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return new Content(new byte[0], new ProtocolStatus(e));
    }
	}
	
//	public String fetchContentUsingNutch(Text url, long lastModified) throws ProtocolNotFound, MalformedURLException{
//		Protocol protocol = this.protocolFactory.getProtocol(url.toString());
//		
//    RobotRules rules = protocol.getRobotRules(url, null);
//    if (!rules.isAllowed(new URL(url.toString()))) {
//      // unblock
//      return "";
//    }
//    CrawlDatum date = new CrawlDatum();
//    date.setModifiedTime(lastModified);
//    ProtocolOutput output = protocol.getProtocolOutput(url, date);
//    ProtocolStatus status = output.getStatus();
//    Content content = output.getContent();
//		return new String(content.getContent());
//	}
}
