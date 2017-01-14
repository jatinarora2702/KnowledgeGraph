// (c) Wiltrud Kessler
// 03.04.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/



package de.uni_stuttgart.ims.expansion.candidates;

import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * A helper that adds all parents/children/siblings of the predicate
 * to the result list.
 * 
 * @author kesslewd
 *
 */
public abstract class ArgumentCreatorDependencies extends ArgumentCreatorUniform {




   /**
    * Add dependents of the predicate to the results list.
    * 
    * @param results
    * @param addAllLevels
    * @param predicate
    * @param sentence
    */
   protected void addDescendants (List<Word> results, boolean addAllLevels, Word predicate, SRLSentence sentence) {
      
      // Add all descendants or only direct children
      // (direct children are included in all descendants)
      // invalid candidates are filtered out in super.
      if (addAllLevels) {
         for (Word child : sentence.getDescendants(predicate)) {
               results.add(child);
         }
      
      } else {      
         for (Word child : predicate.getDirectChildren()) {
               results.add(child);
         }
         
      }
   }
   
   /**
    * Add parents of the predicate to the results list.
    * 
    * @param results
    * @param addAllLevels
    * @param predicate
    * @param sentence
    */
   protected void addAncestors (List<Word> results, boolean addAllLevels, Word predicate, SRLSentence sentence) {

      // Add all ancestors or only direct parent
      // (direct parent is included in all descendants)
      if (addAllLevels) {
         // Path includes self, direct parent and ROOT
         for (Word ancestor : sentence.getPathToRoot(predicate)) { 
            
            // Filter out root
            if (isRoot(ancestor))
               continue;
            
            // Filter out predicate itself
            if (ancestor == predicate)
               continue;

            // Add only valid candidates 
            // (i.e., do NOT do preposition collapsing)
            if (isValidCandidate(ancestor, predicate, sentence)) {
               results.add(ancestor);
            }
         }

      } else  {
         Word predicateHead = predicate.getHead();

         if (!isRoot(predicateHead) && isValidCandidate(predicateHead, predicate, sentence)) {
            results.add(predicateHead);
         }
         
      }

   }
   
   /**
    * Add sibligns of the predicate to the results list.
    * 
    * @param results
    * @param addAllLevels
    * @param predicate
    * @param sentence
    */
   protected void addSiblings (List<Word> results, boolean addAllLevels, Word predicate, SRLSentence sentence) {

      Word predicateHead = predicate.getHead();

   
      // Add direct siblings or siblings' children
      for (Word child : predicateHead.getDirectChildren()) {
         
         // Filter out predicate itself
         if (child == predicate)
            continue;

         // invalid candidates are filtered out in super.
         results.add(child);

         // Add siblings' children, if wanted
         if (addAllLevels) {
            for (Word grandchild : sentence.getDescendants(child)) {
               results.add(grandchild);
            }
         }
      
         
      }
      
   }
   
   
   
   

   @Override
   public String getName () {
      return "ArgumentCreatorDependenciesAbstract";
   }
   
   
}
