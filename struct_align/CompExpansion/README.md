# CompExpansion

Training set expansion of labeled comparisons with structural alignment.
Code for paper (Kessler and Kuhn, 2015).

__WARNING__: This is research code, it was not written with anybody else in mind nor with the goal of applying it "in real life". So it is hacky and may not be usable at all for you. Also, efficiency was not really my concern, so it is reeeeally slow when you use a lot of data.



## Prerequisites

To run the expansion you will need:

- [CompBase](https://github.com/WiltrudKessler/CompBase): 
    Basic data structures, in-/output and just general helpful stuff for my project.
    
- [SQLite](https://www.sqlite.org/), aka `sqlite-jdbc-3.7.2.jar`:
    Needed for caching for the vector space similarity measure in `de.uni_stuttgart.ims.expansion.similarity.VectorSpaceSimilarityCached`. If this is not found it falls back to a version without caching that may be a bit more slow, but it will work.
    
- Context vectors for the vector space similarity. You can either use the ones provided in `models/wordvectors_leok_2_2000.ser`, or create your own (see below).


### Create context vectors

This assumes that you have the basic stuff from `CompBase` compiled in a directory `../CompBase/bin`, the Stanford CoreNLP jar in `../CompBase/lib` and the model files for the Stanford tokenizer and sentence splitter in a place where they are found by the tools.

Compile all the files:
    javac -cp bin:../CompBase/bin -d bin src/de/uni_stuttgart/ims/expansion/similarity/VectorSpaceSimCreateVectors.java

You will need to have a large amount of data from which to create the vectors from. Preferably in the domain. Use for example the huge set of Amazon reviews provided as [Amazon Product Review Data (more than 5.8 million reviews)](https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#datasets) by Bing Liu.

The creation of context vectors will need as parameters: (1) an alias (just to enable you to have several different such files), e.g., "a". (2) the number of tokens that should be considered to be "context", i.e., the window size, e.g., "2". (3) The number of dimensions you want your final vectors to have, e.g., 2000. It will just keep the X most frequent words, not a do fancy dimension reduction. (4) the files with your data, the expected format is one line per document, tab-separated fields, text of the documents in the last part, e.g., the files `data1.csv` and `data2.csv`.

    java -cp bin:../CompBase/bin:../CompBase/lib/stanford-corenlp-3.2.0.jar de.uni_stuttgart.ims.expansion.similarity.VectorSpaceSimCreateVectors a 2 2000 "data1.csv" "data2.csv" 

You may get a lot of "WARNING: Untokenizable" messages - that is ok if you work with English. It's just foreign characters that confuse the tokenizer and these words will be ignored.
The created vectors will be in the file `models/wordvectors_a_2_2000.ser`.

To use these vectors in the expansion, give the same values (alias, window, dimensions) in the option `vsopts` as one word separated with commas, e.g., for the above `-vsopts a,2,2000`. This will load the corresponding file (if it is there).



## Running the code

### Usage

This assumes that you have the basic stuff from `CompBase` compiled in a directory `../CompBase/`, the SQLite jar in the folder `lib`, the context vectors in `models/wordvectors_leok_2_2000.ser` and the class files in `bin`.

Compile all the files:

    mkdir bin
    javac -cp src:bin:../CompBase/bin:../lib/sqlite-jdbc-3.7.2.jar -d bin src/de/uni_stuttgart/ims/expansion/ExpandTrainingSet.java

Open `runExpansion.sh`, adapt the paths to your files and run the script. It should look like this:

    JAVAPARAMS="-Xmx12G -cp bin/:../CompBase/bin:../lib/sqlite-jdbc-3.7.2.jar"
    ALLOPTS="-actype labeled,paths -simargs dep,position -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000"
    SEEDS="data/seedsentences.parsed.txt"
    UNLABELED="data/unlabeledsentences.parsed.txt"
    OUTFOLDER="data/output/"
    LOGFILE="expansion.log"

    mkdir $OUTFOLDER
    java $JAVAPARAMS de.uni_stuttgart.ims.expansion.ExpandTrainingSet $ALLOPTS $SEEDS $UNLABELED $OUTFOLDER/expansion $LOGFILE


It is recommended to run the expansion on a machine with a lot of memory. The code is not optimized for speed, so if you run it on a large set of data it will take a while (depending on the similarities you chose and also on the number of argument candidates created).

In the end you will get two files for each predicate in each seed sentence where at least one expansion sentence is found. For the example data, you will get a file `data/output/expansion.1_7_higher.txt` which contains the labeled expansion sentences for predicate "higher", word number seven in the first sentence. The file `data/output/expansion.1_7_higher.scores` contains the alignments and the scores for the alignments for the same sentences.


### What does this actually do... ?

Basically, we have a small set of labeled seed sentences and a large set of unlabeled sentences. From the unlabeled sentences we want to select the ones most similar to a specific labeled seed sentence and project the labels onto them.

The annotations we want to project are comparisons. A comparison consists of a predicate and some arguments: the entities that are compared and an aspect they are compared in. For example in "A has a better resolution than B" the word "better" is the predicate, "A" and "B" are the entities and "resolution" is the aspect.

Say we have a seed sentence s with predicate p. Do the following for every unlabeled sentence u:
- if u does not contain p, discard the sentence 
- get all argument candidates from u and l
- score all possible alignments between the two candidate sets
- save best alignment and score

When we are done with all unlabeled sentences, sort all u by their alignment score and keep the n best sentences. Project the annotations from the labeled seed sentence s along the alignments and annotate the unlabeled sentences.

For more info read my paper listed below!


### Configurations for the RANLP paper

Differences are in the argument candidate creation (`-actype`) and the argument similarities (`-simargs`).

Setting "Path-Flat" :

    ALLOPTS="-actype labeled,paths -simargs vs,dep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000"
   
Setting "Dep-Flat" :

    ALLOPTS="-actype labeled,ancdesc -simargs vs,dep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000"

   
Setting "Path-Context" :

    ALLOPTS="-actype labeled,paths -simargs vs,window,dep,position,level,pathDeps -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000"

   
Setting "Dep-Context" :

    ALLOPTS="-actype labeled,ancdesc -simargs vs,window,dep,position,level,pathDeps -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000"



## Information for developers

Main class: `de.uni_stuttgart.ims.expansion.ExpandTrainingSet`

- `de.uni_stuttgart.ims.expansion.semisupervised`:
   Implementation of structural alignment in `SemiSupervisedExpansion`, uses everything else.
   
- `de.uni_stuttgart.ims.expansion.sentences`:
   Select a set of possible expansion sentences for a given seed sentence and predicate; 
   Identify the predicate candidates in the unlabeled sentence.
   Different methods are subclasses of `SentenceSelector`.

- `de.uni_stuttgart.ims.expansion.candidates`:
   Get the argument candidates from the sentences.
   Different methods are subclasses of `ArgumentCreator`.

- `de.uni_stuttgart.ims.expansion.similarity`:
   Compare two words and assign a similarity score;
   Give an overall similarity score for the whole alignment.
   Different methods are subclasses of `Similarity', combination of similarities use `ComposedSimilarity`.

- `de.uni_stuttgart.ims.expansion.alignment`:
   Do all alignments between candidates and chose the one with the highest similarity.
   Different methods are subclasses of `BestAlignmentFinder`.
   
- `de.uni_stuttgart.ims.expansion.lists`:
   Lists to sort found alignments by similarity scores.
   Different lists are subclasses of `BestScoreList`.



## Licence and References

(c) Wiltrud Kessler

This code is distributed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported license
[http://creativecommons.org/licenses/by-nc-sa/3.0/](http://creativecommons.org/licenses/by-nc-sa/3.0/)

Please cite:
Wiltrud Kessler and Jonas Kuhn (2015)
"Structural Alignment for Comparison Detection"
In Proceedings of the 10th Conference on Recent Advances in Natural Language Processing (RANLP 2015).


Copyright notice for `de.uni_stuttgart.ims.expansion.alignment.HungarianAlgorithm.java`:

> Copyright (c) 2012 Kevin L. Stern
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.
