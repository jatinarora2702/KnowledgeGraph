// (c) Wiltrud Kessler
// 24.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.sentences;

import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.util.StringWordMapping;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Index-based sentence selector, key is part of speech.
 * 
 * @author kesslewd
 *
 */
public class SentenceSelectorPOS extends SentenceSelectorIndexBased {   
   

   /**
    * Get the key(s) for a word.
    * This keys are used to look up the possible expansion sentences
    * in the index (each one separately).
    * Key is part of speech.
    * 
    * @param word A word.
    * @return A list of representations of this word that can be used 
    *       as keys for lookup in the inverted index.
    */
   protected List<String> getKeys (Word word) {
      List<String> keys = new ArrayList<String>();     
      keys.add("POS_" + word.getPOS());
      return keys;
   }
   

   /**
    * For this seed, predicate and unlabeled sentence, what is the predicate in the
    * unlabeled sentence that corresponds to the labeled predicate
    * (this is the anchor for the whole alignment stuff).
    * This corresponds to all words with the same part of speech tag.
    * 
    * @param labeledSentence A labeled seed sentence.
    * @param labeledPredicate A word in the seed sentence.
    * @param unlabeledSentence An unlabeled sentence that has been determined to be
    *          a possible expansion sentence for the given seed and predicate.
    * @return One or several words that are compatible to the seed predicate.
    *          May be null or empty if no matching word is found 
    *          (which should not happen, but who knows).
    */
   public List<Word> getPredicatesFromUnlabeledSentence (SRLSentence labeledSentence, 
         Word labeledPredicate, SRLSentence unlabeledSentence) {
      return StringWordMapping.identifyOnePOSAll(unlabeledSentence, labeledPredicate.getPOS(), false, false);
   }
   

   @Override
   public String getName() {
      return "SentSelIndexBased(POS)";
   }

}
