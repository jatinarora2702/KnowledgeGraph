// (c) Wiltrud Kessler
// 07.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.alignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Generates all possible alignments between two lists of words.
 * WARNING: All alignments are generated and stored in memory.
 * This may take a long time if both lists are big.
 * 
 * @author kesslewd
 *
 */
public class AlignmentGenerator implements Iterable<WordAlignment>, Iterator<WordAlignment> {

   /**
    * Words to be aligned.
    */
   private List<Word> source;

   /**
    * Words the words of source are being aligned with.
    */
   private List<Word> target;
   

   /**
    * All possible alignments (in no particular order).
    */
   private ArrayList<WordAlignment> allAlignments;
   

   /**
    * Keep track of next alignment to return from this.allAlignments.
    */
   private int i = 0;

   

   /**
    * Initialize Generator of all possible alignments of words from
    * 'source' with 'target'.
    * Alignments are 1-to-1, it is not possible that a source word
    * is aligned to two target words or that two source words are
    * aligned to the same target word.
    * Source words may be aligned to null (but not all alignments
    * can be null).
    * 
    * @param source Words to be aligned. Every word will be aligned to
    *    either a word from 'target' or null.
    *    Words from 'source' are the keys in the alignments that are returned.
    * @param target Words that the source words will be aligned to.
    *    Words from 'target' are the values in the alignments that are returned.
    */
   public AlignmentGenerator (List<Word> source, List<Word> target) {   
      this.source = source;
      this.target = target;
   }
   
   
   
   // === ITERATOR METHODS ===
   
   
   
   /**
    * Create an iterator over all possible alignments. 
    * WARNING: All alignments are first generated and stored in memory.
    * This may take a long time if both lists are big.
    */
   @Override
   public Iterator<WordAlignment> iterator() {
      this.allAlignments = new ArrayList<WordAlignment>();
      listAlignments(this.source, this.target, new WordAlignment());
      return this;
   }
   
   
   /**
    * Return false if all possible alignments have been returned,
    * else true.
    */
   @Override
   public boolean hasNext() {
      return (i < allAlignments.size());
   }

   /**
    * Get next possible alignment (no particular order),
    * all alignments are different if no element in source/target
    * is repeated.
    */
   @Override
   public WordAlignment next() {
      i++;
      return allAlignments.get(i-1);
   }

   /**
    * Remove an item from the list
    * - throws UnsupportedOperationException.
    */
   @Override
   public void remove() {
      throw new UnsupportedOperationException(); 
   }
   
   
   // === GENERATION OF ALIGNMENTS ===

   
   /**
    * Recursively list all possible ways of aligning the words in
    * 'toBeAligned' with the remaining words in 'possibilities'.
    * Already aligned words are given in 'currentAlignment'.
    * Completed alignments are saved to 'this.allAlignments'.
    * 
    * @param toBeAligned Words that have not been aligned yet.
    * @param possibilities Possible words where no word has been aligned to them yet,
    *    so a words could be aligned to them.
    * @param currentAlignment All aligned words up until now.
    */
   private void listAlignments (List<Word> toBeAligned, List<Word> possibilities, 
         WordAlignment currentAlignment) {
      
      // Have an alignment for all words to be aligned:
      // Save alignment and return.
      if (toBeAligned == null || toBeAligned.isEmpty()) {
         allAlignments.add(currentAlignment.copyAlignment());
         return;
      }

      // Have no more possibilites for words to be aligned:
      // Align leftover words to null, save alignment and return.
      if (possibilities == null || possibilities.isEmpty()) {
         for (Word a : toBeAligned) {
            currentAlignment.add(a, null, 0);  
         }
         allAlignments.add(currentAlignment.copyAlignment());
         return;
      }
      
      // Have only one target word to be aligned to, but potentially
      // many source words to align 
      if (possibilities.size() == 1) {
         Word b = possibilities.get(0);
         if (b == null) { 
            // Special case b is null -> align everything with null
            // without this, every source word would be aligned with this
            // 'special null' before everything else is aligned with null
            // and this results in many identical alignments.
            listAlignments(toBeAligned, null, currentAlignment);
         } else {
            // Normal case, b is not null
            // -> generate an alignment for each source word where this
            // source word is aligned to b and the rest to null.
            for (Word a : toBeAligned) {
               currentAlignment.add(a, b, 0);
               List<Word> newtoBeAligned = new ArrayList<Word>(toBeAligned);
               newtoBeAligned.remove(a);
               listAlignments(newtoBeAligned, null, currentAlignment);
               currentAlignment.remove(a);
            }
         }
         
      // Usual case: Have many source words to align and
      // many target words to be aligned with.
      // Try out every possibility recursively.
      // We need to copy the lists because when we backtrack
      // they need to still exist in their form at this point.
      // Shallow copies are sufficient as no words are actually deleted,
      // they are only added and removed from lists.
      } else {
         // Get some a
         Word a = toBeAligned.get(0);
         List<Word> newtoBeAligned = new ArrayList<Word>(toBeAligned);
         newtoBeAligned.remove(a);
         
         // Align with every possible b
         for (Word b : possibilities) {
            currentAlignment.add(a, b, 0);
            List<Word> newPossibilities = new ArrayList<Word>(possibilities);
            newPossibilities.remove(b);
            // Call recursively to align the rest of the source words
            listAlignments(newtoBeAligned, newPossibilities, currentAlignment);
            currentAlignment.remove(a);
         }
      }
      
   }
      

}
