// (c) Wiltrud Kessler
// 31.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;


/**
 * Read a String and get a type.
 * 
 * @author kesslewd
 *
 */
public enum SimilarityType {

   DummyZero,
   DummyOne,
   
   // only dependent on words
   VectorSpace,
   DependencyRelation,
   ProductName,
   
   // dependent on words and predicates
   Location,
   LocationDirection,

   // dependent on words and sentence
   ContextFlat,
   ContextSubtree,
   
   // dependent on words, predicates and sentences
   PathDirection,
   PathCompareDependencies,
   PathCompareWords, 
   
   ;


   /**
    * Possibilities:
    * 0 = DummySimilarity(0), 
    * 1 = DummySimilarity(1), 
    * 
    * vs = VectorSpaceSimilarityCached [semantic, flat],
    * prodname = ProductNameSimilarity [semantic, flat],
    * window = ContextSimilarity('vs', false) [semantic, flat context],
    * treewindow = ContextSimilarity('vs', true) [semantic, tree context],
    * 
    * dep = DependencyRelationSimilarity [syntactic, flat],
    * positioni = PositionSimilarity [syntactic, flat context],
    * position = PositionDirectionSimilarity [syntactic, flat context],
    * level = PathDirectionSimilarity [syntactic, tree context],
    * pathdep = PathSimilarity('dep') [syntactic, tree context],
    * pathvs = PathSimilarity('vs') [syntactic, tree context],
    * 
    * @param name name of similarities you want to load.
    * @return the corresponding Similarity, or 'null' if not found.
    */
   public static SimilarityType getTypeFromString (String name) {

      if (name.equals("0")) 
         return DummyZero;

      if (name.equals("1"))  
         return DummyOne;

      if (name.equals("vs")) 
         return VectorSpace;

      if (name.equals("dep")) 
         return DependencyRelation;

      if (name.equals("positioni")) 
         return Location;
      
      if (name.equals("position")) 
         return LocationDirection;

      if (name.equals("level")) 
         return PathDirection;

      if (name.equals("window")) 
         return ContextFlat;

      if (name.equals("treewindow")) 
         return ContextSubtree;

      if (name.equals("pathdep")) 
         return PathCompareDependencies;

      if (name.equals("pathvs")) 
         return PathCompareWords;

      if (name.equals("prodname")) 
         return ProductName;
      
      return null;
   }
   

   
   
   

}
