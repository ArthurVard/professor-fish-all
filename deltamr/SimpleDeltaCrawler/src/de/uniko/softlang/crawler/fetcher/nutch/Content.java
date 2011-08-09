package de.uniko.softlang.crawler.fetcher.nutch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public final class Content implements Writable{
	
	
	byte[] data;
	ProtocolStatus status;
	
	public Content() {
    this.data = new byte[0];
  }
	
	public Content(byte[] data) {
    this.data = data;
  }
	
	public Content(byte[] data, ProtocolStatus status) {
    this.data = data;
    this.status = status;
  }
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ProtocolStatus getStatus() {
		return status;
	}

	public void setStatus(ProtocolStatus status) {
		this.status = status;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		status.write(out);
		out.writeInt(data.length); // write content
    out.write(data);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		status = new ProtocolStatus();
		status.readFields(in);
		data= new byte[in.readInt()];
    in.readFully(data);
	}
	

}
