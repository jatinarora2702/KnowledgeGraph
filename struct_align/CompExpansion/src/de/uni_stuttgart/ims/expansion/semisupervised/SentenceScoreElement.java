// (c) Wiltrud Kessler
// 30.07.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.semisupervised;

import de.uni_stuttgart.ims.expansion.alignment.WordAlignment;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;



/**
 * Save an alignment and a scoring together with a sentence.
 */
public class SentenceScoreElement implements Comparable<SentenceScoreElement> {
   
   public SRLSentence sentence;
   public double similarity;
   public WordAlignment alignment;
   public Word predicate;
   
   /**
    * Save an alignment and a scoring together with a sentence.
    * 
    * @param sentence The unlabeled sentence parse-tree.
    * @param predicate The unlabeled predicate.
    * @param alignment An alignment of the unlabeled sentence with the labeled sentence.
    * @param similarity The similarity score of this sentence and the labeled sentence
    *    with the given alignment.
    */
   public SentenceScoreElement (SRLSentence sentence, Word predicate, WordAlignment alignment, double similarity) {
      this.sentence = sentence;
      this.alignment = alignment;
      this.similarity = similarity;
      this.predicate = predicate;
   }
   
   /**
    * A dummy that has only a score.
    * 
    * @param similarity Some value.
    */
   public SentenceScoreElement (double similarity) {
      this.similarity = similarity;
   }

   /** Compares two sentences with respect to their scores.
    * 
    * @return 0 if the similarities are the same
    *    >1 if this one is better
    *    <1 if the other is better
    */
   @Override
   public int compareTo(SentenceScoreElement o) {
      int result = Double.compare(this.similarity, o.similarity);
      //if (result == 0) // they are equal TODO if I do this, the N-Best-List won't work anymore!
         //return this.sentence.toString().compareTo(o.sentence.toString());
      return result;
   }
   

   public String toString() {
      if (this.sentence != null)
         return this.similarity + " / " + this.alignment + " / " + this.sentence.toString();
      else
         return this.similarity + " / / ";
   }
   
   
}
