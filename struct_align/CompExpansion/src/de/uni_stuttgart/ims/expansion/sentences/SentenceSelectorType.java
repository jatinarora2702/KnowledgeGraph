// (c) Wiltrud Kessler
// 13.04.2016
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.sentences;


/**
 * Read a String and get a type.
 * 
 * @author kesslewd
 *
 */
public enum SentenceSelectorType {

   FORM,
   LEMMA,
   POS,
   COMPPOS
   ;
   

   /**
    * Possibilities:
    * form = SentenceSelectorLemma(),
    * lemma = SentenceSelectorForm(),
    * pos = SentenceSelectorPOS(),
    * comppos = SentenceSelectorCompPOS()
    * 
    * @param name name of sentence selector (only one)
    * @return The corresponding sentence selector or null.
    */
   public static SentenceSelectorType getTypeFromString (String name) {

      if (name.equals("form")) 
         return FORM;

      if (name.equals("lemma")) 
         return LEMMA;

      if (name.equals("pos")) 
         return POS;

      if (name.equals("comppos")) 
         return COMPPOS;
      
      return null;
   }
   

   
   
   

}
