// (c) Wiltrud Kessler
// 14.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import de.uni_stuttgart.ims.nlpbase.tools.SentenceSplitter;
import de.uni_stuttgart.ims.nlpbase.tools.SentenceSplitterStanford;
import de.uni_stuttgart.ims.nlpbase.tools.TextSpan;
import de.uni_stuttgart.ims.nlpbase.tools.Tokenizer;
import de.uni_stuttgart.ims.nlpbase.tools.TokenizerStanford;
import de.uni_stuttgart.ims.util.HashMapHelpers;

/**
 * Create context vectors.
 * 
 * @author kesslewd
 *
 */
public class VectorSpaceSimCreateVectors {

   

   private static int window = 3;
   private static int dimensions = 0;
   private static List<String> files = new ArrayList<String>();
   
   
 


   /**
    * Expected arguments:
    * arg[0] alias - just some identifying name to enable you to
    *    have various files (String, only letters).
    * arg[1] window - token window size (integer).
    * arg[2] dimensions - number of dimensions to keep
    *     0 = keep all, do not reduce (integer).
    * arg[3...] input files - one document per line, tab-separated,
    *    last part is processed as the text. 
    * 
    * Go through all input files, take the text, create
    * context vectors.
    * 
    * Expected file format: 
    * tab-separated, one document per line,
    * Last part is text that is processed here
    * (this corresponds to the file format produced by the
    * Amazon review downloader for example, but also if you
    * just have a text file).
    * 
    * @param args
    */
   public static void main(String[] args) {
      
      String alias = "";
      if (args.length > 2) {
         alias = args[0];
         window = Integer.parseInt(args[1]);
         dimensions = Integer.parseInt(args[2]);
      } else {
         System.err.println("Usage: <alias> <window> <dimensions> <files>*");
         System.err.println("alias - just some identifying name to enable you to have various files (String, only letters).");
         System.err.println("window - token window size (integer).");
         System.err.println("dimensions - number of dimensions to keep; 0 = keep all, do not reduce (integer).");
         System.err.println("input files - one document per line, tab-separated, last part is processed as the text.");
         System.exit(1);
      }
      if (args.length >= 3) {
         for (int i=3; i<args.length; i++) {
            files.add(args[i]);
            System.out.println("process file " + args[i]);
         }
      }

      System.out.println(alias + ": use window of " + window + " and " + dimensions + " dimensions.");
      VectorSpaceSimCreateVectors bla = new VectorSpaceSimCreateVectors();
      
      bla.doStuff(files);
      bla.reduceDimensions(dimensions);
      String vectorOutFilename = VectorSpaceSimilarity.getModelFileName(alias, window, dimensions);
      System.out.println("Write to file " + vectorOutFilename);
      bla.writeToFile(vectorOutFilename);
      System.out.println("done.");
   }
   



   private HashMap<String, HashMap<String, Integer>> wordVectors;
   
   private SentenceSplitter sentenceSplitter;
   private Tokenizer tokenizer;

   private List<Character> allowedChars = Arrays.asList('-', '\'', '/',  '&', '+'); // '.',
   // b&w
   // dvd+rw
   // black/white
   

   private List<String> ignoreList = new ArrayList<String>();
   private HashMap<String,Integer> vocabulary = new HashMap<String,Integer>();

   int totaldocno = 0;
   int totalsentenceno = 0;
   int totaltokenno = 0;

   /**
    * Initialize.
    */
   public VectorSpaceSimCreateVectors () {
      
      // Initialize tokenizer (Stanford)
      tokenizer = new TokenizerStanford();

      // Initialize sentence splitter (Stanford)
      sentenceSplitter = new SentenceSplitterStanford();
      
      // Create my vectors
      wordVectors = new HashMap<String, HashMap<String, Integer>>();
      
   }
   


   

   /**
    * go through all input files and process them.
    * 
    * Expected file format: 
    * tab-separated, one document per line,
    * last part is text that is processed here.
    */
   private void doStuff(List<String> inputFilesNames) {

      try {

         for (String inputFileName : inputFilesNames) {
            System.out.println("Processing file: " + inputFileName);
            BufferedReader fileReader = new BufferedReader(new FileReader(inputFileName));
            this.processFile(fileReader);         
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }

   
   /**
    * Go through one file line-by line.
    * 
    * One line is one document.
    * Split on tab, take last part.
    * Split into sentences and tokens.
    * For each sentence:
    *    for each token:
    *       add up counts from neighboring words
    */
   private void processFile(BufferedReader inputFile) throws IOException {
      
      // Bookkeeping
      String line;
      int docno = 0;
      int sentenceno = 0;
      int tokenno = 0;
      
      // Loop through documents (lines)
      while ((line = inputFile.readLine()) != null) {
         docno++;         
         
         // HUGE format:
         // Document text in one line, take last part.
         // String[] parts = line.split("\t");
         // String text = parts[parts.length-1];
         String text = line;
         TextSpan[] sentenceSpans = sentenceSplitter.split(text);
         
         // Loop sentences
         for (TextSpan span : sentenceSpans) {            
            sentenceno++;
         
            // Tokenize
            String[] tokens = tokenizer.tokenize(span.getCoveredText(text));            

            // Loop tokens
            for (int i=0; i<tokens.length; i++) {

               String token = tokens[i].toLowerCase();
               

               // Ignore punctuation, numbers, etc.
               if (this.ignoreThisWord(token))
                  continue;
               
               // Count how often we have seen this token
               tokenno++;
               Integer seen = vocabulary.get(token);
               if (seen == null) {
                  vocabulary.put(token, 1);
               } else {
                  vocabulary.put(token, seen+1);
               }

               // Get context vector of this token
               HashMap<String, Integer> contextMap = wordVectors.get(token);
               if (contextMap == null) {
                  contextMap = new HashMap<String, Integer>();
                  wordVectors.put(token, contextMap);
               }
               
               // Add to context
               int begin = java.lang.Math.max(0,i-window);
               int end = java.lang.Math.min(tokens.length-1,i+window);
               for (int j=begin; j<=end; j++) {
                  if (i != j) {
                     String contextWord = tokens[j].toLowerCase();

                     // Ignore punctuation, numbers, etc.
                     if (this.ignoreThisWord(contextWord))
                        continue;
                     
                     // Otherwise add 1 to dimension in context vecotr
                     Integer amount = contextMap.get(contextWord);
                     if (amount == null) {
                        contextMap.put(contextWord, 1);
                     } else {
                        contextMap.put(contextWord, amount+1);
                     }
                  }
               }
            }
         }

         //if (docno>=1) break; // DEBUG
         
         if (docno % 1000 == 0) {
            System.out.println("... " + docno + " documents (" + sentenceno + " sentences) ...");
         }
         
      }
      totaldocno+=docno;
      totalsentenceno+=sentenceno;
      totaltokenno+=tokenno;
      System.out.println("... processed " + docno + " documents with " + sentenceno + " sentences and " + tokenno + " tokens.");
      System.out.println("... processed " + totaldocno + " documents with " + totalsentenceno + " sentences and " + totaltokenno + " tokens  [accumulative].");
      System.out.println("... vocabulary size " + vocabulary.size() + " , ignored " + ignoreList.size() + " [accumulative].");

      
   }
   

   /**
    * Determine whether we should ignore this word.
    * Adds it to the ignore list
    * 
    * @param token
    * @return true if should be ignored, otherwise false.
    */
   private boolean ignoreThisWord(String contextWord) {
      if (!this.isWord(contextWord)) {
         if (!ignoreList.contains(contextWord)) {
            ignoreList.add(contextWord);
         }
         return true;
      }
      return false;
   }


   /**
    * Determine if the token is a "word" in the sense of what we
    * want to have in our vector. 
    * Reject words that have non-allowed characters or are only
    * punctuation or digits.
    * 
    * @param token
    * @return true if it is a word, otherwise false.
    */
   private boolean isWord (String token) {
      // Reject words that have non-allowed characters
      // (punctuation etc.)
      // Reject also words that have no letter but only
      // digits and punctuation
      boolean hasLetter = false;
      for (int i=0; i<token.length(); i++) {
         char c = token.charAt(i);
         if (!Character.isLetterOrDigit(c) & !(allowedChars.contains(c))) {
            if ((c == '.') & (i==token.length()-1)) {
               return hasLetter; // allow . as last char (abbreviations)
            }            
            return false;
         }
         if (Character.isLetter(token.charAt(i)))
            hasLetter = true;
      }
      return hasLetter;
   }
   
   
   /**
    * Select n most frequent dimensions.
    * 
    * Delete all others
    */
   private void reduceDimensions(int numberOfDimensions) {
      if (numberOfDimensions == 0) {
         System.out.println("Keep all " + this.vocabulary.size() + " dimensions.");
         return;
      }
      
      System.out.println("Reduce from " + this.vocabulary.size() + " dimensions to " + numberOfDimensions);
      

      // If numberOfDimensions is bigger than dimensions actually there, forget it all
      if (numberOfDimensions >= this.vocabulary.size()) {
         System.err.println("WARNING: Number of dimensions is bigger than vocabulary size.");
         return;
      }
      
      // Sort list by number of times the token was seen
      List<Entry<String, Integer>> sorted = HashMapHelpers.sortHashMapByValueDescending(this.vocabulary);
            

      // keep the X with the highest number
      // i.e., go through list from X on and remember tokens to delete
      // TODO what happens with same numbers at cutoff point? arbitrary decision?
      ArrayList<String> tokensToDelete = new ArrayList<String>();
      for (int i=numberOfDimensions; i<sorted.size(); i++) {
         Entry<String, Integer> entry = sorted.get(i);
         tokensToDelete.add(entry.getKey());
      }
      
      // Go through all vectors and delete dimensions
      List<String> toDelete = new ArrayList<String>();
      for (Entry<String, HashMap<String, Integer>> entry : wordVectors.entrySet()) {
         HashMap<String, Integer> contextMap  = entry.getValue();
         // Delete dimensions
         for (String token : tokensToDelete) {
            contextMap.remove(token);
         }
         // Mark whole vector for deletion if no dimensions are left
         if (contextMap.isEmpty()) {
            toDelete.add(entry.getKey());
         }
      }
      
      // Delete now-empty vectors
      for (String token : toDelete) {
         wordVectors.remove(token);
      }
      
   }

   


   /**
    * Write whole vector stuff to file.
    */
   private void writeToFile(String outputFileName) {

      try {
         FileOutputStream fileOut = new FileOutputStream(outputFileName);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(wordVectors);
         out.close();
         fileOut.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   


}
