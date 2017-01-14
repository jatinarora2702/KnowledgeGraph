// (c) Wiltrud Kessler
// 07.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.alignment;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uni_stuttgart.ims.nlpbase.nlp.Word;
import de.uni_stuttgart.ims.util.HashMapHelpers.Triple;


/**
 * Store the alignment of one word to one other word.
 * An alignment can also be with 'null'.
 * A word can be aligned only with exactly one word,
 * there is no possibility for 1-to-n or n-to-m alignments.
 * The words may or may not be from the same sentence.
 * 
 * @author kesslewd
 *
 */
public class WordAlignment {

   

   /**
    * An alignment maps one word to exactly one word.
    * TODO: Sorting is dependent only on id, if two words are from
    *    different sentences but have the same id, they are regarded
    *    as the same word.
    *    Allow this case?
    */
   private Map<Word, Triple<Word,Double,String>> alignment = new TreeMap<Word,Triple<Word,Double,String>>(new Comparator<Word>(){
      @Override
      public int compare(Word arg0, Word arg1) {
         return arg0.getId() - arg1.getId();
         }     
   });
   
   
   
   /**
    * Add alignment for word1 with word2.
    * Note that you cannot add a key twice.
    * Criteria for the key is the word id, so
    * you cannot add two words with the same
    * id from different sentences.
    * 
    * @param word1 Key (word to be aligned).
    * @param word2 Value (aligned word).
    * @param similarity Similarity of the words
    */
   public void add (Word word1, Word word2, double similarity) {
      alignment.put(word1, new Triple<Word,Double,String>(word2, similarity,""));
   }

   
   /**
    * Add alignment for word1 with word2.
    * Note that you cannot add a key twice.
    * Criteria for the key is the word id, so
    * you cannot add two words with the same
    * id from different sentences.
    * 
    * @param word1 Key (word to be aligned).
    * @param word2 Value (aligned word).
    * @param similarity Similarity of the words
    * @param explanation Similarity explanation
    */
   public void add (Word word1, Word word2, double similarity, String explanation) {
      alignment.put(word1, new Triple<Word,Double,String>(word2, similarity,explanation));
   }

   /**
    * Remove any alignment for word1.
    * 
    * @param word1
    */
   public void remove (Word word1) {
      alignment.remove(word1);
   }

   /**
    * Returns the word that is aligned with the key word.
    * 
    * @param word1
    * @return The aligned word or null in case there is
    *    no alignment for this word or the word is
    *    aligned with null.
    */
   public Word getAligned (Word word1) {
      if (alignment.get(word1) != null)
         return alignment.get(word1).x;
      else
         return null;
   }

   /**
    * Returns the similarity of the word that is aligned with the key word.
    * 
    * @param word1
    * @return The word similarity or -1 in case there is
    *    no alignment for this word or the word is
    *    aligned with null.
    */
   public double getAlignmentSimilarity (Word word1) {
      if (alignment.get(word1) != null)
         return alignment.get(word1).y;
      else
         return -1;
   }

   /**
    * Returns the explanation for the similarity value
    *  of the word that is aligned with the key word.
    * 
    * @param word1
    * @return The word explanation (may be "" or null)
    */
   public String getAlignmentSimilarityExplanation (Word word1) {
      if (alignment.get(word1) != null)
         return alignment.get(word1).z;
      else
         return "";
   }

   /**
    * Get all words for which we have an alignment.
    * 
    * @return Unordered set of all keys.
    */
   public Set<Word> getKeys () {
      return alignment.keySet();
   }


   
   /** 
    * Makes a shallow copy of this alignment,
    * i.e. the new alignment contains pointers to the
    * same objects.
    * 
    * @return Shallow copy of current alignment.
    */
   public WordAlignment copyAlignment() {
      WordAlignment newAlignment = new WordAlignment();
      for (Entry<Word, Triple<Word, Double, String>> entry : this.alignment.entrySet()) {
         newAlignment.add(entry.getKey(), entry.getValue().x, entry.getValue().y, entry.getValue().z);
      }
      return newAlignment;
   }



   /**
    * String representation. Entries are sorted by
    * id of key in the sentence.
    * TODO: include explanation of similarity?
    */
   public String toString () {
      String str = "{";
      boolean first = true;
      for (Entry<Word, Triple<Word, Double, String>> entry : this.alignment.entrySet()) {
         
         if (!first) {
            str += ", ";
         } else {
            first = false;
         }
         
         // Key word
         if (entry.getKey() != null) {
            str += entry.getKey().getForm() + "=";
         } else {
            str += "null=";
         }

         // Aligned word
         // This always has a similarity value and explanation
         if (entry.getValue().x != null) {
            str += String.format("%s (%.3f %s)", entry.getValue().x.getForm(), entry.getValue().y, entry.getValue().z);
         } else { // aligned with null
            str += "null (" + entry.getValue().y + ")";
         }
      }
      return str + "}";
   }

   public void changeSimilarity(Word word1, double thisSim) {
      alignment.get(word1).y = thisSim;      
   }
   
}
