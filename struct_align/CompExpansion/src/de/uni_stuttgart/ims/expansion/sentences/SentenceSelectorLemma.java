// (c) Wiltrud Kessler
// 24.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.sentences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.util.StringWordMapping;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Index-based sentence selector, keys are word form and lemma.
 * 
 * @author kesslewd
 *
 */
public class SentenceSelectorLemma extends SentenceSelectorIndexBased {
   

   /**
    * Get the key(s) for a word.
    * This keys are used to look up the possible expansion sentences
    * in the index (each one separately).
    * Keys are word form and lemma (lowercased).
    * 
    * @param word A word.
    * @return A list of representations of this word that can be used 
    *       as keys for lookup in the inverted index.
    */
   protected List<String> getKeys (Word word) {
      List<String> keys = new ArrayList<String>();
      
      // Add Lemma (always lowercase)
      String lemma = word.getLemma();
      keys.add(lemma);
      
      // Add form (for cases where it is not identical, do lowercase)
      if (!lemma.equalsIgnoreCase(word.getForm())) {
         keys.add(word.getForm().toLowerCase());
      }
      
      return keys;
   }
   

   /**
    * For this seed, predicate and unlabeled sentence, what is the predicate in the
    * unlabeled sentence that corresponds to the labeled predicate
    * (this is the anchor for the whole alignment stuff).
    * This corresponds to all words with the same word form or lemma.
    * 
    * @param labeledSentence A labeled seed sentence.
    * @param labeledPredicate A word in the seed sentence.
    * @param unlabeledSentence An unlabeled sentence that has been determined to be
    *          a possible expansion sentence for the given seed and predicate.
    * @return One or several words that are compatible to the seed predicate.
    *          May be null or empty if no matching word is found 
    *          (which should not happen, but who knows).
    */
   public List<Word> getPredicatesFromUnlabeledSentence (SRLSentence labeledSentence, Word labeledPredicate, SRLSentence unlabeledSentence) {
      
      // A tree-set does not permit duplicates.
      // Sort by word ids. 
      Set<Word> predicatesUnlabeled = new TreeSet<Word>(unlabeledSentence.wordSequenceComparator);

      // Check for same lemma
      List<Word> lemmata = StringWordMapping.identifyOneLemmaAll(unlabeledSentence, labeledPredicate.getLemma(), false, false);
      if (lemmata != null)
         predicatesUnlabeled.addAll(lemmata);

      // Check for same form.
      // Do even if the form is the same as the lemma
      // to find those cases where only forms match.
      List<Word> forms = StringWordMapping.identifyOneWordAll(unlabeledSentence, labeledPredicate.getForm(), false, false);
      if (forms != null)
         predicatesUnlabeled.addAll(forms);
      
      return new ArrayList<Word> (predicatesUnlabeled);
   }
   

   @Override
   public String getName() {
      return "SentSelIndexBased(Form+Lemma)";
   }

}
