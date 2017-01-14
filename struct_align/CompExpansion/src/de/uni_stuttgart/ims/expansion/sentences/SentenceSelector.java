// (c) Wiltrud Kessler
// 24.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.sentences;

import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Abstract wrapper for implementations to get possible expansion
 * sentences for a labeled sentence and predicate.
 * 
 * @author kesslewd
 *
 */
public abstract class SentenceSelector {

   /**
    * This is the pool of all unlabeled sentences to chose from.
    * 
    * @param allUnlabeledSentences List of parsed unlabeled sentences.
    */
   public abstract void setUnlabeledSentencesPool (List<SRLSentence> allUnlabeledSentences);

   
   /**
    * From all unlabeled sentences, get those that are possible expansion sentences
    * for this seed sentence and this predicate.
    * 
    * @param sentence A labeled seed sentence.
    * @param predicate A word in the seed sentence.
    */
   public abstract Set<SRLSentence> getUnlabeledSentences (SRLSentence sentence, Word predicate);
   
   
   /**
    * For this seed, predicate and unlabeled sentence, what is the predicate in the
    * unlabeled sentence that corresponds to the labeled predicate
    * (this is the anchor for the whole alignment stuff).
    * 
    * @param labeledSentence A labeled seed sentence.
    * @param labeledPredicate A word in the seed sentence.
    * @param unlabeledSentence An unlabeled sentence that has been determined to be
    *          a possible expansion sentence for the given seed and predicate.
    * @return One or several words that are compatible to the seed predicate.
    *          May be null or empty if no matching word is found 
    *          (which should not happen, but who knows).
    */
   public abstract List<Word> getPredicatesFromUnlabeledSentence (SRLSentence labeledSentence, Word labeledPredicate, SRLSentence unlabeledSentence);

   
   /**
    * Give a human-readable name for this subclass that contains all relevant parameter settings.
    * 
    * @return Nice name to put on a name tag.
    */   
   public abstract String getName();


}
