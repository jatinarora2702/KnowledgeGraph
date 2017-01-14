// (c) Wiltrud Kessler
// 14.01.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/



package de.uni_stuttgart.ims.expansion.candidates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.nlpbase.nlp.POSUtils;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;
import de.uni_stuttgart.ims.nlpbase.nlp.POSUtils.POSCategory;

/**
 * Abstract wrapper for implementations to create argument candidates.
 * 
 * @author kesslewd
 *
 */
public abstract class ArgumentCreator {
   
   
   /**
    * Two words in a sentence are only included if their distance
    * in the sentence (measured in number of tokens) is smaller than this value.
    */
   protected static int distanceLimit = 20;

   /**
    * If this is set to true, for any preposition or conjunction in the results,
    * its direct children are added to the result.
    */
   public boolean doCollapse = true;

   /**
    * If this is set to true, remove the predicate from the list of candidates.
    */
   public boolean filterOutPredicate = true;

   /**
    * How many candidates extracted in total.
    */
   public int allCandSetSize = 0;
   
   /**
    * How many times a candidate set was extracted.
    */
   public int numberCandSets = 0;
   
   /**
    * How many times a candidate set was empty.
    */
   public int numberCandSetsEmpty = 0;
   
   

   /**
    * List of allowed POS categories
    */
   protected static List<POSCategory> argumentPOSCategories = Arrays.asList(
         POSCategory.NOUN, POSCategory.PRONOUN,
         POSCategory.ADJECTIVE, POSCategory.ADVERB, POSCategory.VERB, 
         POSCategory.DETERMINER,
         POSCategory.NUMBER // include this for model names, e.g., D60
         //POSCategory.PREPOSITION, // if you include this, check collapsing!
         //POSCategory.CONJUNCTION // if you include this, check collapsing!
      );
   

   
   /**
    * Checks if the word is the ROOT of the dependency tree.
    * Check is done by seeing if ID=0.
    * 
    * @param word The word to be checked.
    * @return true if it is root, 
    *    false in every other case (including null)
    */
   protected static boolean isRoot (Word word) {
      if (word == null) {
         return false;
      }
      if (word.getId() == 0) { 
         return true;
      }
      return false;
   }
   
   
   /**
    * Filter out
    * root,
    * punctuation,
    * words too far from the predicate (distance > this.distanceLimit),
    * wrong POS category (not in this.argumentPOSCategories).
    * 
    * @param word The word to be checked.
    * @param predicate The predicate we want to get the arguments for.
    * @param sentence The sentence where the predicate and the word occur.
    * @return false if some filtering condition is met, else true.
    */
   protected static boolean isValidCandidate (Word word, Word predicate, SRLSentence sentence) {

      // Filter out root
      if (isRoot(word))
         return false;
      
      // Filter out punctuation
      if (POSUtils.isPunctuationWord(word.getForm()))
         return false;
      
      // Filter out words too far away
      if (java.lang.Math.abs(sentence.compareSequence(predicate, word)) > distanceLimit)
         return false;
      
      // Filter out bad POS categories
      if (!argumentPOSCategories.contains(POSUtils.getPOSCategory(word.getPOS()))) {
         //System.out.println("FILTER OUT: " + word + " category " + POSUtils.getPOSCategory(word.getPOS()));
         return false;
      }

      return true;
   }
   
   

   /**
    * If the word is a preposition or conjunction,
    * its direct children are returned.
    * 
    * If the word is a conjunction, it is represented as
    * Head -> Word -> and -> Word
    * We want to include the direct children.
    * TODO re-attach to the head??
    * 
    * @param word The word to be checked.
    * @return direct children, or empty set.
    */
   private static Set<Word> collapsePrepConj (Word candidate) {

      // For prepositions: 
      // get direct children instead
      if (POSUtils.isPrepositionPOS(candidate.getPOS())) {
         return candidate.getDirectChildren();
      }

      // For conjunctions: 
      // get direct children instead
      // TODO is this actually a good strategy??
      if (POSUtils.isConjunctionPOS(candidate.getPOS()) ) {
         return candidate.getDirectChildren();
      }

      return new HashSet<Word>();
   }

   

   /**
    * Removes invalid candidates according to global rules
    * collapses prepositions
    * and sorts the list by word ID.
    * 
    * @param results
    * @param predicate
    * @param sentence
    * 
    * @return List of words that are candidates.
    */   
   private List<Word> filterPossibleArguments (List<Word> results, Word predicate, SRLSentence sentence) {
     
      // Collapse edges [if wanted]
      if (doCollapse) {
         List<Word> toAdd = new ArrayList<Word>();
         for (Word word : results) {
            toAdd.addAll(collapsePrepConj(word));
         }
         for (Word word : toAdd) {
            if (!results.contains(word)) {// avoid adding a word twice (may happen with collapse)
               results.add(word);
            }
         }
      }
      
      // Remove invalid candidates and predicate itself (if wanted)
      List<Word> toDelete = new ArrayList<Word>();
      for (Word word : results) {
         if (!isValidCandidate(word, predicate, sentence) | (filterOutPredicate & word == predicate))
            toDelete.add(word);   
      }
      for (Word word : toDelete) {
         results.remove(word);
      }
      
      // Sort by id (DEBUG)
      Collections.sort(results, new Comparator<Word>(){
         @Override
         public int compare(Word arg0, Word arg1) {
            return arg0.getId() - arg1.getId();
            }     
      });
      
      // Statistics
      allCandSetSize+=results.size();
      numberCandSets+=1;
      if (results.size() == 0) numberCandSetsEmpty+=1;
     // System.out.println("-"+this.getName() + " " + allCandSetSize + " " + numberCandSets + " " + predicate + " " + results);
      
      return results;
   
   }

   
   /**
    * Get candidates for arguments (labeled side).
    * Calls implementation-specific methods to get the argument list and then
    * calls 'filterPossibleArguments' to filter according to global rules.
    * 
    * @return List of words that are candidates.
    */   
   public List<Word> getPossibleArgumentsLabeled (Word predicate, SRLSentence sentence) {
      List<Word> results = this.getPossibleArgumentsSpecificLabeled(predicate, sentence);
      return filterPossibleArguments(results, predicate, sentence);
   }

   /**
    * Get candidates for arguments (unlabeled side).
    * Calls implementation-specific methods to get the argument list and then
    * calls 'filterPossibleArguments' to filter according to global rules.
    * 
    * @return List of words that are candidates.
    */   
   public List<Word> getPossibleArgumentsUnlabeled (Word predicate, SRLSentence sentence, Word labeledPredicate, SRLSentence labeledSentence) {
      List<Word> results = this.getPossibleArgumentsSpecificUnlabeled(predicate, sentence, labeledPredicate, labeledSentence);
      return filterPossibleArguments(results, predicate, sentence);
   }
   

   
   /**
    * Get candidates for arguments on labeled side.
    * Abstract, must be implemented by subclasses.
    * 
    * @return List of words that are candidates.
    */   
   protected abstract List<Word> getPossibleArgumentsSpecificLabeled 
         (Word predicate, SRLSentence sentence);

   
   /**
    * Get candidates for arguments on unlabeled side.
    * Abstract, must be implemented by subclasses.
    * 
    * @return List of words that are candidates.
    */   
   protected abstract List<Word> getPossibleArgumentsSpecificUnlabeled (
         Word predicate, SRLSentence sentence, 
         Word labeledPred, SRLSentence labeledSent);
   
   
   /**
    * Give a human-readable name for this subclass that contains all relevant parameter settings.
    * 
    * @return Nice name to put on a name tag.
    */   
   public abstract String getName ();

   
   /**
    * Give some statistics.
    * 
    * @return Some sentence with some relevant numbers.
    */   
   public String getStatistics () {
      return String.format("avg. candidate set size: %.2f/%.2f (%d/%d/%d)", 
            (double)allCandSetSize/(double)numberCandSets, (double)allCandSetSize/(double)(numberCandSets-numberCandSetsEmpty),
            allCandSetSize, numberCandSets, numberCandSetsEmpty);
   };
   
   
   /**
    * Reset statistics.
    */   
   public void resetStatistics () {
      allCandSetSize = 0;
      numberCandSets = 0;
      numberCandSetsEmpty = 0;
   };
   

}
