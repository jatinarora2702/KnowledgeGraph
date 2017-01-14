// (c) Wiltrud Kessler
// 03.04.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.candidates;

import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Possible argument candidates are all actual arguments (for both sides).
 * 
 * @author kesslewd
 *
 */
public class ArgumentCreatorRealArgs extends ArgumentCreatorUniform {

   
   /**
    * Gives a list of possible argument candidates for a predicate
    * in the sentence. The predicate has to be in the sentence.
    * The predicate does have to be marked as predicate.
    * Possible argument candidates are all actual arguments (for both sides).
    * [so you shouldn't use this for real unlabeled sentences, this is a pseudo-argument creator]
    * 
    * Punctuation and ROOT is filtered out.
    * The predicate itself is not included.
    * All words are filtered to have a distance smaller than distanceLimit.
    * 
    * @param predicate A word from the sentence.
    * @param sentence The sentence (with dependency structure).
    * @return A list of possible argument candidates.
    *    The list may be empty if the sentence contains only the predicate,
    *    or all possible candidates are filtered out, but not null.
    */
   @Override
   protected List<Word> getPossibleArgumentsSpecific(Word predicate, SRLSentence sentence) {

      ArrayList<Word> results = new ArrayList<Word>();
         
         for (Word arg : sentence.getArguments(predicate)) {
            results.add(arg);
         }
            
      // Will be filtered by super
      return results;
   }

   


   @Override
   public String getName() {
      return "ArgumentCreatorRealArgs";
   }

}
