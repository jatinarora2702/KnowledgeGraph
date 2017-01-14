// (c) Wiltrud Kessler
// 14.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import de.uni_stuttgart.ims.nlpbase.nlp.POSUtils;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;
import de.uni_stuttgart.ims.nlpbase.nlp.POSUtils.POSCategory;

/**
 * Measures the syntactic similarity between two words.
 * Compared is the POS-tag and the dependency relation.
 * 
 * @author kesslewd
 *
 */
public class DependencyRelationSimilarity extends Similarity {

   /**
    * Adjust the relative importance of lexical and syntactical similarity.
    * Alpha must be between 0 and 1.
    * alpha = 0 means consider only POS similarity.
    * alpha = 1 means consider only dependency similarity.
    * Default value is 0.5
    */
   private double alpha = 0.5;


   /**
    * Measures the syntactic similarity between two words.
    * Compared is the Part-of-Speech-tag and the dependency relation.,
    * If one of the words is null, 0 is returned.
    * 1 is returned if the words have the exact same POS and
    * exactly the same dependency relation with their head. 
    * 
    * Uses only the words.
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
      
      // Treat cases where one or both words are null.
      // The check returns similarity and explanation for these cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(word1, word2);
      if (nullCheck != null)
         return nullCheck;

      // Compare Part of Speech and dependency relation
      double possim = getPOSSimilarity(word1, word2);
      double deprelsim = getDependencySimilarity(word1, word2);
      double score = this.alpha * possim
            + (1-this.alpha) * deprelsim;
      
      return new ExplainedSimilarityValue(score, "pos " + possim + " deprel " + deprelsim);
   }
   
   
   /**
    * If the two words have the exact same POS tag, return 1.
    * If the two words have a POS tag from the same category, return 0.5.
    * Else, return 0.
    * 
    * @param word1 One word.
    * @param word2 Another word.
    * @return A value between 0 and 1 indicating similarity.
    */
   private double getPOSSimilarity (Word word1, Word word2) {

      ExplainedSimilarityValue nullCheck = super.checkNull(word1, word2);
      if (nullCheck != null)
         return nullCheck.similarityValue;
      
      if (word1.getPOS().equals(word2.getPOS())) {
         return 1;
      } else {
         POSCategory category1 = POSUtils.getPOSCategory(word1.getPOS());
         if (category1 != null && !category1.equals(POSCategory.OTHER)) {
            if (POSUtils.haveSamePOSCategory(word1.getPOS(), word2.getPOS())) {
               return 0.5;
            }
         }
      }
      return 0;
   }
   

   /**
    * 
    * Compare dependency relation and heads.
    * 
    * @param word1 One word.
    * @param word2 Another word.
    * @return A value between 0 and 1 indicating similarity.
    */
   private double getDependencySimilarity (Word word1, Word word2) {

      ExplainedSimilarityValue nullCheck = super.checkNull(word1, word2);
      if (nullCheck != null)
         return nullCheck.similarityValue;
      
      double score = 0;

      if (word1.getDeprel().equals(word2.getDeprel())) {
         score += 0.5;
      }
      
      score += 0.5 * getPOSSimilarity(word1.getHead(), word2.getHead());
      
      return score;
   }
   

}
