// (c) Wiltrud Kessler
// 20.04.2015
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Two words are similar if they are either both
 * product names or not.
 * 
 * @author kesslewd
 *
 */
public class ProductNameSimilarity extends Similarity {
   

   /**
    * Check if something is a model name.
    * A model name has to have at least one digit
    * and at least one letter.
    * 
    * @param form The word to check.
    * @return TRUE if it is a model name.
    */
   private static boolean isModelName(String form) {
      
      boolean hasDigit = false;
      boolean hasLetter = false;
      
      for (Character letter : form.toCharArray()) {
         if (Character.isDigit(letter)) {
            hasDigit = true;
         }

         if (Character.isLetter(letter)) {
            hasLetter = true;
         }
      }
      
      return hasDigit & hasLetter;
   }
   
   

   /**
    * Return 1 if both are product names or both are not, else 0.
    * A product name contains at least one digit and at least one letter.
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
   public ExplainedSimilarityValue getExplainedSimilarity (
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {


      // Treat cases where something is null.
      // The check returns similarity and explanation for the different cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(word1, word2);
      if (nullCheck != null)
         return nullCheck;     
      
      boolean isProductWord1 = isModelName(word1.getForm());
      boolean isProductWord2 = isModelName(word2.getForm());
      

      return new ExplainedSimilarityValue((isProductWord1 == isProductWord2)?1.0:0.0, "word1 " + isProductWord1 + " word2 " + isProductWord2);
      
   }
   

   /**
    * Get name.
    * 
    * @return Class name
    */
   @Override
   public String getName() {
      return "ProductNameSimilarity";
   }
   

}
