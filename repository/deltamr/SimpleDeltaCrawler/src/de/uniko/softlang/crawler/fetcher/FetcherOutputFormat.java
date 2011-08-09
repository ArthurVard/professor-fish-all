package de.uniko.softlang.crawler.fetcher;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import de.uniko.softlang.crawler.datastores.SimpleLink;
import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.datastores.SiteVersion;
import de.uniko.softlang.crawler.datastores.VersionedSiteData;

public class FetcherOutputFormat extends FileOutputFormat<Text, SiteVersion>{
	
	 protected SequenceFile.Writer getSequenceWriter(TaskAttemptContext context,Class<?> keyClass, Class<?> valueClass) throws IOException{
		 Configuration conf = context.getConfiguration();
		    
		    CompressionCodec codec = null;
		    CompressionType compressionType = CompressionType.NONE;
		    if (getCompressOutput(context)) {
		      // find the kind of compression to do
		      compressionType = SequenceFileOutputFormat.getOutputCompressionType(context);
		      // find the right codec
		      Class<?> codecClass = getOutputCompressorClass(context, 
		                                                     DefaultCodec.class);
		      codec = (CompressionCodec) 
		        ReflectionUtils.newInstance(codecClass, conf);
		    }
		    // get the path of the temporary output file 
	    	 String topDir = "";
	    	 if(valueClass.equals(SiteContent.class))
		    	topDir = SimpleFetcher.CONTENT_DIR;
	    	 else if(valueClass.equals(SiteMetadata.class))
	 		     topDir = SimpleFetcher.META_DIR;
	    	 else if(valueClass.equals(SimpleLink.class))
	 		     topDir = SimpleFetcher.LINK_DIR;
	    	 else{
	    		 try{throw new Exception("invalid value type in BruteFetcherOutputFormat");}catch(Exception e){e.printStackTrace();}
	    	 }
	    	 Path file = getDefaultWorkFile(context, topDir, "");
		    
		    FileSystem fs = file.getFileSystem(conf);
		    return SequenceFile.createWriter(fs, conf, file,
		             keyClass,
		             valueClass,
		             compressionType,
		             codec,
		             context);
	 }

	private class BruteFetcherRecordWriter extends RecordWriter<Text, SiteVersion>{
		final SequenceFile.Writer contentOut;
		final SequenceFile.Writer metaOut;
		final SequenceFile.Writer linkOut;
		
		public BruteFetcherRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException{
			contentOut = getSequenceWriter(context, Text.class, SiteContent.class);
			metaOut = getSequenceWriter(context, Text.class, SiteMetadata.class);
			linkOut = getSequenceWriter(context, Text.class, SimpleLink.class);
		}

		@Override
		public void write(Text key, SiteVersion value)
				throws IOException, InterruptedException {
			if(value.getMeta() != null){ 
				if(value.getMeta() instanceof SiteMetadata)
					metaOut.append(key, value.getMeta());
				else if(value.getMeta() instanceof SimpleLink)
					linkOut.append(key, value.getMeta());
			}
			if(value.getContent() != null && !value.getContent().getDelta().isEmpty())
				contentOut.append(key, value.getContent());
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException,
				InterruptedException {
			contentOut.close();
			metaOut.close();
			linkOut.close();
		}
	}
	@Override
	public RecordWriter<Text, SiteVersion> getRecordWriter(
			TaskAttemptContext context) throws IOException, InterruptedException {
		
		return new BruteFetcherRecordWriter(context);
	}

	public Path getDefaultWorkFile(TaskAttemptContext context, String topDir, String extension) throws IOException{
		FileOutputCommitter committer = (FileOutputCommitter) getOutputCommitter(context);
		return new Path(new Path(committer.getWorkPath(),topDir), getUniqueFile(context, getOutputName(context), extension));
	}
	
}
