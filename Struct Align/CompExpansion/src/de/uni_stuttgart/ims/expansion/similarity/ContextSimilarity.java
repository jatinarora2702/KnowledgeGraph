// (c) Wiltrud Kessler
// 12.02.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Measures the similarity of the context around the two words,
 * compared is the word immediately preceding the first word of the
 * subtree of word 1 resp. 2 
 * and the word immediately following the last word of the subtree
 * for word 1 and 2.
 * 
 * @author kesslewd
 *
 */
public class ContextSimilarity extends Similarity {
   

   /**
    * Adjust the relative importance of before and after similarity.
    * Alpha must be between 0 and 1.
    * alpha = 0 means consider only before similarity.
    * alpha = 1 means consider only after similarity.
    * Default value is 0.5
    */
   private double alpha = 0.5;

   /**
    * The similarity measure used to compare the words.
    */
   private Similarity sim;
   
   /**
    * If TRUE use the words around the subtree under the given word,
    * if FALSE use only words around the word itself.
    */
   private boolean useSubtree;

   /**
    * Measures the similarity of the context around the two words,
    * compared is the word immediately preceding and immediately following X.
    * where X can be the first/last word of the subtree of the word  (useSubtree = true)
    * or the word itself (useSubtree = false)
    *
    * @param sim Similarity measure used for the words
    * @param subtreesim If true, 
    */
   public ContextSimilarity (Similarity sim, boolean useSubtree) {
      this.sim = sim;
      this.useSubtree = useSubtree;
   }
   
   
   /**
    * Compare the words around (=immediately before/after) this subtree.
    * 
    * Uses the words and the sentences.
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
         SRLSentence sentence1, Word predicate1,
         Word word1, SRLSentence sentence2, Word predicate2, Word word2) {

      // Compare words around subtree

      // Treat cases where something is null.
      // The check returns similarity and explanation for the different cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(sentence1, word1, sentence2, word2);
      if (nullCheck != null)
         return nullCheck;
      
      int[] word1IDs = getMinMaxChildIndices(sentence1, word1);
      int[] word2IDs = getMinMaxChildIndices(sentence2, word2);
      
      Word before1 = sentence1.getWord(word1IDs[0]-1);
      Word after1 = sentence1.getWord(word1IDs[1]+1);

      Word before2 = sentence2.getWord(word2IDs[0]-1);
      Word after2 = sentence2.getWord(word2IDs[1]+1);

      // Compare Part of Speech and dependency relation
      double beforesim = this.sim.getSimilarity(sentence1, predicate1, before1, sentence2, predicate2, before2);
      double aftersim = this.sim.getSimilarity(sentence1, predicate1, after1, sentence2, predicate2, after2);
      double score = this.alpha * beforesim
            + (1-this.alpha) * aftersim;
      
      return new ExplainedSimilarityValue(score, "before " + before1 + " " + before2 + " " + beforesim + "; after " + after1 + " " + after2 + " " + aftersim);
      
   }

   
   /**
    * Get all children of the word and return the minimum and the maximum token id.
    * If there are no children, both will be equal to the token id of the word itself.
    * Also, if this.useSubtree is false, only the word itself will be considered.
    * 
    * @param sentence 
    * @param word
    * @return
    */
   private int[] getMinMaxChildIndices (SRLSentence sentence, Word word) {
      
      int[] minmax = new int[2];
      minmax[0] = word.getId(); // min
      minmax[1] = word.getId(); // max
      
      if (this.useSubtree) {
         for (Word child : sentence.getDescendants(word)) {
            int thisID = child.getId();
            minmax[0] = Math.min(thisID, minmax[0]);
            minmax[1] = Math.max(thisID, minmax[1]);         
         }
      }
      
      return minmax;
      
   }
   

   /**
    * Get name.
    * 
    * @return Class name plus name of included similarity and value of useSubtree
    */
   @Override
   public String getName() {
      String a = this.useSubtree?"Subtree":"Flat";
      return this.getClass().getSimpleName() + a + " [" + this.sim.getName() + "]";
   }
   
   
   
   
}
