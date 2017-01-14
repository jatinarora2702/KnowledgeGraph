// (c) Wiltrud Kessler
// 14.01.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.candidates;

import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * 
 * Possible argument candidates are seen from the predicate:
 *    - descendents (direct or all levels)
 *    - parents head (direct or all levels)
 *    - the siblings (direct or all levels)
 *    
 * @author kesslewd
 *
 */
public class ArgumentCreatorDependencyFiltered extends ArgumentCreatorDependencies {

   /**
    * Extract none of this type, direct [children/parents/siblings] or all levels
    */
   public static enum mode {no, direct, all};

   /**
    */
   private boolean useDirectChildren;

   /**
    * Includes 'useDirectChildren'.
    */
   private boolean useAllDescendants;

   /**
    */
   private boolean useDirectParent;

   /**
    * Includes 'useDirectParent'.
    */
   private boolean useAllAncestors;

   /**
    */
   private boolean useSiblings;

   /**
    * Includes 'useSiblings'.
    */
   private boolean useSiblingDescendants;

   /**
    * Name prefix
    */
   private String name = "ArgumentCreatorDependencyFiltered";

   
   /**
    * Set which children/parents/siblings you want to extract.
    * 
    * @param modeDescendants no (= none), direct (= direct dependant of predicate),
    *    all (= arbitrary deep)
    * @param modeAncestors no (= none), direct (= head of predicate), 
    *    all (= all nodes up until root, excluding root itself)
    * @param modeSiblings no (= none), direct (= direct dependent of head of predicate), 
    *    all (= all children of head of predicate)
    */
   public ArgumentCreatorDependencyFiltered (mode modeDescendants, mode modeAncestors, mode modeSiblings) {
      name += " (" + modeDescendants + " children, ";
      switch (modeDescendants) {
      case no: 
         useDirectChildren = false;
         useAllDescendants = false;
         break;
      case direct:
         useDirectChildren = true;
         useAllDescendants = false;
         break;
      case all:
         useDirectChildren = true;
         useAllDescendants = true;
         break;
      }
      name += modeAncestors + " parents, ";
      switch (modeAncestors) {
      case no: 
         useDirectParent = false;
         useAllAncestors = false;
         break;
      case direct:
         useDirectParent = true;
         useAllAncestors = false;
         break;
      case all:
         useDirectParent = true;
         useAllAncestors = true;
         break;
      }
      name += modeSiblings + " siblings)";
      switch (modeSiblings) {
      case no: 
         useSiblings = false;
         useSiblingDescendants = false;
         break;
      case direct:
         useSiblings = true;
         useSiblingDescendants = false;
         break;
      case all:
         useSiblings = true;
         useSiblingDescendants = true;
         break;
      }
   }
   
   
   
   /**
    * Gives a list of possible argument candidates for a predicate
    * in the sentence. The predicate has to be in the sentence.
    * The predicate does not have to be marked as predicate.
    * Possible argument candidates are:
    *    - descendents (direct or all levels)
    *    - parents head (direct or all levels)
    *    - the siblings (direct or all levels)
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
      
      // Add all descendants or only direct children
      // (direct children are included in all descendants)
      if (useAllDescendants | useDirectChildren)
         super.addDescendants(results, useAllDescendants, predicate, sentence);
      

      // Add all ancestors or only direct parent
      // (direct parent is included in all descendants)
      if (useAllAncestors | useDirectParent)
         super.addAncestors(results, useAllAncestors, predicate, sentence);

      
      // Add direct siblings or siblings' children
      if (useSiblings | useSiblingDescendants)
         super.addSiblings(results, useSiblingDescendants, predicate, sentence);  
      
      // Will be filtered by super
      return results;
   }
   

   @Override
   public String getName () {
      return this.name;
   }

   
}
