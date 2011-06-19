package de.uniko.softlang;

import org.apache.hadoop.util.ProgramDriver;

import de.uniko.softlang.benchmarks.pipelined.PipelinedDeltaJob;
import de.uniko.softlang.benchmarks.pipelined.PipelinedJob;
import de.uniko.softlang.benchmarks.teraSort.generate.DeltaByJoin;
import de.uniko.softlang.benchmarks.teraSort.generate.Generate;
import de.uniko.softlang.benchmarks.teraSort.generate.GenerateDelta;
import de.uniko.softlang.benchmarks.teraSort.generate.GenerateNew;
import de.uniko.softlang.benchmarks.teraSort.merge.MergeByJoin;
import de.uniko.softlang.benchmarks.teraSort.sort.Sort;
import de.uniko.softlang.benchmarks.teraSort.sort.SortDelta;
import de.uniko.softlang.benchmarks.teraSort.streaming.GenerateStreaming;
import de.uniko.softlang.benchmarks.teraSort.streaming.GenerateStreamingDelta;
import de.uniko.softlang.benchmarks.teraSort.validate.Validate;
import de.uniko.softlang.benchmarks.teraSort.validate.ValidateDelta;
import de.uniko.softlang.wordcount.DeltaWordCount;
import de.uniko.softlang.wordcount.LineBasedDeltaCreation;
import de.uniko.softlang.wordcount.MergeWordCount;
import de.uniko.softlang.wordcount.OriginalWordCount;

/**
 * A description of an example program based on its class and a 
 * human-readable description.
 */
public class ExampleDriver {
  
  public static void main(String argv[]){
    int exitCode = -1;
    ProgramDriver pgd = new ProgramDriver();
    try {
      pgd.addClass("wordcount", OriginalWordCount.class, 
                   "A map/reduce program that counts the words in the input files.");
      pgd.addClass("wordcountdelta", DeltaWordCount.class, 
                   "A delta based map/reduce program that counts the words in the input files.");
      pgd.addClass("linedelta", LineBasedDeltaCreation.class, 
                   "A map/reduce program that computes the diff/delta of two line-based input files.");
      pgd.addClass("wordcountmerge", MergeWordCount.class, 
      "A map/reduce program that merges two wordcount-results.");

      pgd.addClass("gen", Generate.class, "Generate data for the terasort");
      pgd.addClass("gennew", GenerateNew.class, "Generate a new version of the input data. The user can specify the percentage of the diff.");
      pgd.addClass("gendelta", GenerateDelta.class, "Generate the delta for the two input files");
      pgd.addClass("genbyjoin", DeltaByJoin.class, "Generate the delta for the two input files using 'Map-side Join'");
      
      pgd.addClass("sort", Sort.class, "Run the terasort");
      pgd.addClass("sortdelta", SortDelta.class, "Run the terasort over the delta");
      
      pgd.addClass("merge", MergeByJoin.class, "Merge the old result with the sorted delta using 'Map-side Join'");
      
      pgd.addClass("validate", Validate.class, "Checking results of terasort");
      pgd.addClass("validatedelta", ValidateDelta.class, "Checking results of terasort applied to a delta");
      
      pgd.addClass("pipe", PipelinedJob.class, "Processing the output of TeraSort in subsequent jobs.");
      pgd.addClass("pipedelta", PipelinedDeltaJob.class, "Processing the output of TeraSort applied to a delta in subsequent jobs.");
      
      pgd.addClass("genStream", GenerateStreaming.class, "Generate data, suitable to create a streaming-delta from.");
      pgd.addClass("genStreamDelta", GenerateStreamingDelta.class, "Generate a streaming-delta, from previously generate data.");
      exitCode = pgd.driver(argv);
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    
    System.exit(exitCode);
  }
}
	
