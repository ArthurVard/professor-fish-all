package de.uniko.softlang.crawler.datastores;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;

import de.uniko.softlang.crawler.delta.DeltaLink;
import de.uniko.softlang.utils.Pair;
import de.uniko.softlang.utils.PairWritable;

/**
 * Data structure to store a version of the metadata of a website. The version
 * can be the full metadata, or a delta relative to earlier versions.
 * The metadata contains all (changed) outlinks to other sites.
 */
public class SiteMetadata extends VersionedSiteData {

	private DeltaLink[] links;
	private long fetchTime;
	private boolean modified;
	
	public SiteMetadata(){ }
	
	public SiteMetadata(Text url, int version, DeltaLink[] links){
		super(url, version);
		this.links = links;
	}
	
	public DeltaLink[] getLinks() {
		return links;
	}

	public void setLinks(DeltaLink[] links) {
		this.links = links;
	}
	
	public void setFetchTime(long fetchTime) {
		this.fetchTime = fetchTime;
	}

	public long getFetchTime() {
		return fetchTime;
	}
	
	public boolean getModified(){
		return modified;
	}
	
	public void setModified(boolean modified){
		this.modified = modified;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
//		super.write(out);
		url.write(out);
		out.writeInt(version);
		//--
		out.writeLong(fetchTime);
		out.writeBoolean(modified);
		out.writeInt(links.length);
		for(int i = 0; i < links.length; i++){
			links[i].write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
//		super.readFields(in);
		url = new Text();
		url.readFields(in);
		version = in.readInt();
		//--
		this.fetchTime = in.readLong();
		this.modified = in.readBoolean();
		int noLinks = in.readInt();
		links = new DeltaLink[noLinks];
		for (int i = 0; i < noLinks; i++) {
			DeltaLink link = new DeltaLink();
			link.readFields(in);
			links[i] = link;
		}
	}

	@Override
	public int compareTo(VersionedSiteData that) {
		int retVal = super.compareTo(that);
		if(retVal == 0){
			if(((SiteMetadata)that).fetchTime != this.fetchTime)
				return (((SiteMetadata)that).fetchTime - this.fetchTime) > 0 ? 1 : -1;
			if(((SiteMetadata)that).modified != this.modified)
				return this.modified ? 1 : -1;
			if(this.links.length != ((SiteMetadata)that).links.length){
				return this.links.length - ((SiteMetadata)that).links.length > 0 ? 1 : -1;
			}else{
				for(int i = 0; i < this.links.length; i++){
					if(this.links[i].compareTo(((SiteMetadata)that).links[i]) != 0){
						return this.links[i].compareTo(((SiteMetadata)that).links[i]) ;
					}
				}
			}
		}
		return retVal;
	}

	
	
	public Text prettyPrint() {
		String outString = "Version = " + version + ", FetchTime = " + fetchTime + ", links: ";
		for (int i = 0; i < links.length; i++) {
			outString = outString.concat("\n\t" + links[i].prettyPrint());
		}
		return new Text(outString);
	}

}
