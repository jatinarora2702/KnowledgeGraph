// (c) Wiltrud Kessler
// 16.09.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.candidates;

import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;

/**
 * Combination of two argument creators, one for the
 * labeled and one for the unlabeled side.
 * 
 * @author kesslewd
 *
 */
public class ArgumentCreatorAssymetric extends ArgumentCreator {
   
   private ArgumentCreator acLabeled;
   private ArgumentCreator acUnlabeled;
   
   /**
    * Combination of two argument creators, one for the
    * labeled and one for the unlabeled side.
    * 
    * @param acLabeled The argument creator used to extract argument
    *          candidates on the labeled side.
    * @param acUnlabeled The argument creator used to extract argument
    *          candidates on the UNlabeled side.
    */
   public ArgumentCreatorAssymetric (ArgumentCreator acLabeled, ArgumentCreator acUnlabeled) {
      this.acLabeled = acLabeled;
      this.acUnlabeled = acUnlabeled;
   }
   

   @Override
   public String getName() {
      return "ArgumentCreatorAssym (" + acLabeled.getName() + ", " + acUnlabeled.getName() + ")";
   }


   @Override
   protected List<Word> getPossibleArgumentsSpecificLabeled(Word predicate,
         SRLSentence sentence) {
      return acLabeled.getPossibleArgumentsLabeled(predicate, sentence);
   }


   @Override
   protected List<Word> getPossibleArgumentsSpecificUnlabeled(Word predicate,
         SRLSentence sentence, Word labeledPredicate, SRLSentence labeledSentence) {
      return acUnlabeled.getPossibleArgumentsUnlabeled(predicate, sentence, labeledPredicate, labeledSentence);
   }

   @Override
   public String getStatistics() {
      return acLabeled.getStatistics() + " | " + acUnlabeled.getStatistics();
   }

   @Override
   public void resetStatistics () {
      acLabeled.resetStatistics();
      acUnlabeled.resetStatistics();
   };

}
