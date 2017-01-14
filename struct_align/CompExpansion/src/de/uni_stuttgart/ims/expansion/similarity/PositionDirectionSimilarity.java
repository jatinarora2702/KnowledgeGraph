// (c) Wiltrud Kessler
// 06.02.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Measures the similarity in position, i.e. the distance between 
 * the word and the predicate.
 * If the direction is different, i.e., one word is before and 
 * the other after the predicate, the distance is always 0.
 *  
 * @author kesslewd
 *
 */
public class PositionDirectionSimilarity extends Similarity {


   /**
    * Measures the similarity in position, i.e. the distance between words
    * expressed as 1/(distance in tokens).
    * If one of the words is null, 0 is returned.
    * 0 is returned if one word is before and the other after the predicate,
    * independent of distance.
    * 1 is returned if the words are at the same position.
    * 
    * Uses the words and the predicates.
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
   public ExplainedSimilarityValue getExplainedSimilarity(
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {
      
      // Treat cases where one or both words or predicates are null.
      // The check returns similarity and explanation for the different cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(predicate1, word1, predicate2, word2);
      if (nullCheck != null)
         return nullCheck;
      
      // Position is expressed in token ids and distance to pred
      int dist1 = word1.getId() - predicate1.getId();
      boolean sign1 = (dist1<0);
      int dist2 = word2.getId() - predicate2.getId();
      boolean sign2 = (dist2<0);
      
      // If they are on different sides of the predicate, return zero
      if (sign1 != sign2)
         return new ExplainedSimilarityValue(0, "dist1 " + dist1 + " dist2 " + dist2);
      
      // Otherwise, return 1/|dist1-dist2|
      return new ExplainedSimilarityValue(1.0 / (Math.abs(dist1-dist2)+1), "dist1 " + dist1 + " dist2 " + dist2);
   }
   
   
}
