// (c) Wiltrud Kessler
// 30.07.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.lists;

import java.util.Iterator;
import java.util.TreeSet;


/**
 * List that is sorted by score.
 * First element ist the one with the best score.
 * Elements that have a score lower than the threshold
 * are not put into the list.
 * 
 * @author kesslewd
 *
 * @param <T> Something that is in the list :)
 */
public class ThresholdList<T extends Comparable<T>> extends BestScoreSortedList<T> {


   /**
    * Best-list.
    */
   private TreeSet<T> bestList;

   /**
    * Threshold, above this include in list.
    */
   private T threshold;



   public ThresholdList (T threshold) {
      this.bestList = new TreeSet<T>();
      this.threshold = threshold;
   }

   
   /**
    * ThresholdList (t=.)
    */
   @Override
   public String getName () {
      return "ThresholdList (t=" + this.threshold + ")";
   }

   /**
    * Give the number of elements.
    */
   @Override
   public int size () {
      return bestList.size();
   }

   @Override
   public void add (T object) {
      if (object.compareTo(this.threshold) >= 0) {
         bestList.add(object); 
      }
   }

   /**
    * Get object at given place.
    */
   @Override
   public T get (int index) {
      T myT = null;
      Iterator<T> asdf = this.iterator();
      for (int i=0; i<=index; i++) {
         if (asdf.hasNext())
            myT = asdf.next();
         else
            myT = null;
      }
      
      return myT; // TODO untested
   }



   /**
    * List is empty afterwards.
    */
   @Override
   public void reset() {
      this.bestList = new TreeSet<T>();
   }


   /**
    * Gives best scoring first, then second, then ...
    */
   @Override
   public Iterator<T> iterator() {
      return bestList.descendingIterator(); // TODO check ordering
   }
   
   
}
