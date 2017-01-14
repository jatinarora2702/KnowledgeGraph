// (c) Wiltrud Kessler
// 30.07.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.lists;

import java.util.Iterator;


/**
 * Abstract list that is sorted by score.
 * First element ist the one with the best score.
 * List can be cut off at threshold or after n-best (see implementations).
 * List should be able to contain two different items with the same score.
 * 
 * @author kesslewd
 *
 * @param <T> Something that is in the list and sortable (is this a word?)
 */
public abstract class BestScoreSortedList<T> implements Iterable<T> {

   /**
    * List is empty afterwards.
    */
   public abstract void reset ();

   /**
    * Add object at place given by score.
    * May not be in the list after adding, if score is too low.
    */
   public abstract void add (T object);

   /**
    * Get object at given place.
    */
   public abstract T get (int index);

   /**
    * Gives best scoring first, then second, then ...
    */
   public abstract Iterator<T> iterator();

   /**
    * Give the number of elements.
    */
   public abstract int size ();

   /**
    * Give my name.
    */
   public abstract String getName ();
   
}
