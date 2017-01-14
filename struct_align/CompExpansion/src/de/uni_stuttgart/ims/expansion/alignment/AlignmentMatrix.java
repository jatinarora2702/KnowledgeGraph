// (c) Wiltrud Kessler
// 26.03.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.alignment;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.uni_stuttgart.ims.expansion.similarity.Similarity;
import de.uni_stuttgart.ims.expansion.similarity.Similarity.ExplainedSimilarityValue;
import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Create a matrix of similarities between labeled and unlabeled
 * and get maximum assignments with the Hungarian Algorithm. 
 * 
 * @author kesslewd
 *
 */
public class AlignmentMatrix {

   
   public double[][] similarityMatrix;

   HashMap<String, String> explanations;
   
   private List<Word> argumentsLabeled;
   private List<Word> argumentsUnlabeled;
   private SRLSentence sentenceLabeled;
   private Word predicateLabeled;
   private SRLSentence sentenceUnlabeled;
   private Word predicateUnlabeled;
   
   private WordAlignment bestAlignment = null;
   private Double bestSimilarity = null;
   
   
   public AlignmentMatrix(List<Word> argumentsLabeled, List<Word> argumentsUnlabeled, 
         SRLSentence sentenceUnlabeled, Word predicateUnlabeled, SRLSentence sentenceLabeled, Word predicateLabeled) {
      this.argumentsLabeled = argumentsLabeled;
      this.sentenceLabeled = sentenceLabeled;
      this.predicateLabeled = predicateLabeled;      
      this.argumentsUnlabeled = argumentsUnlabeled;
      this.sentenceUnlabeled = sentenceUnlabeled;
      this.predicateUnlabeled = predicateUnlabeled;
   }

   
   /**
    * Creates a matrix of size [argumentsUnlabeled.size()][argumentsLabeled.size()]
    * Calculate similarities to fill all entries in the matrix.
    * Warning: Size of argumentsLabeled may not be 1, it must be >= 2 
    * due to the matrix being a stupid matrix
    * 
    * @param similarityFunction Similarity to calculate per word pair
    */
   public void calculateSimilarities(Similarity similarityFunction) {

      // Create empty matrix of similarity values
      similarityMatrix = new double[argumentsUnlabeled.size()][argumentsLabeled.size()];
      
      // Save also the explanations of the similarity values
      explanations = new HashMap<String, String>();
      
      int unlabeledIndex=0;
      int labeledIndex=0;
      
      // Go through every space in the matrix and calculate corresponding similarity
      for (Word unlabeled : argumentsUnlabeled) {
         labeledIndex=0;
         for (Word labeled : argumentsLabeled) {
            
            ExplainedSimilarityValue simexp = similarityFunction.getExplainedSimilarity(sentenceLabeled, predicateLabeled, labeled,
                  sentenceUnlabeled, predicateUnlabeled, unlabeled);
            
            double sim = simexp.similarityValue;

            explanations.put(unlabeledIndex + " " + labeledIndex, simexp.explanation);
            
            similarityMatrix[unlabeledIndex][labeledIndex] = sim;
            
            labeledIndex++;
         }
         unlabeledIndex++;
      }
           
   }
   

   /**
    * From the already-computed similarity matrix, get the best solution
    * (alignment and similarity).
    * To get the results, use getBestSimilarity() and/or getBestAlignment()
    * 
    * You need to call calculateSimiliarties() first!!!
    * 
    * @param similarityFunction Similarity to calculate per word pair
    */
   public void getSolution() {

      // We have a SIMILARITY matrix, where we want the MAXIMUM similarity.
      // We need a COST matrix where the MINIMUM cost will be calculated.
      
      // So create a new matrix
      double[][] costMatrix = new double[similarityMatrix.length][similarityMatrix[0].length];

      // 'Invert' values, i.e., take distances (1-sim)      
      for (int i=0; i<similarityMatrix.length; i++) {
         for (int j=0; j<similarityMatrix[0].length; j++) {
               costMatrix[i][j] = 1 - similarityMatrix[i][j];
         }
      }
      
      // Search for the best alignment in the matrix
      HungarianAlgorithm aligner = new HungarianAlgorithm(costMatrix);
      
      // Result is an array that gives the best index per ROW
      int[] result = aligner.execute();
      
      
      // Get the best similarity and alignment

      // Result is an array that gives the best index per ROW (labeled).
      // Calculate overall similarity (sum of scores over all) and corresponding alignments.
      bestSimilarity = 0.0;
      bestAlignment = new WordAlignment();
      for (int i=0; i<result.length; i++) {
         if (i < argumentsUnlabeled.size() & result[i] < argumentsLabeled.size() & result[i] != -1) { // non-null assignments
            bestSimilarity += similarityMatrix[i][result[i]]; 
            bestAlignment.add(this.argumentsLabeled.get(result[i]), this.argumentsUnlabeled.get(i), similarityMatrix[i][result[i]], explanations.get(i + " " + result[i]));
         } else { // non-assigned
            //bestAlignment.add(this.argumentsLabeled.get(i), null, 0, "");
         }

      }
      
      // Null-assignments
      Set<Word> keys = bestAlignment.getKeys();
      for (Word argument : argumentsLabeled) {
         if (!keys.contains(argument))
            bestAlignment.add(argument, null, 0, "");
      }
      
      // Normalize similarity by arguments size
      bestSimilarity = bestSimilarity / (float) argumentsLabeled.size();
      
   }
   

   /**
    * Get the normalized total similarity of the best alignment.
    * 
    * You need to call calculateSimiliarties() first!!!
    *  
    * @return ( sum of similarities ) / (number of labeled arguments) 
    */
   public double getBestSimilarity() {
      
      // If result is null, the solution has not been calculated.
      // Do this now.
      // Otherwise take the already-created solution.
      if (bestSimilarity == null)
         getSolution();


      return bestSimilarity;
   }


   /**
    * Get the word alignments of the best alignment.
    * 
    * You need to call calculateSimiliarties() first!!!
    *  
    * @return 
    */
   public WordAlignment getBestAlignment() {

      // If result is null, the solution has not been calculated.
      // Do this now.
      // Otherwise take the already-created solution.
      if (bestAlignment == null)
         getSolution();

      return bestAlignment;
   }
   
   
   /**
    * Print out the table.
    */
   public void printTable(double[][] table) {

      String row = "";
      for (int i=0; i<table.length; i++) {
         row = argumentsUnlabeled.get(i) + " ";
         for (int j=0; j<table[0].length; j++) {
            row = row + String.format("%.3f", table[i][j]) + " | ";
         }
         System.out.println(  " | " + row);
      }
   }
   

   /**
    * Print out the table.
    */
   public void printTableInverted(double[][] table) {
      String row = "";
      for (int j=0; j<table.length; j++) {
         row +=  " & " + argumentsUnlabeled.get(j).getForm();
      }
      System.out.println( "      " + row);
      for (int i=0; i<table[0].length; i++) {
         row = argumentsLabeled.get(i).getForm();
         for (int j=0; j<table.length; j++) {
            row += " & " + String.format("%.2f", table[j][i]) ;
         }
         System.out.println( row);
      }
   }
   
   
}
