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
 * Possible argument candidates are all ancestors and their
 * direct children (i.e., go up as much as you like and then maximum
 * one step down), as well as all descendents of the predicate.
 * 
 * @author kesslewd
 *
 */
public class ArgumentCreatorAncestorDescendant extends ArgumentCreatorUniform {


   private boolean useOnlyDirectChildren = false;
   

   /**
    * Gives a list of possible argument candidates for a predicate
    * in the sentence. The predicate has to be in the sentence.
    * The predicate does not have to be marked as predicate.
    * Possible argument candidates are all ancestors and their
    * direct children (i.e., go up as much as you like and then maximum
    * one step down), as well as all descendents of the predicate.
    * 
    * Punctuation and ROOT is filtered out.
    * The predicate itself is not included.
    * All words are filtered to have a distance smaller than distanceLimit.
    * If the word is an actual predicate, the list also only contains
    * the above mentioned words. It may be the case that real arguments
    * are not in this list.
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
      ArrayList<Word> ancestors = new ArrayList<Word>();

      // Get path to root (= only up).
      // Path includes self, direct parent and ROOT
      for (Word ancestor : sentence.getPathToRoot(predicate)) {
         
         // We want to get direct children of ROOT,
         // but not have the root itself as a result,
         // so add only to ancestor list, not to result
         if (isRoot(ancestor)) {
            ancestors.add(ancestor);
            continue;
         }
         
         // Filter out predicate itself (get children separately)
         if (ancestor == predicate){
            continue;
         }

         // Add rest to ancestor list and result list
         ancestors.add(ancestor);
         results.add(ancestor);
      }
      
      // For all ancestors, get all direct children (= one down)
      for (Word ancestor : ancestors) {
         for (Word child : ancestor.getDirectChildren()) {
            if (!results.contains(child) & child != predicate) {
               results.add(child);
            }
         }
      }
      
      if (useOnlyDirectChildren) {
         // Get all DIRECT children of the predicate
         for (Word child : predicate.getDirectChildren()) {
                results.add(child);
          }
      } else {
         // Get all descendents of the predicate
         for (Word child : sentence.getDescendants(predicate)) {
                results.add(child);
          }
      }

      
      // Will be filtered by super
      return results;
   }

   
   @Override
   public String getName() {
      return "ArgumentCreatorAncestorDesc";
   }

}
