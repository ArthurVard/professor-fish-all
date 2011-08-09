package de.uniko.softlang.indexer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.apache.mahout.vectorizer.encoders.TextValueEncoder;

import de.uniko.softlang.crawler.datastores.SiteContent;
import de.uniko.softlang.crawler.delta.WordLevelDelta;
import de.uniko.softlang.crawler.fetcher.DeltaFetcher;
import de.uniko.softlang.utils.PairWritable;

public class TermFreqVectorCreator extends Configured implements Tool {
	public static final Log LOG = LogFactory.getLog(Indexer.class);
	public static final String VECTOR_BASE_DIR = "outVectorize";
	private static final String ALL_VECTORS_DIR = "allVectors";
	public static final String FULL_DIR = "full";
	public static final String CHANGED_DIR = "changed";
	private static final double WEIGHT = 1.0;
  private static final double LOG_2 = Math.log(2);
  

	
	public static class DocVectorCreatMapper extends Mapper<Text, TupleWritable, Text, VectorWritable>{
		TextValueEncoder encoder;
		
		@Override
	  protected void setup(Context context) throws IOException, InterruptedException {
			encoder = new TextValueEncoder("encoder-" + org.apache.hadoop.mapreduce.Mapper.Context.TASK_ID);
		}
		
		protected void map(Text key, TupleWritable values, Context context) throws IOException, InterruptedException {
			SiteContent first = (SiteContent)values.iterator().next();
			SiteContent latestVersion = null;
			for (Writable val : values) {
				SiteContent content = (SiteContent) val;
				if(latestVersion == null || content.getVersion() > latestVersion.getVersion())
					latestVersion = content;
			}
			encoder.addText(latestVersion.getDelta().getAsString());
			Vector v = new NamedVector(new RandomAccessSparseVector(30,10), key.toString()); 
			encoder.flush(WEIGHT, v);
			context.write(key, new VectorWritable(v));
			
		}
	}
	
	public static class WordVectorCreatMapper extends Mapper<Text, TupleWritable, Text, PairWritable<VectorWritable,BooleanWritable>>{
		StaticWordValueEncoder wordEncoder;
		BooleanWritable TRUE;
		BooleanWritable FALSE;
		
		@Override
	  protected void setup(Context context) throws IOException, InterruptedException {
			wordEncoder = new StaticWordValueEncoder("encoder-" + org.apache.hadoop.mapreduce.Mapper.Context.TASK_ID);
			TRUE = new BooleanWritable(true);
			FALSE = new BooleanWritable(false);
		}
		
		protected void map(Text key, TupleWritable values, Context context) throws IOException, InterruptedException {
			//check delta level
			VectorWritable fullVector = null;
			WordLevelDelta accumulatedDelta = new WordLevelDelta();
			for (Writable val : values) {
				if(val instanceof VectorWritable){
					fullVector = (VectorWritable)val;
				}else{
					SiteContent content = (SiteContent) val;
					if(content != null)
						accumulatedDelta.apply(content.getDelta());
					
				}
			}
			if(accumulatedDelta.isEmpty() && fullVector != null){
				//no changes to apply
				context.write(key, new PairWritable<VectorWritable, BooleanWritable>(fullVector, FALSE));
			}else{
				Vector deltaVector = new NamedVector(new RandomAccessSparseVector(Clusterer.VECTOR_SIZE,10), key.toString()); 
				for(String word : accumulatedDelta.getContent().keySet()){
					int count = accumulatedDelta.getContent().get(word).get();
					wordEncoder.addToVector(word, WEIGHT * Math.log(1 + count) / LOG_2,deltaVector);
				}
				if(fullVector == null){
					//no previous vector available (-> first encounter of this site)
					context.write(key, new PairWritable<VectorWritable, BooleanWritable>(new VectorWritable(deltaVector), TRUE));
				}else{
					fullVector.get().addTo(deltaVector);
					context.write(key, new PairWritable<VectorWritable, BooleanWritable>(fullVector, TRUE));
				}
				
				
			}
		}
	}
	/**
	 * Creates a {@link Vector} for each site in the specified segments. 
	 * In the case of multiple entries of a site in different segments, only the 
	 * latest version of {@link SiteContent} is considered.
	 * @param in the segments to process
	 * @param out the output directory
	 * @return the number of vectors created
	 * @throws Exception
	 */
	public long createTermFreqVectors(Path[] in, Path out, long time, boolean wordLvlDelta) throws Exception {
		Configuration conf = new Configuration();
		setConf(conf);
		Job job = Job.getInstance(new Cluster(getConf()), getConf());
		job.setJobName("TermFreqVectorCreator");
		Path[] inputContent;
	  Path output = new Path(out, ""+time);
	  Path allVectorsDir = new Path(out, ALL_VECTORS_DIR);
	  FileSystem fs = out.getFileSystem(getConf());
  	boolean firstVectors = !fs.exists(allVectorsDir);
	  
		if(wordLvlDelta){
			if(firstVectors){
				inputContent = new Path[in.length];
			}else{
				inputContent = new Path[in.length+1];
				inputContent[in.length] = allVectorsDir;
			}
			DeltaVectorOutputFormant.setOutputPath(job, output);
		  job.setOutputFormatClass(DeltaVectorOutputFormant.class);
		  job.setMapperClass(WordVectorCreatMapper.class);
		  job.setOutputKeyClass(Text.class);
		  job.setOutputValueClass(PairWritable.class);
		}else{
			inputContent = new Path[in.length];
			SequenceFileOutputFormat.setOutputPath(job, output);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.setMapperClass(DocVectorCreatMapper.class);
			job.setOutputKeyClass(Text.class);
		  job.setOutputValueClass(VectorWritable.class);

		}
		for (int i = 0; i < in.length; i++) {
			inputContent[i] = new Path(in[i],DeltaFetcher.CONTENT_DIR); 
		}
		job.setInputFormatClass(CompositeInputFormat.class);
		job.getConfiguration().set(
				CompositeInputFormat.JOIN_EXPR,
				CompositeInputFormat.compose("outer", SequenceFileInputFormat.class, inputContent));
		
		job.setJarByClass(TermFreqVectorCreator.class);
		job.setNumReduceTasks(0);
	  
	  boolean success = job.waitForCompletion(true);

	  if(success && wordLvlDelta){
	  	Path newFullOut = new Path(output, FULL_DIR);
	  	Path changedOut = new Path(output, CHANGED_DIR);
	  	if(!firstVectors)
	  		fs.delete(allVectorsDir, true);
	  	fs.rename(newFullOut, allVectorsDir);
	  	Path tmp = new Path(out, "tmp");
	  	fs.rename(changedOut, tmp);
	  	fs.delete(output, true);
	  	fs.rename(tmp, output);
	  	fs.delete(tmp, true);
	  }
	  Counter c1 = job.getCounters().findCounter("org.apache.hadoop.mapreduce.TaskCounter","MAP_OUTPUT_RECORDS");
	  return c1.getValue();
	 
	}
	
	public int run(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: Index <segment_dir> <out_dir> [word_lvl]");
      return -1;
    }	
    boolean wordLvl = false;
    if(args.length > 2 && args[3].equals("word_lvl"))
    	wordLvl = true;
    Path[] in = {new Path(args[0])};
    long time = System.currentTimeMillis();
		createTermFreqVectors(in, new Path(args[1]+"/"+TermFreqVectorCreator.VECTOR_BASE_DIR),time, wordLvl);
		return 0;
}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new TermFreqVectorCreator(), args);
		System.exit(res);
}
}
