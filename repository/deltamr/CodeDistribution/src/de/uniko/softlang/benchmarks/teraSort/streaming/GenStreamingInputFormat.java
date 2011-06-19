package de.uniko.softlang.benchmarks.teraSort.streaming;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;

import de.uniko.softlang.benchmarks.teraSort.generate.GenerateDelta;
import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;
import de.uniko.softlang.utils.PairWritable;

public class GenStreamingInputFormat extends FileInputFormat<LongWritable, PairWritable<Text, Text>>{
	public static final String DELTA_FILES_SET = "uni-ko.delta.files";
	private FSDataInputStream in;

	@Override
	public RecordReader<LongWritable, PairWritable<Text, Text>> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException {
		String fileName = ((FileSplit)split).getPath().toString();	
		boolean deltaFile;
		if(isDeltaFile(context.getConfiguration().getStringCollection(DELTA_FILES_SET),fileName))
			deltaFile = true;
		else
			deltaFile = false;
		return new WrappingRecordReader(deltaFile);
	}

	/*
	 * Need to check for substrings, as {@link DELTA_FILES_SET} will typically contain directories,
	 * while the passed filename will be some file in that directory (typically named 'part-xxxx').
	 */
	private boolean isDeltaFile(Collection<String> stringCollection,
			String fileName) {
		for(String s : stringCollection){
			if(fileName.indexOf(s) != -1){
				return true;
			}
		}
		return false;
	}

	private class WrappingRecordReader extends RecordReader<LongWritable, PairWritable<Text, Text>>{
		public static final int KEY_LENGTH = 10;
		public static final int VALUE_LENGTH = 90;
		public  int RECORD_LENGTH;
		public int DELTA_SIGN_LENGTH;


		private boolean deltaFile;
		private FSDataInputStream in;
		private long offset;
		private long length;
		private byte[] buffer;
		private LongWritable key;
		private Text tmp = new Text();
		private PairWritable<Text, Text> value;

		public WrappingRecordReader(boolean deltaFile) {
			this.deltaFile = deltaFile;
			DELTA_SIGN_LENGTH = GenerateDelta.POS_STR.length();
			if(deltaFile)
				RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH + DELTA_SIGN_LENGTH;
			else
				RECORD_LENGTH = KEY_LENGTH + VALUE_LENGTH;
;			buffer = new byte[RECORD_LENGTH];
		}

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
		throws IOException, InterruptedException {
			Path p = ((FileSplit)split).getPath();
			FileSystem fs = p.getFileSystem(context.getConfiguration());
			in = fs.open(p);
			long start = ((FileSplit)split).getStart();
			// find the offset to start at a record boundary
			offset = (RECORD_LENGTH - (start % RECORD_LENGTH)) % RECORD_LENGTH;
			in.seek(start + offset);
			length = ((FileSplit)split).getLength();
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if(key == null){
				key = new LongWritable();
				value = new PairWritable<Text, Text>();
			}
			if (offset >= length) {
				return false;
			}
			int read = 0;
			while (read < RECORD_LENGTH) {
				long newRead = in.read(buffer, read, (RECORD_LENGTH) - read);
				if (newRead == -1) {
					if (read == 0) {
						return false;
					} else {
						throw new EOFException("read past eof");
					}
				}
				read += newRead;
			}
			tmp.set(buffer, 0, KEY_LENGTH);
			key.set(KVGenerator.bytesToLong(tmp.getBytes()));
			Text valueBytes = new Text();
			valueBytes.set(buffer, KEY_LENGTH, VALUE_LENGTH);
			value.setFirst(valueBytes);
			Text valueSign = new Text();
			
			if(deltaFile){
				//read the sign from the file
				valueSign.set(buffer, KEY_LENGTH+VALUE_LENGTH, DELTA_SIGN_LENGTH);
			}else{
				//this is the orignal version -> no sign was written however it is always positive
				valueSign.set(new Text(GenerateDelta.POS_STR));
			}
			
			value.setSecond(valueSign);
			offset += RECORD_LENGTH;
			return true;
		}	


		@Override
		public LongWritable getCurrentKey() throws IOException,
		InterruptedException {
			return key;
		}

		@Override
		public PairWritable<Text, Text> getCurrentValue() throws IOException,
		InterruptedException {
			return value;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return (float) offset / length;
		}

		@Override
		public void close() throws IOException {
			in.close();
		}
	}


}
