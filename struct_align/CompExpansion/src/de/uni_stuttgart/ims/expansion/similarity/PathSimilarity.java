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
 * Measures the similarity of the nodes on the path
 * from the word to the predicate.
 * The 'up' and 'down' parts are compared separately
 * and a penalty is added if they differ in length.
 * 
 * @author kesslewd
 *
 */
public class PathSimilarity extends Similarity {

   /**
    * The similarity measure used to compare
    * the nodes on the path.
    */
   private Similarity sim;

   /**
    * Measures the similarity of the nodes on the path
    * from the word to the predicate.
    * Two words are compared by Similarity measure sim
    *
    * @param sim Similarity measure used to compare
    *     the nodes on the path.
    */
   public PathSimilarity (Similarity sim) {
      this.sim = sim;
   }
   
   

   /**
    * Measures the similarity of the words on the path
    * from the word to the predicate.
    * The 'up' and 'down' parts are compared separately
    * and a penalty is added if they differ in length.
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
     
      List<List<Word>> path1 = sentence1.getWordsOnPath(word1, predicate1);
      //System.out.println(path1);
      List<List<Word>> path2 = sentence2.getWordsOnPath(word2, predicate2);
      //System.out.println(path2);

      double result = 0;
      int total = 0;
      
      // TODO compare first i, where i=length of shorter
      // is there something better we could do?
      
      // go "up" from argument
      String explanation = " up";
      for (int i=0; i<Math.min(path1.get(0).size(), path2.get(0).size()); i++) {
         Word pathword1 = path1.get(0).get(i);
         Word pathword2 = path2.get(0).get(i);
         double partialresult = sim.getSimilarity(sentence1, predicate1, pathword1, sentence2, predicate2, pathword2);
         explanation += "/" + partialresult;
         result += partialresult;
         total++;
      }
      // penalize different length     
      int penalization = Math.max(path1.get(0).size(), path2.get(0).size()) - Math.min(path1.get(0).size(), path2.get(0).size());
      total += penalization;
      
      
      // go "down" 
      explanation += " down";
      for (int i=0; i<Math.min(path1.get(1).size(), path2.get(1).size()); i++) {
         Word pathword1 = path1.get(1).get(path1.get(1).size()-i-1);
         Word pathword2 = path2.get(1).get(path2.get(1).size()-i-1);
         double partialresult = sim.getSimilarity(sentence1, predicate1, pathword1, sentence2, predicate2, pathword2);
         explanation += "/" + partialresult;
         result += partialresult;
         total++;
      }
      // penalize different length
      penalization = Math.max(path1.get(1).size(), path2.get(1).size()) - Math.min(path1.get(1).size(), path2.get(1).size());
      total += penalization;
      
      return new ExplainedSimilarityValue (result/total, explanation);
   }

   
   /**
    * Get name.
    * 
    * @return Class name plus name of included similarity
    */
   @Override
   public String getName() {
      return this.getClass().getSimpleName() + " [" + this.sim.getName() + "]";
   }
   
   
}
