package de.uniko.softlang.crawler.fetcher.nutch;


// JDK imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import de.uniko.softlang.crawler.fetcher.HttpFetch;


public class HttpResponse implements Response {

  private URL url;
  private byte[] content;
  private int code;
  private HashMap<String, String> headers =  new HashMap<String, String>();
  protected static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
  private static MultiThreadedHttpConnectionManager connectionManager =
    new MultiThreadedHttpConnectionManager();
  private static HttpClient client = new HttpClient(connectionManager);
  public static final int BUFFER_SIZE = 8 * 1024;
  protected int MAX_CONTENT = 64 * 1024; 

  /**
   * Fetches the given <code>url</code> and prepares HTTP response.
   *
   * @param http                An instance of the implementation class
   *                            of this plugin
   * @param url                 URL to be fetched
   * @param datum               Crawl data
   * @param followRedirects     Whether to follow redirects; follows
   *                            redirect if and only if this is true
   * @return                    HTTP response
   * @throws IOException        When an error occurs
   */
  public HttpResponse(URL url, long modifiedTime,
      boolean followRedirects) throws IOException {

    // Prepare GET method for HTTP request
    this.url = url;
    GetMethod get = new GetMethod(url.toString());
    get.setFollowRedirects(followRedirects);
    get.setDoAuthentication(true);
    if (modifiedTime > 0) {
      get.setRequestHeader("If-Modified-Since",
          format.format(new Date(modifiedTime)));
    }

    // Set HTTP parameters
    HttpMethodParams params = get.getParams();
    params.setVersion(HttpVersion.HTTP_1_0);
    
    params.makeLenient();
    params.setContentCharset("UTF-8");
    params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    params.setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
    // XXX (ab) not sure about this... the default is to retry 3 times; if
    // XXX the request body was sent the method is not retried, so there is
    // XXX little danger in retrying...
    // params.setParameter(HttpMethodParams.RETRY_HANDLER, null);
    try {
      code = getClient().executeMethod(get);

      Header[] heads = get.getResponseHeaders();

      for (int i = 0; i < heads.length; i++) {
        headers.put(heads[i].getName(), heads[i].getValue());
      }
      
      // Limit download size
      int contentLength = Integer.MAX_VALUE;
      String contentLengthString = headers.get(Response.CONTENT_LENGTH);
      if (contentLengthString != null) {
        try {
          contentLength = Integer.parseInt(contentLengthString.trim());
        } catch (NumberFormatException ex) {
          throw new HttpException("bad content length: " +
              contentLengthString);
        }
      }
      if (contentLength > MAX_CONTENT) {
        contentLength = MAX_CONTENT;
      }

      // always read content. Sometimes content is useful to find a cause
      // for error.
      InputStream in = get.getResponseBodyAsStream();
      try {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferFilled = 0;
        int totalRead = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((bufferFilled = in.read(buffer, 0, buffer.length)) != -1
            && totalRead < contentLength) {
          totalRead += bufferFilled;
          out.write(buffer, 0, bufferFilled);
        }

        content = out.toByteArray();
      } catch (Exception e) {
        if (code == 200) throw new IOException(e.toString());
        // for codes other than 200 OK, we are fine with empty content
      } finally {
        if (in != null) {
          in.close();
        }
        get.abort();
      }
      
      StringBuilder fetchTrace = null;
      if (HttpFetch.LOG.isTraceEnabled()) {
        // Trace message
        fetchTrace = new StringBuilder("url: " + url +
            "; status code: " + code +
            "; bytes received: " + content.length);
        if (getHeader(Response.CONTENT_LENGTH) != null)
          fetchTrace.append("; Content-Length: " +
              getHeader(Response.CONTENT_LENGTH));
        if (getHeader(Response.LOCATION) != null)
          fetchTrace.append("; Location: " + getHeader(Response.LOCATION));
      }
      // Extract gzip, x-gzip and deflate content
      if (content != null) {
        // check if we have to uncompress it
        String contentEncoding = headers.get(Response.CONTENT_ENCODING);
        if (contentEncoding != null){
        	//TODO handle encoded content
        	content = null;
        }
//        if (contentEncoding != null && HttpFetch.LOG.isTraceEnabled())
//          fetchTrace.append("; Content-Encoding: " + contentEncoding);
//        if ("gzip".equals(contentEncoding) ||
//            "x-gzip".equals(contentEncoding)) {
//          content = http.processGzipEncoded(content, url);
//          if (HttpFetch.LOG.isTraceEnabled())
//            fetchTrace.append("; extracted to " + content.length + " bytes");
//        } else if ("deflate".equals(contentEncoding)) {
//          content = http.processDeflateEncoded(content, url);
//          if (HttpFetch.LOG.isTraceEnabled())
//            fetchTrace.append("; extracted to " + content.length + " bytes");
//        }
      }

      // Log trace message
      if (HttpFetch.LOG.isTraceEnabled()) {
        HttpFetch.LOG.trace(fetchTrace);
      }
    } finally {
      get.releaseConnection();
    }
  }

  static synchronized HttpClient getClient() {
    return client;
  }
  
  /* ------------------------- *
   * <implementation:Response> *
   * ------------------------- */
  
  public URL getUrl() {
    return url;
  }
  
  public int getCode() {
    return code;
  }

  public String getHeader(String name) {
    return headers.get(name);
  }
  
  public HashMap<String,String> getHeaders() {
    return headers;
  }

  public byte[] getContent() {
    return content;
  }

  /* -------------------------- *
   * </implementation:Response> *
   * -------------------------- */
}


