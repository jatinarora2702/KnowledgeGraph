// (c) Wiltrud Kessler
// 14.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Measures the similarity between two words as a composite similarity
 * between a set of n similaritiss.
 * All measures are weighted equally with 1/n.
 * 
 * @author kesslewd
 *
 */
public class ComposedSimilarity extends Similarity {


   /**
    * List of similarity measures that are to be combined.
    */
   private List<Similarity> similaritiesList = new ArrayList<Similarity>();

   
   /**
    * [dev] Debug mode.
    */
   public boolean debug = false;
   

   /**
    * Add similarity measure.
    * 
    * @param sim A similarity measure
    */
   public void addSimilarityMeasure (Similarity sim) {
      if (sim != null) {
         this.similaritiesList.add(sim);
      }
   }
   
   
   /**
    * Measures the similarity by using the given similarity measure .
    * If one of the words is null, 0 is returned.
    * 
    * @param sim Similarity measure.
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
   private ExplainedSimilarityValue getExplainedSimilarity(
         Similarity sim, 
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {      
      
      ExplainedSimilarityValue result = sim.getExplainedSimilarity(sentence1, predicate1, word1, sentence2, predicate2, word2);
      if (result.similarityValue < 0) {
         System.out.println("ERROR!!! Similarity < 0 of " + word1==null?"null":word1.getForm() + " / " + word2==null?"null":word2.getForm() + " : "+ result);
         result.similarityValue = 0;
      }
      if (result.similarityValue > 1) {
         System.out.println("ERROR!!! Similarity > 1 of " + word1==null?"null":word1.getForm() + " / " + word2==null?"null":word2.getForm() + " : "+ result);
         result.similarityValue = 1;
      }
      return result;

   }


   /**
    * Measures the similarity between two words as a composite similarity
    * between all given similarity measures.
    * All measures are weighted equally with 1/n.
    * 
    * Which parameters are actually used depends on whatever is in 
    * the list of similarities that is calculated.
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
      
      int total = similaritiesList.size();
      
      if (total == 0) {
         System.err.println("Error! No simililarity measure given! All similarities will be 0.");
         return new ExplainedSimilarityValue(0, "No simililarity measure given! All similarities will be 0.");
      }
      
      double result = 0;
      String partialresultsString = "";
      String debugpartialresultsString = "";
      
      for (Similarity simmy : similaritiesList) {
         ExplainedSimilarityValue partialresult = getExplainedSimilarity(simmy, sentence1, predicate1, word1, sentence2, predicate2, word2);
         // Add the values
         result += partialresult.similarityValue/total;
         // explanation string = list of single values
         partialresultsString += String.format("+%.3f",partialresult.similarityValue);
         // debug explanation string = long stuff 
         if (debug) debugpartialresultsString += " | " + simmy.getName() 
            + String.format("%.3f",partialresult.similarityValue)
            + " (" +partialresult.explanation + ")";
      }
      if (debug) System.out.println("total similiarty " + word1 + " / " + word2 + " = " + result + " || " + debugpartialresultsString);
           
      return new ExplainedSimilarityValue (result, partialresultsString.substring(1));

   }

   

   /**
    * Get name.
    * 
    * @return Class names of all included similarities
    */
   @Override
   public String getName() {
      String name = this.getClass().getSimpleName() + " [";
      for (Similarity simmy : similaritiesList) {
         name += simmy.getName() + ", ";
      }
      name = name.substring(0, name.length()-2) + "]";
      return name;
   }
   

   /**
    * Close all individual similarities.
    * @throws Exception 
    */
   public void close() throws IOException {
      for (Similarity simmy : similaritiesList) {
         simmy.close();
      }
   }

}
