// (c) Wiltrud Kessler
// 11.06.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.alignment;

import java.util.List;

import de.uni_stuttgart.ims.expansion.alignment.AlignmentGenerator;
import de.uni_stuttgart.ims.expansion.alignment.WordAlignment;
import de.uni_stuttgart.ims.expansion.semisupervised.SentenceScoreElement;
import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * 
 * Find the best alignment of the unlabeled sentence to the labeled sentence
 * using a generator that just tries out all possible combinations.
 * 
 * Exhaustive search, checks all alignments - may take a while!
 * 
 * @author kesslewd
 *
 */
public class BestAlignmentFinderGenerator extends BestAlignmentFinder {

     

   /**
    * Find the best alignment of the unlabeled sentence to the labeled sentence
    * using a generator that just tries out all possible combinations.
    *
    * @param similarityFunction Similarity measure for two words (argument candidates)
    */
   public BestAlignmentFinderGenerator (Similarity similarityFunction) {
      super(similarityFunction);
   }
   
   

   /**
    * Find the best alignment of the unlabeled sentence to the labeled sentence
    * using a generator that just tries out all possible combinations.
    * Checks all alignments, computes a score, and returns best.
    * May take a while.
    *  
    * @param argumentsLabeled  Argument candidates on LABELED side.
    * @param argumentsUnlabeled  Argument candidates on UNLABELED side.
    * @param sentenceUnlabeled  LABELED seed sentence.
    * @param predicateUnlabeled  LABELED predicate (that is currently processed).
    * @param sentenceLabeled  UNLABELED expansion sentence.
    * @param predicateLabeled  UNLABELED predicate (that is currently processed).
    * @return Best alignment and its score for the two sentences.
    */
   public SentenceScoreElement findBestAlignment(List<Word> argumentsLabeled, List<Word> argumentsUnlabeled, 
         SRLSentence sentenceUnlabeled, Word predicateUnlabeled, SRLSentence sentenceLabeled, Word predicateLabeled) {
      
      // Bookkeeping for best alignment / similarity
      double bestSimilarity = similarityFunction.getMinimum()-1;
      WordAlignment bestAlignment = null;
      
      try {
         // Generate all alignments and iterate over them.
         // THIS MAY TAKE TIME!
         AlignmentGenerator generator = new AlignmentGenerator(argumentsLabeled, argumentsUnlabeled);
         
         for (WordAlignment currentAlignment : generator) {
            
            
            // Score for this alignemtn is the sum of scores over all
            // similarities of aligned words.
            double similarity = 0;
            for (Word key : currentAlignment.getKeys()) {
               double thisSim = this.similarityFunction.getSimilarity(sentenceLabeled, predicateLabeled, key,
                     sentenceUnlabeled, predicateUnlabeled, currentAlignment.getAligned(key));
               similarity += thisSim;
               currentAlignment.changeSimilarity(key, thisSim);
            }
   
            // Normalize
            similarity = similarity / (float) argumentsLabeled.size();
            //System.out.println(similarity + " / " + currentAlignment); // TEST !!!
            
            // Keep best alignment and similarity
            if (similarity > bestSimilarity) {
               bestSimilarity = similarity;
               bestAlignment = currentAlignment;
            }
            
         }
      } catch (java.lang.OutOfMemoryError e) {
         // Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
         System.err.println("ERROR !!! " + e.getMessage());
         e.printStackTrace();
         return null;
      }
      
      return new SentenceScoreElement(sentenceUnlabeled, predicateUnlabeled, bestAlignment, bestSimilarity);
   }
   

   

}
