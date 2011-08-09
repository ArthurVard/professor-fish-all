package de.uniko.softlang.indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.mahout.math.VectorWritable;

import de.uniko.softlang.crawler.datastores.SimpleLink;
import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.datastores.SiteMetadata;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.utils.PairWritable;

public class DeltaVectorOutputFormant extends FileOutputFormat<Text, PairWritable<VectorWritable,BooleanWritable>>{

	protected SequenceFile.Writer getSequenceWriter(TaskAttemptContext context,Class<?> keyClass, Class<?> valueClass, String topDir) throws IOException{
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
	    	Path file = getDefaultWorkFile(context, topDir, "");
		    
		    FileSystem fs = file.getFileSystem(conf);
		    return SequenceFile.createWriter(fs, conf, file,
		             keyClass,
		             valueClass,
		             compressionType,
		             codec,
		             context);
	 }

	private class DeltaVectorRecordWriter extends RecordWriter<Text, PairWritable<VectorWritable, BooleanWritable>>{
		final SequenceFile.Writer changedOut;
		final SequenceFile.Writer fullOut;
		
		public DeltaVectorRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException{
			changedOut = getSequenceWriter(context, Text.class, VectorWritable.class, TermFreqVectorCreator.CHANGED_DIR);
			fullOut = getSequenceWriter(context, Text.class, VectorWritable.class, TermFreqVectorCreator.FULL_DIR);
		}

		@Override
		public void write(Text key,
				PairWritable<VectorWritable, BooleanWritable> value)
				throws IOException, InterruptedException {
			fullOut.append(key, value.getFirst());
			if(value.getSecond().get()){
				changedOut.append(key, value.getFirst());
			}
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException,
				InterruptedException {
			fullOut.close();
			changedOut.close();
		}
		
	}
	@Override
	public RecordWriter<Text, PairWritable<VectorWritable, BooleanWritable>> getRecordWriter(
			TaskAttemptContext context) throws IOException, InterruptedException {
		return new DeltaVectorRecordWriter(context);
	}

	public Path getDefaultWorkFile(TaskAttemptContext context, String topDir, String extension) throws IOException{
		FileOutputCommitter committer = (FileOutputCommitter) getOutputCommitter(context);
		return new Path(new Path(committer.getWorkPath(),topDir), getUniqueFile(context, getOutputName(context), extension));
	}

}
