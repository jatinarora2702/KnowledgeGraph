// (c) Wiltrud Kessler
// 11.06.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.semisupervised;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.expansion.alignment.AlignmentAnnotation;
import de.uni_stuttgart.ims.expansion.alignment.BestAlignmentFinder;
import de.uni_stuttgart.ims.expansion.alignment.WordAlignment;
import de.uni_stuttgart.ims.expansion.candidates.ArgumentCreator;
import de.uni_stuttgart.ims.expansion.lists.BestScoreSortedList;
import de.uni_stuttgart.ims.expansion.lists.NBestList;
import de.uni_stuttgart.ims.expansion.lists.ThresholdList;
import de.uni_stuttgart.ims.expansion.sentences.SentenceSelector;
import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.expansion.similarity.Similarity.ExplainedSimilarityValue;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Sentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;
import de.uni_stuttgart.ims.util.Fileutils;


/**
 * 
 * Expand a seed sentence with a comparative predicate
 * with the most similar sentences 
 * from a set of unlabeled sentences.
 * 
 * Processing in 'expandWithUnlabeled()': 
 * For a seed sentence s, predicate p:
 *    For every unlabeled sentence u
 *    - if u does not contain p, continue with next
 *       [see subclasses of 'SentenceSelector' from 'expansion.sentences']
 *    - get all argument candidates from u and l
 *       [see subclasses of 'ArgumentCreator' from 'expansion.candidates']
 *    - score all possible alignments between candidates
 *       [see subclasses of 'BestAlignmentFinder' from 'expansion.alignment',
 *       see subclasses of 'Similarity' from 'expansion.similarity']
 *    - save best alignment and score
 *    Sort all u by score and keep best n / best over threshold
 *       [see subclasses of 'BestScoreSortedList' from 'expansion.utils']
 *     
 * Annotate the resulting sentences according to the alignment 
 * with 'makeResultList()'
 * 
 * 
 * @author kesslewd
 *
 */
public class SemiSupervisedExpansion  implements Closeable {

   /**
    * Offers method to find best alignment.
    */
   protected BestAlignmentFinder alignmentFinder;
   
   /**
    * Similarity function to use to score an alignemnt.
    */
   protected Similarity argumentSimilarityFunction;
   
   /**
    * Similarity function to use to score the predicates.
    */
   protected Similarity predicateSimilarityFunction;

   /**
    * List to store best alignments.
    */
   protected BestScoreSortedList<SentenceScoreElement> bestAlignmentsList = null;

   /**
    * Class to get argument candidates from labeled/unlabeled sentences.
    */
   protected ArgumentCreator candidateCreator;

   /**
    * Select the possible expansion sentences (those with compatible predicates)
    * from the complete set of sentences.
    */
   protected SentenceSelector sentenceSelector;

   
   /**
    * [DEBUG] Debug output (more than normal).
    */
   private boolean debug = false;

   /**
    * [DEBUG] Suppress all output.
    */
   public boolean silent = false;

   /**
    * [DEBUG] Write output to file instead of stdout.
    */
   private BufferedWriter logFile = null;

   

   // ============= GETTER/SETTER =============
   
   
   /**
    * Expand a seed sentence with a comparative predicate
    * with the most similar sentences from the set of unlabeled sentences.
    * 
    * @param unlabeledSentences Pool of unlabeled sentences to choose from.
    * @param n How many sentences to collect per seed.
    */
   public SemiSupervisedExpansion (
         Similarity argumentSimilarityFunction, 
         Similarity predicateSimilarityFunction, 
         BestAlignmentFinder alignmentFinder,
         ArgumentCreator candidateCreator,
         SentenceSelector sentenceSelector
         ) {
      this.argumentSimilarityFunction = argumentSimilarityFunction;
      this.predicateSimilarityFunction = predicateSimilarityFunction;
      this.alignmentFinder = alignmentFinder;
      this.candidateCreator = candidateCreator;
      this.sentenceSelector = sentenceSelector;
   }
 


   /**
    * Similarity function to use to score an alignemnt.
    * @param argumentSimilarityFunction
    */
   public void setArgumentSimilarity(Similarity argumentSimilarityFunction) {
      alignmentFinder.setSimilarityFunction(argumentSimilarityFunction);
      this.argumentSimilarityFunction = argumentSimilarityFunction;
   }

   /**
    * Similarity function to use to score an alignemnt.
    * return argumentSimilarityFunction
    */
   public Similarity getArgumentSimilarity() {
      return this.argumentSimilarityFunction;
   }

   /**
    * Similarity function to use to score the predicates.
    * @param predicateSimilarityFunction
    */
   public void setPredicateSimilarity(Similarity predicateSimilarityFunction) {
      this.predicateSimilarityFunction = predicateSimilarityFunction;
   }

   /**
    * Similarity function to use to score the predicates.
    * return predicateSimilarityFunction
    */
   public Similarity getPrgumentSimilarity() {
      return this.predicateSimilarityFunction;
   }

   /**
    * Set to output to silent (no messages).
    */
   public void setSuppressMessages(boolean silentMode) {
      silent = silentMode;
   }

   /**
    * Set to output to debug mode (more messages).
    */
   public void setDebugMessages(boolean debugMessages) {
      debug = debugMessages;
   }

   /**
    * Set log file (then everything will be written to this file instead of stdout,
    * what is written depends on the level above).
    * You'll need to close this file yourself!
    */
   public void setLogFile(BufferedWriter logFile) {
      this.logFile = logFile;
   }


   
   /**
    * Close all open resources.
    */  
   public void close() {
         Fileutils.closeSilently(argumentSimilarityFunction);
         Fileutils.closeSilently(predicateSimilarityFunction);
   }

   
   
   
   // ============= PROCESSING =============

   
   
   /**
    * Check all unlabeled sentences for the best matches with the given seed sentence
    * containing the comparative predicate and return the best n sentences. 
    * 
    * @param sentenceLabeled The labeled seed sentence.
    * @param predicateLabeled The predicate.
    * @param n The number of unlabeled sentences you want to get back.
    * 
    * @return n best sentences, with best alignment, descending order of similarity
    *    'null' in case of error (no predicate, no unlabeled, no real args, ...)
    */
   public BestScoreSortedList<SentenceScoreElement> expandWithUnlabeled(SRLSentence sentenceLabeled, Word predicateLabeled, int n) {
      
      // Reset storage for result (n-best list)
      bestAlignmentsList = new NBestList<SentenceScoreElement>(n);
      
      return expandWithUnlabeled(sentenceLabeled, predicateLabeled);
   }

   /**
    * Check all unlabeled sentences for the best matches with the given seed sentence
    * containing the comparative predicate and return the sentences that have a similarity
    * value higher than the given threshold. 
    * 
    * @param sentenceLabeled The labeled seed sentence.
    * @param predicateLabeled The predicate.
    * @param threshold The minimum similarity value to return a sentence.
    * 
    * @return Sentences that have a similarity, with best alignment, descending order of similarity
    *    'null' in case of error (no predicate, no unlabeled, no real args, ...)
    */
   public BestScoreSortedList<SentenceScoreElement> expandWithUnlabeled(SRLSentence sentenceLabeled, Word predicateLabeled, float threshold) {
      
      // Reset storage for result (threshold list)
      bestAlignmentsList = new ThresholdList<SentenceScoreElement>(new SentenceScoreElement(threshold));
      
      return expandWithUnlabeled(sentenceLabeled, predicateLabeled);
   }
   
   
   /**
    * Check all unlabeled sentences for the best matches with the given seed sentence
    * containing the comparative predicate and return the best n or those that have 
    * a similarity value higher than the given threshold. 
    * (according to the list previously set with one of the other 'expandWithUnlabeled')
    * [reset the previously used list to store results]
    * 
    * @param sentenceLabeled The labeled seed sentence.
    * @param predicateLabeled The predicate of the labeled seed sentence.
    * 
    * @return best sentences, with best alignment, descending order of similarity
    *    'null' in case of error (no predicate, no unlabeled, no real args, ...)
    */
   public BestScoreSortedList<SentenceScoreElement> expandWithUnlabeled(SRLSentence sentenceLabeled, Word predicateLabeled) {
      
      // TODO HACK
      // Set a default best alignments list
      if (bestAlignmentsList == null) {
         System.err.println("No best alignment list!! Set the default NBestList<SentenceScoreElement>(20)!");
         bestAlignmentsList = new NBestList<SentenceScoreElement>(20); // TODO
      }
         
      
      // Reset storage for result
      // (abstract - implementation specific)
      bestAlignmentsList.reset();
      
      
      // Check labeled sentence
      
      // If there is no predicate - return
      if (predicateLabeled == null) {
         logorprint("Abort expansion - no predicate", !silent);
         return null;
      }

      // Get candidates from labeled sentence
      // (abstract - implementation specific)
      List<Word> argumentsLabeled = this.candidateCreator.getPossibleArgumentsLabeled(predicateLabeled, sentenceLabeled);      
      Collections.sort(argumentsLabeled, sentenceLabeled.wordSequenceComparator);
      logorprint("Source: " + argumentsLabeled, debug);
      
      // If we have no candidate arguments, there is nothing to do - return
      if (argumentsLabeled == null || argumentsLabeled.isEmpty()) {
         logorprint("Abort expansion - no source side argument candidates", !silent);
         return null;
      }

      // If none/only one of the real arguments is in the candidate list - return
      // TODO 0 or 1??
      int contains = 0;
      for (Word realArgument : sentenceLabeled.getArguments(predicateLabeled)) {
         if (argumentsLabeled.contains(realArgument)) {
            contains++;
         }
      }
      if (contains <= 0) {
         logorprint("Abort expansion - no real arguments in source side argument candidates", !silent);
         return null;
      }
      

      
      // Get unlabeled sentences
      Set<SRLSentence> unlabeledSentences = sentenceSelector.getUnlabeledSentences(sentenceLabeled, predicateLabeled);

      // If there are no unlabeled sentences - return
      if (unlabeledSentences == null || unlabeledSentences.isEmpty()) {
         logorprint("Abort expansion - no unlabeled sentences", !silent);
         return null;
      }
      
      
      // all seems good, go on checking the unlabeled sentences
      
      logorprint("Check " + unlabeledSentences.size() + " expansion sentences.", debug);
      int skip = 0;
      
      // Check all the sentences
      for (SRLSentence unlabeledSentence : unlabeledSentences) {
          
         // Ignore sentence if it is exactly the same sentence
         // (the exact same sentence does not give new information) 
         if (unlabeledSentence.isSameSentence(sentenceLabeled)) {
            logorprint("Skip sentence - is same sentence", !silent);
            skip++;
            continue;
         }
         
         // Find the predicate in the unlabeled sentence (not case-sensitive)
         List<Word> predicatesUnlabeled = sentenceSelector.getPredicatesFromUnlabeledSentence(sentenceLabeled, predicateLabeled, unlabeledSentence);
               
         if (predicatesUnlabeled == null || predicatesUnlabeled.isEmpty()) {
            // this should not happen
            logorprint("Skip sentence - no target side predicates for " + unlabeledSentence.toString(), !silent);
            skip++;
            continue;
         }
         
         for (Word predicateUnlabeled : predicatesUnlabeled) {
                       
            // If the sentence contains the predicate, calculate alignment scores.
            if (predicateUnlabeled != null) {
               
               // DEBUG output to check if it still running and how long each sentence takes...
               Timestamp tstamp = new Timestamp(System.currentTimeMillis()); // TEST !!!
               logorprint(tstamp + " check sentence for pred " + predicateUnlabeled + " : "  + unlabeledSentence, debug); // TEST!!!
   
               // Get possible argument candidates for both sentences.
               // Note that actual arguments on the labeled side may not be included.
               List<Word> argumentsUnlabeled = this.candidateCreator.getPossibleArgumentsUnlabeled
                        (predicateUnlabeled, unlabeledSentence, predicateLabeled, sentenceLabeled);

               if (argumentsUnlabeled == null || argumentsUnlabeled.isEmpty()) {
                  logorprint("Skip sentence - no target side arg candidates for " + unlabeledSentence.toString(), !silent);
                  skip++;
                  continue;
               }
              
               logorprint(argumentsUnlabeled.toString(), debug);
               
               // Get the best aligment (considering only the arguments with their similarities)
               SentenceScoreElement bestAlignment = this.alignmentFinder.findBestAlignment(argumentsLabeled, argumentsUnlabeled, 
                     unlabeledSentence, predicateUnlabeled, sentenceLabeled, predicateLabeled);
   
               // Add predicate similarity (if set)
               if (this.predicateSimilarityFunction != null) {
                  // Calculate the predicate similarity
                  ExplainedSimilarityValue predicateSimilarity = this.predicateSimilarityFunction.getExplainedSimilarity
                        (sentenceLabeled, predicateLabeled, predicateLabeled, unlabeledSentence, predicateUnlabeled, predicateUnlabeled);
                  // Add to aligment
                  bestAlignment.alignment.add(predicateLabeled, predicateUnlabeled, predicateSimilarity.similarityValue, predicateSimilarity.explanation);
                  // Add to alignment score, the predicate similarity is weighted as much as one argument
                  // (ArgSim * ArgSize + PredSim) / (ArgSize+1)
                  bestAlignment.similarity = (bestAlignment.similarity * argumentsLabeled.size()
                                                +  predicateSimilarity.similarityValue) / (argumentsLabeled.size()+1);
               }
              
               logorprint("Best alignment (" + bestAlignment.similarity + ") " + bestAlignment.alignment, debug); // TEST!!!
               
               // Decide if to put into list
               // (abstract - implementation specific)
               bestAlignmentsList.add(bestAlignment);            
               
            } else { // this should never happen
               logorprint("Skip sentence - unlabeled Predicate is null for " + unlabeledSentence, !silent);
               skip++;
            }
         }
         
      }
      
      logorprint("Skipped " + skip + " sentences.", debug | (!silent & skip>0));
      
      return bestAlignmentsList;
   }
   
   
   
   /**
    * Write the text to a log file (if it is != null) or to standard out,
    * iff the condition is met. 
    * 
    * @param text
    * @param condition
    */
   private void logorprint (String text, boolean condition) {
      if (condition) {
         if (logFile != null)
            try {
               logFile.write(text + "\n");
            } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         else
            System.out.println(text);
      }      
   }


   
   // ============= ABSTRACT METHODS =============
   
   
   /**
    * Get the n best sentences and annotate them according to alignment.
    * 
    * @param predicate
    * @param seedSentence
    * @return
    */
   public List<SRLSentence> makeResultList (SRLSentence sentenceLabeled, Word predicateLabeled) {

      // Get the n best sentences and annotate them according to alignment.
      // list.first() is the worst of them
      // list.last() is the best of them
      ArrayList<SRLSentence> results = new ArrayList<SRLSentence>();
      Iterator<SentenceScoreElement> list = this.bestAlignmentsList.iterator();
      
      while (list.hasNext()) {
         
         // Get a copy of the sentence
         // (we do not want to modify the unlabeled sentences,
         // the next seed sentence should have completely unlabeled sentences)
         SentenceScoreElement unlabeledSSE = list.next();
         SRLSentence sentenceCopy = new SRLSentence( (Sentence)unlabeledSSE.sentence); // only deps
         Word predicateUnlabeled = sentenceCopy.getWord(unlabeledSSE.predicate.getId());
         
         // Get a copy of the word alignment, because the values are Word objects
         // from the unlabeled sentence, and we now have new Word objects in
         // the sentence copy.
         WordAlignment alignmentCopy = new WordAlignment();
         for (Word key : unlabeledSSE.alignment.getKeys()) {
            Word aligned = unlabeledSSE.alignment.getAligned(key);
            Word word2 = null;
            if (aligned != null) {
               word2 = sentenceCopy.getWord(aligned.getId());
            }
            double similarity = unlabeledSSE.alignment.getAlignmentSimilarity(key);
            alignmentCopy.add(key, word2, similarity);
         }

         // Annotate sentence and add to results
         AlignmentAnnotation.annotateSentence(predicateLabeled, sentenceLabeled, predicateUnlabeled, sentenceCopy, alignmentCopy);
         results.add(sentenceCopy);
      }
      return results;
   }

}
