// (c) Wiltrud Kessler
// 13.04.2016
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.candidates;

/**
 * Read a String and get a type.
 * Just to be able to use option files.
 * 
 * @author kesslewd
 *
 */
public enum ArgumentCreatorType {

   DependencyFiltered,
   AncestorDescendant,
   Labeled,
   LabeledPaths
   ;

   /**
    * Possibilities:
    * depfiltered = ArgumentCreatorDependencyFiltered(),
    * ancdesc = ArgumentCreatorAncestorDescendant(),
    * labeled = ArgumentCreatorRealArgs(),
    * paths = ArgumentCreatorFromLabeledPaths()
    * 
    * @param name name of arg creator (only one)
    * @return The corresponding argument creator or null.
    */
   public static ArgumentCreatorType getTypeFromString (String name) {

      if (name.equals("depfiltered")) 
         return DependencyFiltered;

      if (name.equals("ancdesc")) 
         return AncestorDescendant;

      if (name.equals("labeled")) 
         return Labeled;

      if (name.equals("paths")) 
         return LabeledPaths;
      
      return null;
   }
   

   
   
   

}
