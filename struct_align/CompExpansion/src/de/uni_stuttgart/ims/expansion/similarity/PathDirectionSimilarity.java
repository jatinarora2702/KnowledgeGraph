// (c) Wiltrud Kessler
// 06.02.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Measures the similarity in ups/downs on the path to the predicate.
 * 
 * @author kesslewd
 *
 */
public class PathDirectionSimilarity extends Similarity {


   /**
    * Measures the similarity by comparing the number of 'ups' and 'downs'
    * on the path between the word and the predicate.
    * 
    * Uses all parameters.
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

      // Treat cases where something is null.
      // The check returns similarity and explanation for the different cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(sentence1, predicate1, word1, sentence2, predicate2, word2);
      if (nullCheck != null)
         return nullCheck;
      
      // Get paths
      List<List<Word>> path1 = sentence1.getWordsOnPath(word1, predicate1);
      List<List<Word>> path2 = sentence2.getWordsOnPath(word2, predicate2);
      
      // Calculate similarity between paths length only
      int len1up = path1.get(0).size();
      int len2up = path2.get(0).size();
      int len1down = path1.get(1).size();
      int len2down = path2.get(1).size();
      
      double upSim = 1.0 / (Math.abs(len1up-len2up)+1);
      double downSim = 1.0 / (Math.abs(len1down-len2down)+1);
      
      return new ExplainedSimilarityValue(0.5 * upSim + 0.5 * downSim, "up " + upSim + " down " + downSim);
   }
   
   
   
}
