// (c) Wiltrud Kessler
// 26.03.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.alignment;

import java.util.List;

import de.uni_stuttgart.ims.expansion.semisupervised.SentenceScoreElement;
import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Abstract wrapper for implementations to find the best alignment 
 * of the unlabeled sentence to the labeled sentence.
 * 
 * @author kesslewd
 *
 */
public abstract class BestAlignmentFinder {

   protected Similarity similarityFunction;
   

   /**
    * Find the best alignment of the unlabeled sentence to the labeled sentence.
    *
    * @param similarityFunction Similarity measure for two words (argument candidates)
    */
   public BestAlignmentFinder (Similarity similarityFunction) {
      this.similarityFunction = similarityFunction;
   }


   /**
    * Set a new similarity measure.
    * 
    * @param similarityFunction Similarity measure for two words (argument candidates)
    */
   public void setSimilarityFunction (Similarity similarityFunction) {
      this.similarityFunction = similarityFunction;
   }
   
   /**
    * ABSTRACT!
    * Find the best alignment of the unlabeled sentence to the labeled sentence.
    * 
    * @param argumentsLabeled  Argument candidates on LABELED side.
    * @param argumentsUnlabeled  Argument candidates on UNLABELED side.
    * @param sentenceUnlabeled  LABELED seed sentence.
    * @param predicateUnlabeled  LABELED predicate (that is currently processed).
    * @param sentenceLabeled  UNLABELED expansion sentence.
    * @param predicateLabeled  UNLABELED predicate (that is currently processed).
    * @return Best alignment and its score for the two sentences.
    */
   public abstract SentenceScoreElement findBestAlignment(List<Word> argumentsLabeled, List<Word> argumentsUnlabeled, 
         SRLSentence sentenceUnlabeled, Word predicateUnlabeled, SRLSentence sentenceLabeled, Word predicateLabeled);


   /**
    * Returns a name for the alignment finder (class name)
    * @return some identifying name.
    */
   public String getName() {
      return this.getClass().getSimpleName() + "(" + ((this.similarityFunction == null)?"null":this.similarityFunction.getName()) +")";
   }
      
   
}
