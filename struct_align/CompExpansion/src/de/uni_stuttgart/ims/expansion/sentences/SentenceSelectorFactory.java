// (c) Wiltrud Kessler
// 13.04.2016
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package de.uni_stuttgart.ims.expansion.sentences;


/**
 * Read a String and get a SentenceSelector class.
 * 
 * @author kesslewd
 *
 */
public class SentenceSelectorFactory {
   

   /**
    * Get one sentence selector by name.
    * 
    * Possibilities:
    * form = SentenceSelectorLemma(),
    * lemma = SentenceSelectorForm(),
    * pos = SentenceSelectorPOS(),
    * comppos = SentenceSelectorCompPOS()
    * 
    * @param name name of sentence selector (only one)
    * @return The corresponding sentence selector or null.
    */
   public static SentenceSelector getSentenceSelector (String name) throws Exception {
          
      SentenceSelectorType type = SentenceSelectorType.getTypeFromString(name);

      if (type == null)
         throw new Exception("Error, unknown value for sentence selector: " + name);

      SentenceSelector sentenceselector = null;
      
      // Parameter sentence subselection
      switch (type) {
      case LEMMA:
         sentenceselector = new SentenceSelectorLemma();
         break;
      case FORM: 
         sentenceselector = new SentenceSelectorForm();
         break;
      case POS: 
         sentenceselector = new SentenceSelectorPOS();
         break;
      case COMPPOS: 
         sentenceselector = new SentenceSelectorCompPOS();
         break;
      default: 
         throw new Exception("Error, unknown value for seltype: " + name);
      }
      
      return sentenceselector;
      
   }

   public static SentenceSelector getDefaultSS() {
      return new SentenceSelectorLemma();
   }

}
