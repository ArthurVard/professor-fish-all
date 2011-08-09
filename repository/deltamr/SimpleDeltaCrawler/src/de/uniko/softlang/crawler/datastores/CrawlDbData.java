package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * Class to store metadata of a website inside <code>CrawlDb</code>.
 */
public class CrawlDbData extends VersionedSiteData {

	public static final class SiteStatus {
		public static final byte INJECTED = 0x01;
		public static final byte UNFETCHED = 0x02;
		public static final byte FETCHED = 0x03;
		public static final byte UNAVAILABLE = 0x04;
		public static final byte BLOCKED = 0x04;

		public static String returnString(byte status) {
			if (status == INJECTED)
				return "INJECTED";
			if (status == UNFETCHED)
				return "UNFETCHED";
			if (status == FETCHED)
				return "FETCHED";
			if (status == UNAVAILABLE)
				return "UNAVAILABLE";
			if (status == BLOCKED)
				return "BLOCKED";
			else
				return "";
		}

	}

	private byte status;
	private int fetchInterval;
	private long lastFetched = Long.MAX_VALUE;
	private long lastModified = Long.MIN_VALUE;
	private long fullSegment = -1;
	private LinkedList<Long> deltaSegments;

	public CrawlDbData() {
		this.deltaSegments = new LinkedList<Long>();
	}

	public CrawlDbData(Text url, int version, int fetchInterval) {
		super(url, version);
		this.fetchInterval = fetchInterval;
		this.status = SiteStatus.INJECTED;
		this.fullSegment = -1;
		this.deltaSegments = new LinkedList<Long>();
	}

	public CrawlDbData(Text url, int version, int fetchInterval, byte status) {
		super(url, version);
		this.fetchInterval = fetchInterval;
		this.status = status;
		this.fullSegment = -1;
		this.deltaSegments = new LinkedList<Long>();
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public int getFetchInterval() {
		return fetchInterval;
	}

	public void setFetchInterval(int fetchInterval) {
		this.fetchInterval = fetchInterval;
	}

	public long getLastFetched() {
		return lastFetched;
	}

	public void setLastFetched(long lastFetched) {
		this.lastFetched = lastFetched;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getFullSegment() {
		return fullSegment;
	}

	public void setFullSegment(long fullSegment) {
		this.fullSegment = fullSegment;
	}

	public LinkedList<Long> getDeltaSegments() {
		return deltaSegments;
	}

	public void addDeltaSegment(long t) {
		deltaSegments.add(t);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// super.write(out);
		url.write(out);
		out.writeInt(version);
		// --
		out.writeByte(status);
		out.writeInt(fetchInterval);
		out.writeLong(lastFetched);
		out.writeLong(lastModified);
		out.writeLong(fullSegment);
		out.writeInt(deltaSegments.size());
		for (long seg : deltaSegments) {
			out.writeLong(seg);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// super.readFields(in);
		url = new Text();
		url.readFields(in);
		version = in.readInt();
		// --

		status = in.readByte();
		fetchInterval = in.readInt();
		lastFetched = in.readLong();
		lastModified = in.readLong();
		fullSegment = in.readLong();
		int numDeltaSegments = in.readInt();
		deltaSegments = new LinkedList<Long>();
		for (int i = 0; i < numDeltaSegments; i++) {
			deltaSegments.add(in.readLong());
		}
	}

	@Override
	public int compareTo(VersionedSiteData other) {
		int retVal = super.compareTo(other);
		if (retVal != 0)
			return retVal;
		if (!(other instanceof CrawlDbData))
			return 1;
		CrawlDbData that = (CrawlDbData) other;
		if (that.status != this.status)
			return (that.status - this.status) > 0 ? 1 : -1;
		if (that.url.compareTo(this.url) != 0)
			return that.url.compareTo(this.url);
		if (that.fetchInterval != this.fetchInterval)
			return (that.fetchInterval - this.fetchInterval) > 0 ? 1 : -1;
		if (that.lastFetched != this.lastFetched)
			return (that.lastFetched - this.lastFetched) > 0 ? 1 : -1;
		if (that.lastModified != this.lastModified)
			return (that.lastModified - this.lastModified) > 0 ? 1 : -1;
		if (that.fullSegment != this.fullSegment)
			return (that.fullSegment - this.fullSegment) > 0 ? 1 : -1;
		if (that.deltaSegments.size() != this.deltaSegments.size())
			return (that.deltaSegments.size() - this.deltaSegments.size()) > 0 ? 1
					: -1;
		for (int i = 0; i < this.deltaSegments.size(); i++) {
			if (that.deltaSegments.get(i) != this.deltaSegments.get(i))
				return (that.deltaSegments.get(i) - this.deltaSegments.get(i)) > 0 ? 1
						: -1;
		}
		return 0;
	}

	public void update(SiteMetadata data) {
		this.status = SiteStatus.FETCHED;
		this.lastFetched = data.getFetchTime();
		this.lastModified = data.getModified() ? data.getFetchTime() : this
				.getLastModified();
		this.version = data.getVersion();
		// updateFetchInterval();
	}

	public Text prettyPrint() {
		String outString = "Status = " + SiteStatus.returnString(status)
				+ ", FetchInterval = " + fetchInterval + ", LastFetched = "
				+ lastFetched + ", " + ", LastModified = " + lastModified + ", ";
		if (fullSegment != -1)
			outString.concat(", FullSegment = " + fullSegment);
		if (deltaSegments.size() > 0)
			outString.concat(", DeltaSegments = ");
		for (long seg : deltaSegments) {
			outString.concat(seg + " - ");
		}
		return new Text(outString);
	}

}
