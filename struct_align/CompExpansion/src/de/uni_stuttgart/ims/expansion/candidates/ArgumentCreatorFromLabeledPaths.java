// (c) Wiltrud Kessler
// 03.04.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.candidates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.nlpbase.nlp.POSUtils;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Get paths from the predicate to the argument and all words on this paths.
 * (cf. Fuerstenau and Lapata 2009, 2012)
 * 
 * @author kesslewd
 *
 */
public class ArgumentCreatorFromLabeledPaths extends ArgumentCreator {


   /**
    * Paths from the predicate to all arguments 
    * in the labeled sentence.
    * 
    * A path is a list of a list of words:
    * list <
    *   list of words up ,
    *   list of words down
    *   >
    * This is a list of paths.
    */
   private List<List<List<Word>>> paths = null;

   /**
    * The sentence we have extracted the paths for.
    */
   private SRLSentence sentence;

   /**
    * The predicate we have extracted the paths for.
    */
   private Word predicate;
   
   /**
    * [DEBUG] Debug output.
    */
   public boolean debug = false;
   
   
   /**
    * If set to true, compare POS of words on the paths
    * instead of the dependency relation.
    */
   private boolean usePOS = false;
   
   
   /**
    * How many paths are _extracted_ from the labeled sentences?
    */
   private int pathsExtracted = 0;
   
   /**
    * How many paths are _checked_ in the unlabeled sentences?
    */
   private int pathsChecked = 0;

   /**
    * How many paths are _found_ in the unlabeled sentences?
    */
   private int pathsFound = 0;

   


   @Override
   protected List<Word> getPossibleArgumentsSpecificLabeled(Word predicate,
         SRLSentence sentence) {
      return getPossibleArgumentsSpecific(null, null, predicate, sentence, true);
   }


   @Override
   protected List<Word> getPossibleArgumentsSpecificUnlabeled(Word predicate,
         SRLSentence sentence, Word labeledPredicate, SRLSentence labeledSentence) {
      return getPossibleArgumentsSpecific(predicate, sentence, labeledPredicate, labeledSentence, false);
   }
   
   
   
   /**
    * Gives a list of possible argument candidates for a predicate
    * in the sentence. The predicate has to be in the sentence.
    * The predicate does have to be marked as predicate.
    * Possible argument candidates on labeled side are all actual arguments
    * and all words on the dependency path to the argument.
    * On unlabeled side the labeled paths are searched and if found all
    * words on this path are extracted.
    * For unlabeled, labeled has to be called first ON THE CORRECT
    * LABELED SENTENCE (!!).
    * (cf. Fuerstenau and Lapata 2009, 2012)
    * 
    * Punctuation and ROOT is filtered out.
    * The predicate itself is not included.
    * All words are filtered to have a distance smaller than distanceLimit.
    * If the word is an actual predicate, the list also only contains
    * the above mentioned words. It may be the case that real arguments
    * are not in this list.
    * 
    * @param predicate A word from the UNLABELED sentence
    *          (ignored when the LABELED side is regarded).
    * @param sentence The UNLABELED sentence (with dependency structure)
    *          (ignored when the LABELED side is regarded).
    * @param labeledPredicate A word from the LABELED sentence
    *          (must be provided also on unlableed side).
    * @param labeledSentence The LABELED sentence (with dependency structure)
    *          (must be provided also on unlableed side).
    * @param labeled whether to extract candidates for LABELED or UNLABELED side.
    * 
    * @return A list of possible argument candidates.
    *    The list may be empty if the sentence contains only the predicate,
    *    or all possible candidates are filtered out, but not null.
    */
   private List<Word> getPossibleArgumentsSpecific
         (Word predicate, SRLSentence sentence, 
          Word labeledPredicate, SRLSentence labeledSentence, boolean labeled) {

      ArrayList<Word> results = new ArrayList<Word>();

      
      // A) include all direct dependents
      // TODO except auxiliaries ??
      if (labeled) {
         for (Word child : labeledPredicate.getDirectChildren()) {
            results.add(child);            
         }
      } else { // use predicate from unlabeled sentence 
         for (Word child : predicate.getDirectChildren()) {
            results.add(child);            
         }
      }

      
      // Check if we are in the same sentence / same predicate as when the paths
      // were created, if not, delete them.
      // (if 'sentence' is null, 'paths' is also null, no need to check that)
      if (this.sentence != null && (!this.sentence.isSameSentence(labeledSentence) | !this.predicate.equals(labeledPredicate))) {
         paths = null;
      }
      
      
      // Extract paths to labeled arguments
      // If we are creating candidates for the labeled side,
      // add all the words on the path to the result.
      if (paths==null) {
         
         // Bookkeeping
         paths = new ArrayList<List<List<Word>>>();
         this.sentence = labeledSentence;
         this.predicate = labeledPredicate;
         
         // Extract paths
         for (Word arg : labeledSentence.getArguments(labeledPredicate)) {
            List<List<Word>> wordsOnPath = labeledSentence.getWordsOnPath(labeledPredicate, arg);
            paths.add(wordsOnPath);
            if (debug) System.out.println("\nAdd path: " + wordsOnPath); // TEST !!!
            pathsExtracted+=1;
            
            // TODO FOR 
            if (labeled) { // add to candidate list only for the labeled side
               // results.add(arg); // THIS IS THE OLD STUFF, WHERE ONLY ARGS ARE ADDED
               for (List<Word> pathpart : wordsOnPath) { // up/down parts separately
                  for (Word word : pathpart) {
                     if (!results.contains(word)) // filter out duplicates that are on several paths
                        results.add(word);
                  }
               }
            }
            
         }
      }
      
     
      // For unlabeled side:
      // Try to find paths
      if (!labeled) { // unlabeled
         
         // B) complex paths
         // have called labeled first and saved result in pathWords
         for (List<List<Word>> path : paths) {
            pathsChecked+=1;
            
            if (debug) System.out.println("\nCheck path: " + path); // TEST !!!
            
            Word current = predicate;
            Word last = null;
            boolean abort = false;
   
            // Go from word1 up to LCA
            for (Word word : path.get(0)) {
               if (debug) System.out.println("go up from " + word + " / " + current);
               // Check if they have the same deprel, otherwise abort (or if we check null, i.e., want to go higher than ROOT)
               if (current == null || !checkMatch(word, current)) {
                  if (debug) System.out.println("abort, does not match " + word + " / " + current);
                  abort = true;
                  break;
               }
               last = current;
               current = current.getHead();
            }
   
            // If I do not have that path, continue with next path
            if (abort)
               continue;
            
            // Go from LCA down to word2
            Set<Word> continuepaths = new HashSet<Word>();
            Set<Word> candidates = new HashSet<Word>();
            continuepaths.add(last);
            for (Word word : path.get(1)) { // start at word below LCA
               if (debug) System.out.println("continuepaths " + continuepaths);
               for (Word currentWord : continuepaths) { // initially LCA
                  // Get all direct children
                  Set<Word> children = currentWord.getDirectChildren();
                  if (debug) System.out.println("go down to " + word + " / " + children);
                  // Abort this candidate if no children are found
                  if (children == null || children.isEmpty()) {
                     continue;
                  }
                  // Otherwise add those that match the path
                  for (Word match : children) {
                     if ( checkMatch(word, match)) {
                        candidates.add(match);
                     }
                  }
                  if (debug) System.out.println("candidates " + candidates);
               }
               if (debug) System.out.println("all candidates " + candidates);
               continuepaths = candidates;
               candidates = new HashSet<Word>();    
               if (continuepaths.isEmpty()) {
                  if (debug) System.out.println("SORRY, none found :(");
                  abort = true;
                  break;
               }          

            }

            // Have reached the end of the path, add words on this path to result
            for (Word currentWord : continuepaths) {
               List<List<Word>> pathToThis = sentence.getWordsOnPath(predicate, currentWord);
               results.addAll(pathToThis.get(0));
               results.addAll(pathToThis.get(1));
               if (debug) System.out.println("YEAH: add " + currentWord + " / " + pathToThis);
               pathsFound+=1;
            }
            
         }
              
         
      }
            
      // Will be filtered by super
      return results;
   }

   
   /**
    * Check if there is a match, i.e., the POS category or the Deprel
    * (depending on the value of 'usePos').
    * 
    * @param current A word, not null.
    * @param word A second word, not null.
    * @return TRUE if they have the same POS|Deprel, FALSE otherwise.
    */
   private boolean checkMatch(Word current, Word word) {
      if (usePOS)
         return POSUtils.haveSamePOSCategory(current.getPOS(), word.getPOS());
      else
         return current.getDeprel().equalsIgnoreCase(word.getDeprel());
   }


   @Override
   public String getName() {
      if (usePOS)
         return "ArgumentCreatorFromLabeledPaths/POSCat";
      else
         return "ArgumentCreatorFromLabeledPaths/Deprel";
   }
   

   @Override
   public String getStatistics() {
      return super.getStatistics() +  String.format(" %d paths extracted, %d paths checked, %d paths found", pathsExtracted, pathsChecked, pathsFound);
   }

   @Override
   public void resetStatistics () {
      super.resetStatistics();
      pathsExtracted = 0;
      pathsChecked = 0;
      pathsFound = 0;
   };
   
   

}
