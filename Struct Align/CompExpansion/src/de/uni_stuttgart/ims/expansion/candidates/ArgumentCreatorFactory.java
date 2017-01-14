// (c) Wiltrud Kessler
// 13.04.2016
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.candidates;

import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.ims.expansion.candidates.ArgumentCreatorDependencyFiltered.mode;

/**
 * Read a String and get a ArgumentCreator class.
 * 
 * @author kesslewd
 *
 */
public class ArgumentCreatorFactory {
   

   /**
    * Get a default ArgumentCreator = ArgumentCreatorRealArgs()
    * @return
    */
   public static ArgumentCreator getDefaultAC () {
      return new ArgumentCreatorRealArgs();
   }
   

   /**
    * Possibilities:
    * depfiltered = ArgumentCreatorDependencyFiltered(),
    * ancdesc = ArgumentCreatorAncestorDescendant(),
    * labeled = ArgumentCreatorRealArgs(),
    * paths = ArgumentCreatorFromLabeledPaths()
    * 
    * You can combine two with a comma,
    * e.g., 'labeled,ancdesc' will give use real arguments
    * on the labeled sentence and ArgumentCreatorAncestorDescendant
    * on the unlabeled sentence.
    * 
    * @param name name of arg creator
    * @return The corresponding argument creator or null.
    */
   public static ArgumentCreator getArgumentCreator (String name) throws Exception {
            
      String[] parts = name.split(",");
      
      List<ArgumentCreator> aclist = new ArrayList<ArgumentCreator>();
      
      for (String part : parts) {
         
         ArgumentCreatorType type = ArgumentCreatorType.getTypeFromString(part);
         
         if (type == null)
            throw new Exception("Error, unknown value for part: " + part);
      
         switch (type) {
         case DependencyFiltered : 
            aclist.add(new ArgumentCreatorDependencyFiltered(mode.all, mode.all, mode.direct));
            break;
         case AncestorDescendant : 
            aclist.add(new ArgumentCreatorAncestorDescendant());
            break;
         case Labeled : 
            aclist.add(new ArgumentCreatorRealArgs());
            break;
         case LabeledPaths : 
            aclist.add(new ArgumentCreatorFromLabeledPaths());
            break;
         default : 
            throw new Exception("Error, unknown value for actype part: " + part);
         }
      }
      
      if (aclist.size() == 1)
         return aclist.get(0);
      else if (aclist.size() == 2)
         return new ArgumentCreatorAssymetric(aclist.get(0), aclist.get(1));
      else
         throw new Exception("Error, have more than 2 items in argument creator: " + aclist);
      
   }

}
