// (c) Wiltrud Kessler
// 22.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion;

import de.uni_stuttgart.ims.expansion.alignment.*;
import de.uni_stuttgart.ims.expansion.candidates.*;
import de.uni_stuttgart.ims.expansion.sentences.*;
import de.uni_stuttgart.ims.expansion.similarity.*;


/**
 * Read options for the expansion from the command line. 
 * 
 * @author kesslewd
 *
 */
public class ExpansionOptions {
   
   // Avoid loading VS similarity more than once
   // not static -> allow different options
   // but inside one options, always use same
   /**
    * -vsopts: Options for vector space similarity
    */
   private static String vsopts = null;
      
   
   // Parameters that can set by options (otherwise assume defaults)
   
   /**
    * -simargs: Similarity measure for arguments
    */
   private Similarity sim = null;
   
   /**
    * -simpreds: Similarity measure for predicates
    */
   private Similarity simPreds = null;
   
   /**
    * -actype: Argument candidate creator
    */
   private ArgumentCreator ac = null;
   
   /**
    * -aftype: Alignment finder
    */
   private BestAlignmentFinder af = null;
   
   /**
    * -seltype: Sentence selector
    */
   private SentenceSelector sentenceselector = null;
   
   /**
    * -maxSeed: Maximum seed sentence number to process.
    */
   private int maxSeed = Integer.MAX_VALUE;
   
   /**
    * -minSeed: Minimum seed sentence number to process.
    */
   private int minSeed = 1;
   
   /**
    * -n: Number of best expansion sentences to get.
    */
   private int n = 10;

   

   /**
    * Loop through all args and instantiate things.
    * 
    * Available options (always with an argument):
    * -simargs: Similarity measure for arguments
    * -simpreds: Similarity measure for predicates
    * -actype: Argument candidate creator
    * -seltype: Sentence selector
    * -aftype: Alignment finder
    * -n: Number of best expansion sentences to get.
    * -maxSeed: Maximum seed sentence number to process.
    * -minSeed: Minimum seed sentence number to process.
    * -vsopts: Options for vector space similarity
    *
    * @param args The array to go through.
    * @throws Exception Something is not right.
    */
   public void parseOptions (String[] args) throws Exception {
      parseOptions(args, 0, args.length);
   }

   
   /**
    * Loop through args and instantiate things.
    * 
    * @param args The array to go through.
    * @param startIndex First element to process in the array is args[startIndex]
    * @param endIndex First element to process in the array is args[endIndex-1]
    * @throws Exception Something is not right.
    */
   public void parseOptions (String[] args, int startIndex, int endIndex) throws Exception {
      
      String actype = null;
      String aftype = null;
      String seltype = null;
      
      String simargs = null;
      String simpreds = null;

      boolean thisknown = false;
      boolean nextknown = false;
      
      for (int i=startIndex; i<endIndex; i++) {
         
         if (nextknown) {
            nextknown = false;
            continue;
         }
         
         if (args[i].equals("-simargs")) {
            simargs = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-simpreds")) {
            simpreds = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-actype")) {
            actype = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-seltype")) {
            seltype = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-aftype")) {
            aftype = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-n")) {
            n = Integer.parseInt(args[i+1]);
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-maxSeed")) {
            maxSeed = Integer.parseInt(args[i+1]);
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-minSeed")) {
            minSeed = Integer.parseInt(args[i+1]);
            thisknown = true;
            nextknown = true;
         }
         if (args[i].equals("-vsopts") | args[i].equals("-vsfile")) {
            vsopts = args[i+1];
            thisknown = true;
            nextknown = true;
         }
         
         if (!thisknown) {
            System.err.println("Error, unknown option " + args[i]);
         }
         thisknown = false;
         

      }

      // Parameter Similarity for arguments
      if (simargs != null) {
         sim = getSimilarity(simargs);
      } else {
         sim = new DummySimilarity(1);
         System.err.println("No 'simargs' option set: Assume default value: " + sim.getName());
      }

      // Parameter Similarity for predicates
      if (simpreds != null) {
         simPreds = getSimilarity(simpreds);
      } else {
         simPreds = null;
         System.err.println("No 'simpreds' option set: Assume default value: null"); 
      }
      
      
      // Parameter Argument candidate creator
      ac = ArgumentCreatorFactory.getArgumentCreator(actype);
      if (ac == null) {
         ac = ArgumentCreatorFactory.getDefaultAC();
         System.err.println("No 'actype' option set: Assume default value: " + ac.getName());
      }
      
      // Parameter Alignment finder
      if (aftype == null) {
         af = new BestAlignmentFinderMatrix(sim);
         System.err.println("No 'aftype' option set: Assume default value: " + af.getName());
      } else if (aftype.equals("matrix")) {
         af = new BestAlignmentFinderMatrix(sim);
      } else if (aftype.equals("generator")) {
         af = new BestAlignmentFinderGenerator(sim);
      } else {
         throw new Exception("Error, unknown value for aftype: " + aftype);
      }         

      
      // Parameter sentence subselection
      sentenceselector = SentenceSelectorFactory.getSentenceSelector(seltype);
      if (sentenceselector == null) {
         sentenceselector = SentenceSelectorFactory.getDefaultSS();
         System.err.println("No 'seltype' option set: Assume default value: " + sentenceselector.getName());
      } 

      
      
   }
   

   /**
    * Get a Similarity from the String.
    * First set the Vector space stuff.
    * 
    * @param optionString Name of similarities to load.
    * @return ComposedSimilarity that contains the stuff you want to load.
    * @throws Exception Something bad!
    */
   private static Similarity getSimilarity (String optionString) throws Exception {
      if (vsopts != null) {
         SimilarityFactory.setVSSimArgs(vsopts);
      }
      return SimilarityFactory.getSimilarity(optionString);
   }


      
   /**
    * -simargs: Similarity measure for arguments
    */
   public Similarity getArgumentSimilarity() {
      return sim;
   }


   /**
    * -simpreds: Similarity measure for predicates
    */
   public Similarity getPredicateSimilarity() {
      return simPreds;
   }


   /**
    * -actype: Argument candidate creator
    */
   public ArgumentCreator getArgumentCreator() {
      return ac;
   }


   /**
    * -aftype: Alignment finder
    */
   public BestAlignmentFinder getAlignmentFinder() {
      return af;
   }


   /**
    * -seltype: Sentence selector
    */
   public SentenceSelector getSentenceSelector() {
      return sentenceselector;
   }


   /**
    * -n: Number of best expansion sentences to get.
    */
   public int getN() {
      return n;
   }


   /**
    * -maxSeed: Maximum seed sentence number to process.
    */
   public int getMaxSeed() {
      return maxSeed;
   }


   /**
    * -minSeed: Minimum seed sentence number to process.
    */
   public int getMinSeed() {
      return minSeed;
   }


   
   

}
