// (c) Wiltrud Kessler
// 06.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.io.ParseReaderCoNLL;
import de.uni_stuttgart.ims.nlpbase.io.ParseWriterCoNLL;
import de.uni_stuttgart.ims.util.Fileutils;
import de.uni_stuttgart.ims.expansion.alignment.BestAlignmentFinder;
import de.uni_stuttgart.ims.expansion.candidates.ArgumentCreator;
import de.uni_stuttgart.ims.expansion.lists.BestScoreSortedList;
import de.uni_stuttgart.ims.expansion.semisupervised.SemiSupervisedExpansion;
import de.uni_stuttgart.ims.expansion.semisupervised.SentenceScoreElement;
import de.uni_stuttgart.ims.expansion.sentences.SentenceSelector;
import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;
import de.uni_stuttgart.ims.nlpbase.nlp.PredicateType;

/**
 * Expand a training data set with structural alignment.
 * 
 * @author kesslewd
 *
 */
public class ExpandTrainingSet {

   
   /**
    * Start the expansion!
    * 
    * @param args Array of Strings:
    *     seedsFilename , 
    *     unlabeledFilename, 
    *     outputFilename-prefix , 
    *     logfile
    */
   public static void main(String[] args) {
      if (args.length < 4) {
         System.err.println("Usage: ExpandTrainingSet [options] <seedsFilename> <unlabeledFilename> <outputFilename-prefix> <logfile>");
         System.exit(1);
      } else {
         new ExpandTrainingSet(args);
      }
   }
   
   
   
   /**
    * 
    */
   BufferedWriter outLog = null;
      
      
      
   /**
    * 
    * @param args
    */
   public ExpandTrainingSet (String[] args) {

      // ===== INITIALIZATION =====

      long tstampStartInit = System.currentTimeMillis(); // TEST !!!
      
      String seedsFilename = args[args.length-4];
      String unlabeledFilename = args[args.length-3];
      String outputFilename = args[args.length-2];
      String logFilename = args[args.length-1];
      
      
      // DEBUG !!
      try {
         outLog = new BufferedWriter(new FileWriter(logFilename,true));
      } catch (IOException e1) {
      }
      boolean debug = false;
      if (debug) writeToLog("... debugging is on ");
      
      

      boolean severalFiles = true;   
      //boolean severalFiles = false;


      // Things you can (and should) overwrite
      Similarity simArgs = null;
      Similarity simPreds = null;
      ArgumentCreator ac = null;
      BestAlignmentFinder af = null;
      SentenceSelector sentselector = null;
      int n = 0;
      int minSeed = 0;
      int maxSeed = 0;
      
      try {
         ExpansionOptions opty = new ExpansionOptions();
         opty.parseOptions(args, 0, args.length-4);
         System.err.println("Use options: " + Arrays.toString(args)); // debug
         writeToLog("Use options: " + Arrays.toString(args));
         
         simArgs = opty.getArgumentSimilarity();
         writeToLog("... loaded args similiarty measure " + simArgs.getName());
   
         simPreds = opty.getPredicateSimilarity();
         writeToLog("... loaded preds measure " + simPreds.getName());         
   
         ac = opty.getArgumentCreator();
         writeToLog("... loaded candidate creator " + ac.getName());
   
         af = opty.getAlignmentFinder();      
         writeToLog("... loaded alignment finder " + af.getClass());
         
         n = opty.getN();
         writeToLog("... expand with n=" + n + " sentences per seed");   
   
         minSeed = opty.getMinSeed();
         maxSeed = opty.getMaxSeed();
         writeToLog("... start from sentence " + minSeed + " and extract until " + maxSeed);

         sentselector = opty.getSentenceSelector();
         writeToLog("... sentence selector " + sentselector.getClass());
      
      } catch (Exception e) {
         System.err.println("ERROR in initialization: " + e.getMessage());
         e.printStackTrace();
         writeToLog("ERROR in initialization: " + e.getMessage());
         writeToLog(e.getStackTrace().toString());
         System.exit(1);
      }


      // Read files

      ParseReaderCoNLL parseReaderSeeds = null;
      ParseWriterCoNLL parseWriterOutput = null;
      writeToLog("... input seeds sentences file " + seedsFilename);
      System.err.println("... input seeds sentences file " + seedsFilename); // debug
      writeToLog("... input unlabeled sentences file " + unlabeledFilename);
      System.err.println("... input unlabeled sentences file " + unlabeledFilename); // debug
      writeToLog("... output expansion sentences files " + outputFilename + ".<sentence number and pred>");
      System.err.println("... output expansion sentences files " + outputFilename + ".<sentence number and pred>"); // debug
      
      try {
         ParseReaderCoNLL parseReaderUnlabeled = null;
         // Open input parsed sentences file
         parseReaderSeeds = new ParseReaderCoNLL(seedsFilename);
         parseReaderSeeds.openFile();

         // Open output file
         if (!severalFiles) {
            parseWriterOutput = new ParseWriterCoNLL(outputFilename);
            parseWriterOutput.openFile();
         }

         // Open input parsed sentences file
         parseReaderUnlabeled = new ParseReaderCoNLL(unlabeledFilename);
         parseReaderUnlabeled.openFile();

         // Collect all unlabeled sentences
         // (this is not really memory-efficient)
         List<SRLSentence> unlabeledSentences = new ArrayList<SRLSentence>();
         SRLSentence unlabeledSentence;
         while (!(unlabeledSentence = parseReaderUnlabeled.readParseOnlyDeps()).isEmpty()) {
            unlabeledSentences.add(unlabeledSentence);
         }

         sentselector.setUnlabeledSentencesPool (unlabeledSentences);
         writeToLog("... added " + unlabeledSentences.size() + " unlabeled sentences.");

         Fileutils.closeSilently(parseReaderUnlabeled);
         
      } catch (Exception e) {
         System.err.println("ERROR in initialization: " + e.getMessage());         
         e.printStackTrace();
         writeToLog("ERROR in initialization: " + e.getMessage());
         writeToLog(e.getStackTrace().toString());
         System.exit(1);
      }
      
      

      
      // ===== PROCESSING =====
            
      // Initialize Expansion module
      SemiSupervisedExpansion expander = new SemiSupervisedExpansion(simArgs, simPreds, af, ac, sentselector);
      expander.setLogFile(outLog);

      // DEBUG !!
      long tstampEndInit = System.currentTimeMillis(); 
      writeToLog("Processing time init : " + ((tstampEndInit-tstampStartInit)/1000.0) + "s" );

      
      
      // Bookkeeping
      SRLSentence seedSentence;
      int seedSentenceNo = 0;
      int newSamplesNo = 0;
      long processingTime = (long) 0.0;
      int errorNo = 0;
      
      BufferedWriter outScores = null;
      
      // For every seed sentence ...
      while (!(seedSentence = parseReaderSeeds.readParseSRL()).isEmpty()) {

         seedSentenceNo++;
         
         // Start with sentence given in minSeed,
         // skip all others.
         if (seedSentenceNo < minSeed) { 
            continue;
         }

         // DEBUG !!
         long tstampStart = System.currentTimeMillis();
         writeToLog("");
         writeToLog("\n== Seed sentence (no. " + seedSentenceNo + ") ==" + "\n");
         writeToLog(seedSentence.toSRLString());
        
         try {
            
            for (Word predicate : seedSentence.getPredicates()) {
               
               if (predicate.getType() == PredicateType.difference) {
                  writeToLog("Skip predicate - comparison type is difference."); // DEBUG !!
                  continue;
               }

               writeToLog("--- Predicate: " + predicate + " ---");
               
               
               // Do the expansion
               BestScoreSortedList<SentenceScoreElement> bestAlignmentsList = expander.expandWithUnlabeled(seedSentence, predicate, n);
               
               if (bestAlignmentsList == null) {
                  writeToLog("No best alignments for sentence " + seedSentenceNo + " pred " + predicate + ", check what has happened!");
                  continue;
               } else {
                  writeToLog( bestAlignmentsList.size() + " best alignments for sentence " + seedSentenceNo + " pred " + predicate + ".");
               }
              
               
               // Annotate the result
               List<SRLSentence> expansionSentences = expander.makeResultList(seedSentence, predicate);
               newSamplesNo += expansionSentences.size();
      
               
               // Open output file and write annotates expansion sentences
               if (severalFiles) {                  
                  try {
                     if (seedSentenceNo > minSeed && parseWriterOutput != null) // Close old output file and open new one
                        parseWriterOutput.close();
                  } catch (IOException e) {
                     System.err.println("ERROR when closing file output: " + e.getMessage());
                     e.printStackTrace();
                     writeToLog("ERROR when closing file output: " + e.getMessage());
                     writeToLog(e.getStackTrace().toString());
                  }
                  try {
                     parseWriterOutput = new ParseWriterCoNLL(outputFilename + "." + seedSentenceNo  + "_" + predicate.getId()+ "_" + clean(predicate.getLemma()) + ".txt");
                     parseWriterOutput.openFile();   

                     for (SRLSentence sentence : expansionSentences) {                  
                        parseWriterOutput.writeParse(sentence);
                     }
                  } catch (IOException e) {
                     System.err.println("ERROR when opening new output file: " + e.getMessage());
                     e.printStackTrace();
                     writeToLog("ERROR when opening new output file: " + e.getMessage());
                     writeToLog(e.getStackTrace().toString());
                     errorNo++;
                     parseWriterOutput.close();
                     throw e;
                  }
               }

               // Open scores file and write the scores
               try {
                  if (outScores != null ) // if there is one to close, close
                     outScores.close();
              
                  FileWriter fstream2 = new FileWriter(outputFilename + "." + seedSentenceNo + "_" + predicate.getId()+ "_" + clean(predicate.getLemma()) + ".scores");
                  outScores = new BufferedWriter(fstream2);

                  for (SentenceScoreElement score : bestAlignmentsList) {
                     writeToLog("   " + score.similarity + " / " + score.predicate + " / " + score.sentence); // DEBUG !!
                     outScores.write(score.similarity + " / " + score.predicate + " / " + score.sentence); 
                     outScores.newLine();
                     outScores.write(score.alignment.toString()); 
                     outScores.newLine();
                     outScores.flush();
                  }
               } catch (IOException e) {                  
                  System.err.println("ERROR when opening new score output file: " + e.getMessage());
                  e.printStackTrace();
                  writeToLog("ERROR when opening new score output file: " + e.getMessage());
                  writeToLog(e.getStackTrace().toString());
                  errorNo++;
                  throw e;
               }

               
            } // for every seed predicate

            
         } catch (Throwable e) { // one sentence
            errorNo++;
            // Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
            System.err.println("ERROR !!! Aborted expansion of sentence no. " + seedSentenceNo + " / " + seedSentence);
            System.err.println("Message: " + e.getMessage() + " (error number " + errorNo + ")");
            writeToLog("ERROR !!! Aborted expansion of sentence no. " + seedSentenceNo + " / " + seedSentence);
            writeToLog("Message: " + e.getMessage() + " (error number " + errorNo + ")");
            e.printStackTrace();
            if (errorNo>10)
               break;
         }
         

         // DEBUG !!
         long tstampEnd = System.currentTimeMillis(); 
         processingTime += tstampEnd-tstampStart;
         System.err.println("Processing time sentence " + seedSentenceNo + " : " + timeOutput(tstampEnd-tstampStart) + " at " + new Date()); // debug purposes 
         writeToLog("Processing time sentence " + seedSentenceNo + " : " + timeOutput(tstampEnd-tstampStart) + " at " + new Date() + "\n");
         writeToLog("Total processing time " + timeOutput(processingTime) + "\n");
         
         
         // Stop with sentence given in maxSeed
         if (seedSentenceNo >= maxSeed) {
            writeToLog("Stop after sentence " + seedSentenceNo + "\n");
            break;
         }
      

      } // for every seed sentence

      
      

      // ===== STATISTICS, CLEANUP =====

      // DEBUG !!
      writeToLog("Collected " + newSamplesNo + " sentences for " + (maxSeed - minSeed) + " seeds.");
      writeToLog("Total processing time " + timeOutput(processingTime));
      writeToLog("Started at " + new Date(tstampStartInit) + " ended at " + new Date() + "\n"); 
      writeToLog(ac.getStatistics()); // Print argument creator statistics

         
      //Close files and clean up
      Fileutils.closeSilently(parseReaderSeeds);
      Fileutils.closeSilently(expander);
      writeToLog("... done.");
      

   }
   
   

   /**
    * Write something to the log (or not, if there is an error).
    * If there is no log file write to stdout.
    * 
    * @param string A line to write.
    */
   private void writeToLog(String string) {
      if (outLog != null)
         try {
            outLog.write (string);
            outLog.newLine();
            outLog.flush();
         } catch (IOException e) {
            System.out.println("Fail in log writing!!");
         }
      else {
         System.out.println(string);
      }
   }
   

   /**
    * Remove all non-alphanumeric, make lowercase.
    */
   private static String clean(String lemma) {
      lemma = lemma.replaceAll("\\p{Punct}", "");
      return lemma.toLowerCase();
   }

   /**
    * Output a time stamp in human-readable form. 
    */
   private static String timeOutput(long processingTime) {
      return  String.format(" %.4f s (%.4f min, %.4f h)", (processingTime/1000.0), processingTime/60000.0 , processingTime/3600000.0);
   }
}
