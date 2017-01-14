// (c) Wiltrud Kessler
// 24.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/




package de.uni_stuttgart.ims.expansion.sentences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Abstract class for all index-based sentence selectors that first create
 * an inverted index that lists all sentences.
 * The key is left to the actual implementation.
 * 
 * Example:
 * key1 - sentence2, sentence15, sentence7, ...
 * key3 - sentence2, sentence8, sentence100, ...
 * key4 - sentence1, sentence2, sentence4, ...
 * 
 * What the key is is determined by the specific implementation ('getKeys').
 * The implementation then also has to provide a way of identifying a
 * compatible predicate in an unlabeled sentence ('getPredicatesFromUnlabeledSentence').
 * 
 * @author kesslewd
 *
 */
public abstract class SentenceSelectorIndexBased extends SentenceSelector {

   /**
    * All unlabeled sentences, indexed by key.
    */
   HashMap<String, Set<SRLSentence>> invertedIndex;
   

   /**
    * ABSTRACT!!
    * Get the key(s) for a word.
    * This keys are used to look up the possible expansion sentences
    * in the index (each one separately).
    * 
    * @param word A word.
    * @return A list of representations of this word that can be used 
    *       as keys for lookup in the inverted index.
    */
   protected abstract List<String> getKeys(Word word);

   
   /**
    * This is the pool of all unlabeled sentences to chose from.
    * 
    * @param allUnlabeledSentences List of parsed unlabeled sentences.
    */
   public void setUnlabeledSentencesPool (List<SRLSentence> allUnlabeledSentences) {
      
      // Create inverted index for faster predicate lookup
      invertedIndex = new HashMap<String, Set<SRLSentence>>();
      for (SRLSentence sent : allUnlabeledSentences) {
         for (Word word : sent.getWordList()) {
            
            // Get keys (dependent on subclass implementation)
            List<String> keylist = getKeys(word);

            
            // Add sentence to corresponding lists
            for (String key : keylist) {            
               Set<SRLSentence> listy = invertedIndex.get(key);
               if (listy == null) {
                  listy = new HashSet<SRLSentence>();
                  invertedIndex.put(key, listy);
               }
               listy.add(sent);
            }
         }
      }
   }
   
   
   /**
    * From all unlabeled sentences, get those that are possible expansion sentences
    * for this seed sentence and this predicate.
    * 
    * @param sentence A labeled seed sentence 
    *             (not actually used in this selector).
    * @param predicate A labeled predicate 
    *             (key is extracted from this word and looked up in the index).
    */
   public Set<SRLSentence> getUnlabeledSentences (SRLSentence sentence, Word predicate) {

      // Get keys (dependent on subclass implementation)
      List<String> keylist = getKeys(predicate);
      
      // Add sentence to corresponding lists
      Set<SRLSentence> myunlabeled = new HashSet<SRLSentence>();
      for (String key : keylist) {            
         Set<SRLSentence> partset = invertedIndex.get(key);
         if (partset != null)
            myunlabeled.addAll(partset);
      }
      
      return myunlabeled;
      
   }
   


}
