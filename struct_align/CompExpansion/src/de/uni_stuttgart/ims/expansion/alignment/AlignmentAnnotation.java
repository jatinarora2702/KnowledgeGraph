// (c) Wiltrud Kessler
// 23.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.alignment;

import de.uni_stuttgart.ims.nlpbase.nlp.ArgumentType;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Annotate an unlabeled sentence according to the alignment of 
 * words in a labeled sentences with words of the unlabeled sentence.
 * 
 * @author kesslewd
 *
 */
public class AlignmentAnnotation {


   /**
    * Annotate an unlabeled sentence with predicate and arguments
    * according to the labeled sentence and the alignment.
    * For every argument in the labeled sentence, the aligned word
    * is annotated with the same relation as the labeled argument.
    * If there is no aligned word, nothing is done.
    * This method modifies the information in 'sentenceUnlabeled'
    * (add a new predicate and arguments).
    * 
    * @param predicateLabeled The predicate of the labeled sentence.
    * @param sentenceLabeled The labeled sentence.
    * @param predicateUnlabeled The word to be labeled predicate of the unlabeled sentence.
    * @param sentenceUnlabeled The sentence to be labeled.
    * @param alignment keys are words in the labeled sentence,
    *    aligned words are words in the unlabeled sentence.
    */
   public static void annotateSentence (Word predicateLabeled, SRLSentence sentenceLabeled, 
         Word predicateUnlabeled, SRLSentence sentenceUnlabeled, WordAlignment alignment) {
           
      // Annotate predicate with the same annotation as in the labeled sentence
      sentenceUnlabeled.addPredicate(predicateUnlabeled, predicateLabeled.getType(), predicateLabeled.getDirection());
      
      // Annotate words aligned with arguments with the same relation 
      // as in the labeled sentence.
      for (Word argumentLabeled : sentenceLabeled.getArguments(predicateLabeled)) {
         
         // Label if the argument is the same word as the predicate
         if (argumentLabeled == predicateLabeled) {
            ArgumentType relation = sentenceLabeled.getRelation(predicateLabeled, predicateLabeled);
            sentenceUnlabeled.addArgument(predicateUnlabeled, predicateUnlabeled, relation);
            continue;
         }
         
         // Label according to alignment
         Word argumentUnlabeled = alignment.getAligned(argumentLabeled);
         if (argumentUnlabeled != null) {
            ArgumentType relation = sentenceLabeled.getRelation(predicateLabeled, argumentLabeled);
            sentenceUnlabeled.addArgument(predicateUnlabeled, argumentUnlabeled, relation);
         }
         
      }
      
   }
   
}
