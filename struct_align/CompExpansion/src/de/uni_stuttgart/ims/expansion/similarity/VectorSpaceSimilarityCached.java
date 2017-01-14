// (c) Wiltrud Kessler
// 14.05.2013
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.uni_stuttgart.ims.nlpbase.nlp.SRLSentence;
import de.uni_stuttgart.ims.nlpbase.nlp.Word;


/**
 * Measures the cosine similarity between the context
 * vectors of two words.
 * Caches all values in a SQLite DB.
 * The vectors need to be created beforehand
 * (see class CreateContextVectors)
 * 
 * @author kesslewd
 *
 */
public class VectorSpaceSimilarityCached extends VectorSpaceSimilarity {
   

   /**
    * SQLite DB to cache calculated distances to avoid recalculation.
    */
   private String dbLocation;
   private Statement distancesCacheDB;
   private Connection connection;

   public int foundInCache = 0; // DEBUG
   public int addedToCache = 0; // DEBUG
   
   
   /**
    * Table name in SQLite DB for caching distances.
    */
   private static String tableName = "XYZ";
   
   /**
    * [dev] Debug mode.
    */
   private static boolean debug = false;

   
   /**
    * Commit at every n-th value.
    * Set to 1 for always, set to something high for never.
    */
   private static int commitAt = 50;



   /**
    * Measures the cosine similarity between the context
    * vectors of the two words.
    * The vectors need to be created beforehand, they are loaded
    * from the file 'vectorsFilename'.
    */
   public VectorSpaceSimilarityCached(String alias, int window, int dimensions) {
      this(getModelFileName(alias, window, dimensions)); 
   }
   


   /**
    * Measures the cosine similarity between the context
    * vectors of the two words.
    * The vectors need to be created beforehand, they are loaded
    * from the file 'vectorsFilename'.
    */
   public VectorSpaceSimilarityCached(String vectorsFilename) {
      super(vectorsFilename);
      // Get DB connection for caching

      if (distancesCacheDB == null) {
         dbLocation = "jdbc:sqlite:" + vectorsFilename.replace(".ser", ".db");
         System.out.println("DB connect to " + dbLocation);
         initializeCache();      
      }
   }
   
   

   // Methods for caching / db-storage
   
   
   /**
    * Initialize the Cache (SQLite database) 
    * Sets connection and statement
    */
   private void initializeCache () {
      //System.out.println("use cache");
      try {
         // load the sqlite-JDBC driver using the current class loader
         Class.forName("org.sqlite.JDBC");
         
         // Get a connection to the DB in this file 
         connection = DriverManager.getConnection(dbLocation);
         connection.setAutoCommit(false); // do not commit after every insert -> faster
         distancesCacheDB = connection.createStatement();
         distancesCacheDB.setQueryTimeout(30);  // set timeout to 30 sec.


         // Delete previous table and create anew
         distancesCacheDB.executeUpdate("drop table if exists " + tableName + ""); 
         distancesCacheDB.executeUpdate("create table " + tableName + " (name string, distance real)");

      } catch (ClassNotFoundException e) {
         System.err.println("ERROR in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("Did you forget the SQLite driver? Caching will be disabled, this may seriously harm performance.");
         distancesCacheDB = null;
      } catch (SQLException e) {
         System.err.println("ERROR in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("There is something wrong with the SQLite DB. Caching will be disabled, this may seriously harm performance.");
         distancesCacheDB = null;
      }

   }
   

   
   /**
    * Similarity is symmetric, so we want to get the same identifier for
    * word1/word2 than for word2/word1.
    * Delete characters from words that do not work with database.
    * 
    * @param wordForm1 first word form
    * @param wordForm2 second word form
    * @return An identifier for this word combination independent of order.
    */
   private static String getDBIdentifier (String wordForm1, String wordForm2) {
      wordForm1 = wordForm1.replace("'", "");
      wordForm2 = wordForm2.replace("'", "");
      if (wordForm1.compareToIgnoreCase(wordForm2) < 0)
         return wordForm1 + " " + wordForm2;
      else
         return wordForm2 + " " + wordForm1;
   }
   
   
   /**
    * Check the cache for the similarity between the two words.
    * Return the similarity if found, or -1 if not found and in case of error.
    * 
    * @param wordForm1
    * @param wordForm2
    * @return the similarity if found, or -1 if not found
    */
   private double checkCache (String wordForm1, String wordForm2) {
      
      // Abort if the initialization of the DB didn't work somehow
      if (this.distancesCacheDB == null)
         return -1;
      
      // Get DB identifier (independent of word order)
      String dbEntryID = getDBIdentifier(wordForm1, wordForm2);

      // Query the DB, return the value if found
      String query = null;
      double value = -1;
      try {
         query = "select * from " + tableName + " where name='" + dbEntryID +"'";
         ResultSet result = this.distancesCacheDB.executeQuery(query);
         if (result.next()) { // there should be only one result
            value = result.getDouble("distance");
         }
         result.close();
      } catch (SQLException e) {
         System.err.println("ERROR with caching in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("Query: " + query);
         e.printStackTrace();
      }
       
      // Not found or error
      return value;
   }
   
   
   /**
    * Add a value to the Cache DB.
    * 
    * @param wordForm1
    * @param wordForm2
    * @param distance
    */
   private void addToCache (String wordForm1, String wordForm2, double distance) {

      // Abort if the initialization of the DB didn't work somehow
      if (this.distancesCacheDB == null)
         return;
      
      addedToCache++;
      
      // Get DB identifier (independent of word order)
      String dbEntryID = getDBIdentifier(wordForm1, wordForm2);

      // Insert the value
      String update = null;
      try {
         update = "insert into " + tableName + " values('" + dbEntryID + "', " + distance + ")";
         this.distancesCacheDB.executeUpdate(update);
      
         if (addedToCache % commitAt == 0) {
            //System.out.println("commit");
            connection.commit();
         }
         
      } catch (SQLException e) {
         System.err.println("ERROR with caching in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("Query: " + update);
         e.printStackTrace();
      }
   }   
   
   
   // Methods to calculate vector space similarity
   

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
      
      String wordForm1 = word1.getForm();
      String wordForm2 = word2.getForm();

      // Returned cached value if we have one
      // Return value -1 means not found
      double cachedValue = checkCache(wordForm1, wordForm2);
      if (cachedValue >= 0) {
         //System.out.println("found in cache " + cachedValue); // TEST !!!
         foundInCache ++;
         return new ExplainedSimilarityValue(cachedValue, "cached");
      }


      
      // If both vectors are found, calcuate cosine similarity and return
      double cosine = cosine(nullCheck.vector1, nullCheck.vector2);
      if (debug) System.out.println("cosine " + wordForm1 + " " + wordForm2 + " " + cosine);
      //System.out.println("cosine " + wordForm1 + " " + wordForm2 + " " + cosine);

      //if (cosine < 0.1)
         //System.out.println("cosine is 0 " + wordForm1 + " " + wordForm2 + " " + cosine);
      
      addToCache(wordForm1, wordForm2, cosine);
      
      return new ExplainedSimilarityValue(cosine);
      
   }
   
   
   

   /**
    * May be used to close any resources.
    * @throws SQLException 
    */
   public void close() throws IOException {
      this.closeConnection();
   }
   
   
   
   /**
    * Close DB connection, print some statistics.
    */
   public void commit() {
      try {
         connection.commit();
      } catch (SQLException e) {
         System.err.println("ERROR with caching in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("I was trying to commit my changes.");
         e.printStackTrace();
      }
   }

   /**
    * Close DB connection, print some statistics.
    */
   public void closeConnection() {
      try {
         System.out.println("foundInCache " + foundInCache 
               + "; added to cache " + addedToCache);
         connection.commit();
         distancesCacheDB.close();
      } catch (SQLException e) {
         System.err.println("ERROR with caching in VectorSpaceSimilarity !!! " + e.getMessage());
         System.err.println("I was trying to close the connection.");
         e.printStackTrace();
      }
   }
   
   // don't do this in finalize
   // because it closes the connection at random times when the garbage collector collects one of the objects
   // TODO there may be uncommitted things at the end of use of one of the objects
   // -- manually have to call 'commit'
   // TODO the connection is never closed
   // -- manually have to call 'closeConnection'
   
}
