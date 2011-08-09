package de.uniko.softlang.indexer;

/**
 * Container for a candidate for a given search query.
 */
public class DocumentMapEntry {

	private String url;
	private long segmentNo;
	int clusterMembership;
	
	public DocumentMapEntry(String url, long segmentNo, int clusterMembership){
		this.url = url;
		this.segmentNo = segmentNo;
		this.clusterMembership = clusterMembership;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSegmentNo() {
		return segmentNo;
	}

	public void setSegmentNo(long segmentNo) {
		this.segmentNo = segmentNo;
	}

	public int getClusterMembership() {
		return clusterMembership;
	}

	public void setClusterMembership(int clusterMembership) {
		this.clusterMembership = clusterMembership;
	}
}
