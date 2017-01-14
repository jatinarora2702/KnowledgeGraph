// (c) Wiltrud Kessler
// 21.04.2016
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/



package de.uni_stuttgart.ims.expansion.candidates;

import java.util.List;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Abstract class as a parent for argument creators who do the same
 * for the labeled and unlabeled side.
 * 
 * @author kesslewd
 *
 */
public abstract class ArgumentCreatorUniform extends ArgumentCreator {


   @Override
   protected List<Word> getPossibleArgumentsSpecificLabeled(Word predicate, SRLSentence sentence) {
      return getPossibleArgumentsSpecific(predicate, sentence);
   }


   @Override
   protected List<Word> getPossibleArgumentsSpecificUnlabeled(Word predicate,
         SRLSentence sentence, Word labeledPredicate, SRLSentence labeledSentence) {
      return getPossibleArgumentsSpecific(predicate, sentence);
   }
   

   /**
    * This method is called to get the candidates for 
    * both the labeled and unlabeled side. 
    * 
    * @param predicate
    * @param sentence
    * @return
    */
   protected abstract List<Word> getPossibleArgumentsSpecific (Word predicate, SRLSentence sentence);
   

   /**
    * Give a human-readable name for this subclass that contains all relevant parameter settings.
    * 
    * @return Nice name to put on a name tag.
    */   
   public abstract String getName ();
   

}
