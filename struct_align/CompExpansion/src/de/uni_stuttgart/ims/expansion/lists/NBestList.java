// (c) Wiltrud Kessler
// 30.07.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;



/**
 * List that is sorted by score.
 * First element ist the one with the best score.
 * List is cut off after n-best.
 * Elements with the same score are allowed.
 * If they are the last element, they are added as overhead,
 * i.e. 3-best list after inserting 1, 3, 9, 9, 10, 10
 * is 10, 10, 9, 9.
 * 
 * @author kesslewd
 *
 * @param <T> Something that is in the list :)
 */
public class NBestList<T extends Comparable<T>> extends BestScoreSortedList<T> {


   /**
    * N-best-list for this predicate.
    */
   private LinkedList<T> nBestList;

   /**
    * Add n sentences for every predicate
    */
   private int n;

   /**
    * Overbooking
    */
   private int overbooking;
   
   

   /**
    * List that is sorted by score.
    * First element ist the one with the best score.
    * List is cut off after n-best.
    * Elements with the same score are allowed.
    * If they are the last element, they are added as overhead,
    * i.e. 3-best list after inserting 1, 3, 9, 9, 10, 10
    * is 10, 10, 9, 9.
    * 
    * @author kesslewd
    *
    * @param <T>
    */
   public NBestList (int n) {
      this.reset();
      this.n = n;
   }
   

   /**
    * NBestList (n=.)
    */
   @Override
   public String getName () {
      return "NBestList (n=" + this.n + ")";
   }
   
   /**
    * Give the number of elements.
    */
   @Override
   public int size () {
      return nBestList.size();
   }
   

   /**
    * Add according to 'compareTo' function of T.
    * Same objects: keep all.
    * Allow overhead.
    */
   @Override
   public void add (T object) {      
      
      if (object == null)
         return;

      // Add the first n sentences no matter what score
      if (nBestList.size() < this.n) {
         nBestList.add(object);
         Collections.sort(nBestList);
      } else {
         
         // If there are already n sentences, compare the score to the
         // first element in the list (= the one with the lowest score).
         if (nBestList.get(0).compareTo(object) <= 0) {
            
            // If this sentence has a equal or higher similarity score, add it.
            nBestList.add(object);
            
            // Sort list (as the list is short and also sorted except for one element
            // this should not be too much overhead).
            Collections.sort(nBestList);
            
            // Delete the now lowest (= the first item in the list)
            // to keep list size fixed at n.
            // Do not delete lowest if the similarities are identical.
            T shouldBeLast = nBestList.get(overbooking+1);
            ArrayList<T> remove = new ArrayList<T>();
            for (int i=0; i<overbooking+1; i++) {
               if ((shouldBeLast.compareTo(nBestList.get(i)) != 0)) {
                  remove.add(nBestList.get(i)); // overbooking should be removed
               } 
            }
            if (remove.size() == 0)
               overbooking+=1; // don't delete, we are overbooking
            else {
               overbooking=0; // delete overbooking
               nBestList.removeAll(remove);
            }

         } else {
            // The element is too small and will not be added
         }
      }

   }
   

   /**
    * Get object at given place.
    */
   @Override
   public T get (int index) {
      // Expect 0 = highest
      // in reality 0 = lowest
      // -> turn around
      return nBestList.get(nBestList.size()-index-1);      
   }

   
   /**
    * List is empty afterwards.
    */
   @Override
   public void reset() {
      nBestList = new LinkedList<T>();
      overbooking = 0;
   }


   /**
    * Gives best scoring first, then second, then ...
    */
   @Override
   public Iterator<T> iterator() {
      return nBestList.descendingIterator();
   }
   

}
