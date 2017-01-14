// (c) Wiltrud Kessler
// 19.08.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Dummy similarity that always returns the same value.
 * 
 * @author kesslewd
 *
 */
public class DummySimilarity extends Similarity {

   /**
    * The similarity value to return.
    */
   private int value;
   
   /**
    * Dummy similarity that always returns the same value.
    * 
    * @param value The similarity value to return
    *    (set it to 1 or 0).
    */
   public DummySimilarity (int value) {
      this.value = value;
   }

   /**
    * Returns always the same number (set in constructor).
    * 
    * Uses none of the parameters.
    * 
    * @param sentence1 Sentence that contains the FIRST word.
    * @param predicate1 Predicate that the FIRST word is an 
    *    argument candidate of (must be in sentence1).
    * @param word1 The FIRST word.
    * @param sentence2 Sentence that contains the SECOND word.
    * @param predicate2 Predicate that the SECOND word is an 
    *    argument candidate of (must be in sentence1).
    * @param word2 The SECOND word.
    * @return A double value between 0 and 1 
    *    where 1 indicates maximum similarity (identity) 
    *    and 0 indicates no similarity.
    */
   @Override
   public ExplainedSimilarityValue getExplainedSimilarity (
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {
      return new ExplainedSimilarityValue(value);
   }

}
