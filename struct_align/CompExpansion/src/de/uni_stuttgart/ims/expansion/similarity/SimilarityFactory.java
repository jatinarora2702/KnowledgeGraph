// (c) Wiltrud Kessler
// 31.10.2014
// This code is distributed under a Creative Commons
// Attribution-NonCommercial-ShareAlike 3.0 Unported license 
// http://creativecommons.org/licenses/by-nc-sa/3.0/


package de.uni_stuttgart.ims.expansion.similarity;



/**
 * Read a String and get a Similarity class.
 * 
 * @author kesslewd
 *
 */
public class SimilarityFactory {

   /**
    * THE vector space similarity (singleton).
    * We only want one instance of this because of memory.
    */
   private static VectorSpaceSimilarity vssim = null;
   
   /**
    * Settings for vector space similarity.
    */
   private static String vsargs = null;

   /**
    * Did the settings for vector space similarity
    * change since the time we created the instance?
    */
   private static boolean vsargschanged = false;
   
   
   /**
    * Set the settings for vector space similarity.
    * 
    * @param vsargs See 'SimilarityFactory.vsargs'
    */
   public static void setVSSimArgs (String vsargs) {
      if (!vsargs.equals(SimilarityFactory.vsargs)) {
         vsargschanged = true;
         SimilarityFactory.vsargs = vsargs;
      }
   }
   

   /**
    * Gets you the instance of the vector space similarity.
    * 
    * @return The vector space similarity ('SimilarityFactory.vssim')
    * @throws Exception If no options are given ('setVSSimArgs')
    */
   public static Similarity getVSSim() throws Exception {
      if (vssim == null || vsargschanged) {
         if (vsargs != null) {
            String[] myargs = vsargs.split(",");
            System.out.println("get me a new vssim");
            vssim = new VectorSpaceSimilarityCached(myargs[0], Integer.parseInt(myargs[1]), Integer.parseInt(myargs[2]));
            vsargschanged = false;
         } else {
            throw new Exception("Error, if you want to use VSsim, you need to specify the options (vsargs)");
         }
      } 
      return vssim;
   }
   
   
   
   
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
    * You can combine as many similarity as you like with a comma,
    * e.g., 'vs,dep,loc' will give a ComposedSimilarity
    * with three entries: 
    * VectorSpaceSimilarityCached, DependencyRelationSimilarity, PositionSimilarity
    * 
    * Entries can be duplicates, then several instances of the same
    * similarity measure will be added.
    * 
    * @param name name of similarities you want to load.
    * @return composed similarity of all the things you said.
    * @throws Exception If there is something in the list that I
    *    don't know.
    */
   public static Similarity getSimilarity (String name) throws Exception {
      @SuppressWarnings("resource")
      ComposedSimilarity simmy = new ComposedSimilarity();
      
      String[] parts = name.split(",");
      
      for (String part : parts) {
         
         SimilarityType type = SimilarityType.getTypeFromString(part);
         
         if (type == null)
            throw new Exception("Error, unknown value for part: " + part);
      
         switch (type) {
         case DummyOne : 
            simmy.addSimilarityMeasure(new DummySimilarity(1));
            break;
         case DummyZero : 
            simmy.addSimilarityMeasure(new DummySimilarity(0));
            break;
         case VectorSpace :
            simmy.addSimilarityMeasure(getVSSim());
            break;
         case DependencyRelation :
            simmy.addSimilarityMeasure(new DependencyRelationSimilarity());
            break;
         case Location :
            simmy.addSimilarityMeasure(new PositionSimilarity());
            break;
         case LocationDirection :
            simmy.addSimilarityMeasure(new PositionDirectionSimilarity());
            break;
         case ContextFlat :
            simmy.addSimilarityMeasure(new ContextSimilarity(getVSSim(), false));
            break;
         case ContextSubtree :
            simmy.addSimilarityMeasure(new ContextSimilarity(getVSSim(), true));
            break;
         case PathDirection :
            simmy.addSimilarityMeasure(new PathDirectionSimilarity());
            break;
         case PathCompareDependencies :
            simmy.addSimilarityMeasure(new PathSimilarity(new DependencyRelationSimilarity()));
            break;
         case PathCompareWords :
            simmy.addSimilarityMeasure(new PathSimilarity(getVSSim()));
            break;
         case ProductName :
            simmy.addSimilarityMeasure(new ProductNameSimilarity());
            break;
         default : 
            throw new Exception("Error, unknown value for similarity part: " + part);
         }
      }
         
      return simmy;
      
   }

}
