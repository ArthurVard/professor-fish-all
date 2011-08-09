package de.uniko.softlang.crawler.delta;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * Data store for an outlink to an other website, together with a sign that
 * indicates, whether the links was added or deleted. The link itself contains
 * of the URL of the target site, along with the anchor text associated with
 * that link.
 */
public class DeltaLink implements Writable, Comparable<DeltaLink> {

	Text url;
	BooleanWritable sign;
	Text anchor;

	public DeltaLink() {
	}

	public DeltaLink(Text url, BooleanWritable sign, Text anchor) {
		this.url = url;
		this.sign = sign;
		this.anchor = anchor;
	}

	public Text getUrl() {
		return url;
	}

	public void setUrl(Text url) {
		this.url = url;
	}

	public BooleanWritable getSign() {
		return sign;
	}

	public void setSign(BooleanWritable sign) {
		this.sign = sign;
	}

	public Text getAnchor() {
		return anchor;
	}

	public void setAnchor(Text anchor) {
		this.anchor = anchor;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		url.write(out);
		sign.write(out);
		anchor.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		url = new Text();
		url.readFields(in);
		sign = new BooleanWritable();
		sign.readFields(in);
		anchor = new Text();
		anchor.readFields(in);
	}

	@Override
	public int compareTo(DeltaLink that) {
		if (this.url.toString().compareTo(that.url.toString()) != 0)
			return this.url.compareTo(that.url);
		if (this.anchor.toString().compareTo(that.anchor.toString()) != 0)
			return this.anchor.compareTo(that.anchor);
		if (this.sign.get() != that.sign.get())
			return 1;
		return 0;
	}

	public String signToString() {
		return sign.get() ? "true" : "false";
	}

	public String prettyPrint() {
		return "<" + url + ", " + anchor + ", " + signToString() + "> ";
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (!(that instanceof DeltaLink))
			return false;
		DeltaLink thatLink = (DeltaLink) that;
		boolean result = hasEqualState(thatLink);
		return result;
	}

	private boolean hasEqualState(DeltaLink that) {
		return (this.url == null ? that.url == null : this.url.equals(that.url))
				&& (this.sign == null ? that.sign == null : this.sign.equals(that.sign))
				&& (this.anchor == null ? that.anchor == null : this.anchor
						.equals(that.anchor));
	}

	@Override
	public int hashCode() {
		final int fODD_PRIME_NUMBER = 37;
		int result = url.toString().hashCode();
		result = result * fODD_PRIME_NUMBER + sign.hashCode();
		result = result * fODD_PRIME_NUMBER + anchor.hashCode();
		return result;
	}
}
