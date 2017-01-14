// (c) Wiltrud Kessler
// 07.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.io.Closeable;
import java.io.IOException;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Abstract class for similarity measures between two words.
 * 
 * A similarity is a double value between 0 and 1 
 * where 1 indicates maximum similarity (identity) 
 * and 0 indicates no similarity.
 * 
 * @author kesslewd
 *
 */
public abstract class Similarity implements Closeable {

   /**
    * Just a wrapper to be able to return a similarity
    * value together with its explanation.
    */
   public static class ExplainedSimilarityValue {
      public ExplainedSimilarityValue (double similarityValue, String explanation){
         this.similarityValue = similarityValue;
         this.explanation = explanation;
      }
      public ExplainedSimilarityValue (double similarityValue){
         this(similarityValue, "");
      }
      public double similarityValue;
      public String explanation;
      public String toString (){
         return String.format("%.6f", similarityValue);
      }
      public String toLongString (){
         return String.format("%.6f", similarityValue) + " " + explanation;
      }
   }


   /**
    * Abstract function to be implemented by subclasses.
    * Give a measure of similarity of the two words.
    * Subclasses may use the predicate or the sentence for calculations
    * if they want.
    * Give some sort of explanation in the string.
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
   public abstract ExplainedSimilarityValue getExplainedSimilarity(
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2);
   
   /**
    * Give a measure of similarity of the two words.
    * Calls getExplainedSimilarity and throws away explanation.
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
   public double getSimilarity(
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {
      return this.getExplainedSimilarity(sentence1, predicate1, word1, sentence2, predicate2, word2).similarityValue;
   }

   
   /**
    * Does checks for 'null' that are needed in many subclasses.
    * Always checks the words.
    * Predicates and sentences are only checked if the corresponding
    * boolean indicators are set to TRUE. 
    * 
    * @param sentence1 Sentence that contains the FIRST word.
    * @param predicate1 Predicate that the FIRST word is an 
    *    argument candidate of (must be in sentence1).
    * @param word1 The FIRST word.
    * @param sentence2 Sentence that contains the SECOND word.
    * @param predicate2 Predicate that the SECOND word is an 
    *    argument candidate of (must be in sentence1).
    * @param word2 The SECOND word.
    * @param checkSentences Whether to perform checks for the two
    *    sentences or not.
    * @param checkPredicates Whether to perform checks for the two
    *    sentences or not.
    * @return 
    *    1 if both are null, 
    *    0 if only one is null,
    *    null if none is null.
    */
   protected ExplainedSimilarityValue checkNull (
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2,
         boolean checkSentences, boolean checkPredicates) {
      
      // TODO what about both are null, return 1 or 0??

      // Return 1 if both of the words are null.
      if (word1 == null & word2 == null)
         return new ExplainedSimilarityValue(1, "both words are null");
      
      // Return 0 if one of the words is null.
      if (word1 == null || word2 == null)
         return new ExplainedSimilarityValue(0, "one word is null");

      // Return 0 if one of the predicates is null.
      // (only if this should be checked)
      if (checkPredicates) {
         if (predicate1 == null || predicate2 == null)
            return new ExplainedSimilarityValue(0, "one predicate is null");
      }

      // Return 0 if one of the predicates is null.
      // (only if this should be checked)
      if (checkSentences) {
         if (sentence1 == null || sentence2 == null)
            return new ExplainedSimilarityValue(0, "one sentence is null");
      }

      // Everything is fine, nothing is null
      return null;
   }
   

   /**
    * return checkNull(sentence1, predicate1, word1, sentence2, predicate2, word2, true, true); 
    */
   protected ExplainedSimilarityValue checkNull (
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {
      return checkNull(sentence1, predicate1, word1, sentence2, predicate2, word2, true, true);
   }

   /**
    * return checkNull(null, predicate1, word1, null, predicate2, word2, false, true); 
    */
   protected ExplainedSimilarityValue checkNull (Word predicate1, Word word1, Word predicate2, Word word2) {
      return checkNull(null, predicate1, word1, null, predicate2, word2, false, true);
   }

   /**
    * return checkNull(sentence1, null, word1, sentence2, null, word2, true, false); 
    */
   protected ExplainedSimilarityValue checkNull (SRLSentence sentence1, Word word1, SRLSentence sentence2, Word word2) {
      return checkNull(sentence1, null, word1, sentence2, null, word2, true, false);      
   }

   /**
    * return checkNull(null, null, word1, null, null, word2, false, false); 
    */
   protected ExplainedSimilarityValue checkNull (Word word1, Word word2) {
      return checkNull(null, null, word1, null, null, word2, false, false);
   }
   
   

   /**
    * Maximum possible similarity (identity)
    * 
    * @return 1
    */
   public double getMaximum() {
      return 1.0;
   }

   /**
    * Minimum possible similarity (not similar at all)
    * 
    * @return 0
    */
   public double getMinimum() {
      return 0.0;
   }
   

   /**
    * Get name.
    * 
    * @return Short informative name.
    */
   public String getName() {
      return this.getClass().getSimpleName();
   }

   /**
    * May be used to close any open resources.
    * 
    * @throws Exception Oups, something went wrong.
    */
   public void close() throws IOException {
   }
   
}
