// (c) Wiltrud Kessler
// 26.03.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.alignment;

import java.util.List;

import de.uni_stuttgart.ims.expansion.alignment.AlignmentMatrix;
import de.uni_stuttgart.ims.expansion.alignment.WordAlignment;
import de.uni_stuttgart.ims.expansion.semisupervised.SentenceScoreElement;
import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * 
 * Find the best alignment of the unlabeled sentence to the labeled sentence
 * using a matrix-based approach + Hungarian algorithm.
 *
 * Exhaustive search, checks all alignments - may take a while!
 * 
 * @author kesslewd
 *
 */
public class BestAlignmentFinderMatrix extends BestAlignmentFinder {


   /**
    * Find the best alignment of the unlabeled sentence to the labeled sentence
    * using a matrix-based approach + Hungarian algorithm.
    *
    * @param similarityFunction Similarity measure for two words (argument candidates)
    */
   public BestAlignmentFinderMatrix (Similarity similarityFunction) {
      super(similarityFunction);
   }
   


   /**
    * Find the best alignment of the unlabeled sentence to the labeled sentence
    * using a matrix-based approach + Hungarian algorithm.
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
      double bestSimilarity = this.similarityFunction.getMinimum()-1;
      WordAlignment bestAlignment = null;
      
      
      // Build up matrix
      AlignmentMatrix generator = new AlignmentMatrix(argumentsLabeled, argumentsUnlabeled, sentenceUnlabeled, predicateUnlabeled, sentenceLabeled, predicateLabeled);

      // Calculate all similarities
      generator.calculateSimilarities(this.similarityFunction);
      
      // Get best
      bestSimilarity = generator.getBestSimilarity();
      bestAlignment = generator.getBestAlignment();
      
      
      return new SentenceScoreElement(sentenceUnlabeled, predicateUnlabeled, bestAlignment, bestSimilarity);
   }
      

}
