// (c) Wiltrud Kessler
// 14.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.util.HashMap;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Measures the cosine similarity between the context
 * vectors of two words.
 * The vectors need to be created beforehand
 * (see class CreateContextVectors)
 * 
 * @author kesslewd
 *
 */
public class VectorSpaceSimilarity extends Similarity {
   

   /**
    * Save word vectors in memory.
    */
   private static HashMap<String, HashMap<String, Integer>> wordVectors;
   
   /**
    * Number of digits after decimal point for BigDecimal (in case of scale>0).
    * Used for calculation of square root, length and cosine similarity.
    */
   private static final int scale = 10;

   /**
    * [dev] Debug mode.
    */
   private static boolean debug = false;

   /**
    * Zero.
    */
   private static final BigDecimal ZERO = BigDecimal.valueOf(0);
   
   
   /**
    * 
    * @param alias Just a name (to enable you to have several)
    * @param window Token window size
    * @param dimensions Number of dimensions/tokens in the model
    * @return "../models/wordvectors_" + alias + "_" + window + "_" + dimensions + ".ser"
    */
   protected static String getModelFileName(String alias, int window, int dimensions) {
      return "models/wordvectors_" + alias + "_" + window + "_" + dimensions + ".ser";
   }


   /**
    * Measures the cosine similarity between the context
    * vectors of the two words.
    * The vectors need to be created beforehand, they are loaded
    * from the file 'filename'.
    */
   public VectorSpaceSimilarity(String alias, int window, int dimensions) {
      this(getModelFileName(alias, window, dimensions));
   }
   

   /**
    * Measures the cosine similarity between the context
    * vectors of the two words.
    * The vectors need to be created beforehand, they are loaded
    * from the file 'filename'.
    */
   @SuppressWarnings("unchecked")
   public VectorSpaceSimilarity(String filename) {
      // Load word vectors
      wordVectors = null;
      try {
         FileInputStream fileIn = new FileInputStream(filename);
         ObjectInputStream in = new ObjectInputStream(fileIn);
         wordVectors = (HashMap<String, HashMap<String, Integer>>) in.readObject();
         in.close();
         fileIn.close();
      } catch (IOException i) {
         System.err.println("ERROR in VectorSpaceSimilarity !!! " + i.getMessage());
         System.err.println("The context vector file is missing, did you create it with the class CreateContextVectors? For now, all lexical similarities will be 0.");
         wordVectors = new HashMap<String, HashMap<String, Integer>>();
      } catch (ClassNotFoundException c) {
         System.err.println("ERROR in VectorSpaceSimilarity !!! " + c.getMessage());
         System.err.println("There should be context vectors in the file " + filename + ". Did you create them with the class CreateContextVectors? For now, all lexical similarities will be 0.");
         wordVectors = new HashMap<String, HashMap<String, Integer>>();
      }
      
   }

   
   
   /**
    * Get the context vector for a word form.
    * 
    * @param wordForm
    * @return
    */
   protected HashMap<String, Integer> getWordVector (String wordForm) { 
      return wordVectors.get(wordForm);
   }

   
   
   // Methods to calculate vector space similarity
   
   
   /**
    * Stupid class that allows us to return three values.
    */
   class Container {
      public Container (ExplainedSimilarityValue explainedSim, 
            HashMap<String, Integer> vector1, HashMap<String, Integer> vector2){
         this.explainedSim = explainedSim;
         this.vector1 = vector1;
         this.vector2 = vector2;
      }
      public Container (ExplainedSimilarityValue explainedSim){
         this(explainedSim, null, null);
      }
      ExplainedSimilarityValue explainedSim;
      HashMap<String, Integer> vector1;
      HashMap<String, Integer> vector2;
   }
   
   
   /**
    * Treat cases where one or both words are null.
    * Get the context vectors for both words,
    * treat cases where one or both of them are not found.
    * 
    * @param word1
    * @param word2
    * @return
    */
   protected Container runChecks (Word word1, Word word2) {

      // Treat cases where one or both words are null.
      // The check returns similarity and explanation for these cases.
      // If everything is ok it returns null.
      ExplainedSimilarityValue nullCheck = super.checkNull(word1, word2);
      if (nullCheck != null)
         return new Container(nullCheck);

      String wordForm1 = word1.getForm().toLowerCase();
      String wordForm2 = word2.getForm().toLowerCase();
      
      // Return 1 if the word forms are the same.
      if (wordForm1.equals(wordForm2)) {
         return new Container(new ExplainedSimilarityValue(1, "words are identical"));
      }

      // Get the context vector of word 1.
      // If no context vector is found, return 0.
      HashMap<String, Integer> vectorWord1 = getWordVector(wordForm1);
      if (vectorWord1 == null) {
         if (debug) System.out.println("vector for " + wordForm1 + " is 0");
         return new Container(new ExplainedSimilarityValue(0, "vector for " + wordForm1 + " is 0"));
      }

      // Get the context vector of word 2.
      // If no context vector is found, return 0.
      HashMap<String, Integer> vectorWord2 = getWordVector(wordForm2);
      if (vectorWord2 == null) {
         if (debug) System.out.println("vector for " + wordForm2 + " is 0");
         return new Container(new ExplainedSimilarityValue(0, "vector for " + wordForm2 + " is 0"));
      }
      
      return new Container(null, vectorWord1, vectorWord2);
   }

   
   /**
    * Measures the cosine similarity between the context
    * vectors of the two words.
    * If one of the words is null, 0 is returned.
    * If the word forms are the same, 1 is returned.
    * If no context vector is found for a word, 0 is returned.
    * 
    * Uses only the words itself.
    * 
    * @param sentence1 Sentence that contains the FIRST word.
    * @param predicate1 Predicate that the FIRST word is an 
    *    argument candidate of (must be in sentence1).
    * @param word1 The FIRST word.
    * @param sentence2 Sentence that contains the SECOND word.
    * @param predicate2 Predicate that the SECOND word is an 
    *    argument candidate of (must be in sentence1).
    * @param word2 The SECOND word.
    * @return A double value between 0 and 1 
    *    where 1 indicates maximum similarity (identity) 
    *    and 0 indicates no similarity.
    */
   @Override
   public ExplainedSimilarityValue getExplainedSimilarity(
         SRLSentence sentence1, Word predicate1, Word word1, 
         SRLSentence sentence2, Word predicate2, Word word2) {      
      
      // Treat cases where one or both words are null or vectors are not found.
      // The check returns similarity and explanation for the different cases.
      // If everything is ok it returns null.      
      Container nullCheck = this.runChecks(word1, word2);      
      if (nullCheck.explainedSim != null)
         return nullCheck.explainedSim;

      // If both vectors are found, calcuate cosine similarity and return
      double cosine = cosine(nullCheck.vector1, nullCheck.vector2);
      
      if (debug) System.out.println("cosine " + word1.getForm() + " " + word2.getForm() + " " + cosine);
      
      return new ExplainedSimilarityValue(cosine);
      
   }
   
   
   /**
    * Calculate the cosine similarity between the two context vectors.
    * Both vectors must be not null (they may be empty).
    * 
    * @param vectorWord1 One context vector.
    * @param vectorWord2 Another context vector.
    * @return A value between 0 and 1 indicating similarity.
    */
   protected static double cosine (HashMap<String, Integer> vectorWord1, HashMap<String, Integer> vectorWord2) {
      
      // Use BigDecimal, because the numbers may become bigger
      // than the range of double.
      // Note: Java does not throw errors or warnings in case of overflows,
      // so be sure to check every case where double is used
      // BigDecimal doc:
      //    http://docs.oracle.com/javase/6/docs/api/java/math/BigDecimal.html
      BigDecimal sum = BigDecimal.valueOf(0);
      
      // Sum up product of term frequencies over all dimensions.
      // Do not normalize here, because multiplying many small numbers
      // may lead to inaccuracies.
      // We iterate only over dimensions that are not 0 in vector1.
      for (String token : vectorWord1.keySet()) {
         Integer count = vectorWord2.get(token);
         
         // The token occurs in vector1, but not in vector2
         if (count == null || count == 0) 
            continue;
           
         // The token occurs in both vectors - add to sum
         //    Sum_of_TFs = Sum_of_TFs + tf_vector2 * tf_vector1
         sum = sum.add(BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(vectorWord1.get(token))));
         if (debug) System.out.println(token + " " + count + " * " + vectorWord1.get(token) + " = " + sum);

      }

      // Calculate lengths of vectors for normalization
      if (debug) System.out.println("sum: " + sum);
      BigDecimal lengthWord1 = length(vectorWord1);
      if (lengthWord1.compareTo(ZERO) == 0) {
         if (debug) System.out.println("Error, length of vector for word 1  is zero!");
         return 0;
      }
      BigDecimal lengthWord2 = length(vectorWord2);
      if (lengthWord2.compareTo(ZERO) == 0) {
         if (debug) System.out.println("Error, length of vector for word 2  is zero!");
         return 0;
      }
      
      // Calculate cosine similarity.
      // This value is between 0 and 1 and can be converted to double.
      //    cos(w1,w2) = Sum_of_TFs(w1,w2) / (length(w1) * length(w2)
      return sum.divide(lengthWord1.multiply(lengthWord2), scale, BigDecimal.ROUND_DOWN).doubleValue();
   }
   
   
   /**
    * Calculate the length of the vector in vector space.
    * 
    * @param vectorWord A vector.
    * @return Eucledean norm of input vector.
    */
   private static BigDecimal length (HashMap<String, Integer> wordVector) {
      
      // Use BigDecimal, (see comment on method cosine)
      BigDecimal sum = BigDecimal.valueOf(0);

      // The length of a vector is defined with the Eucledean norm
      // as the squareroot of the sum of all its squared components.
      //    ||v|| = sqrt(v1^2 + v2^2 + ... )
      for (Integer counter : wordVector.values()) {
         sum = sum.add(BigDecimal.valueOf(counter).multiply(BigDecimal.valueOf(counter)));
      }
      return sqrt(sum);
   }

   
   /**
    * Calculate the square root of a BigDecimal.
    * (c) http://www.java2s.com/Code/Java/Data-Type/DemonstrationofhighprecisionarithmeticwiththeBigDoubleclass.htm
    * 
    * @param n The number that we want to calculate the square root of.
    * @return The squareroot of n with an accuracy of
    *     this.scale digits after the decimal point.
    */
   private static BigDecimal sqrt (BigDecimal n) {
      
      if (n.compareTo(ZERO) == 0) // avoid division by zero
         return ZERO;
      
      BigDecimal TWO = BigDecimal.valueOf(2);

      // Obtain the first approximation
      BigDecimal x = n.divide(BigDecimal.valueOf(3), scale, BigDecimal.ROUND_DOWN);
      BigDecimal lastX = ZERO;

      // Proceed through 50 iterations
      for (int i = 0; i < 50; i++) {
        x = n.add(x.multiply(x)).divide(x.multiply(TWO), scale,
            BigDecimal.ROUND_DOWN);
        if (x.compareTo(lastX) == 0)
          break;
        lastX = x;
      }
      return x;
    }

   
}
