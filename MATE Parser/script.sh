java -cp  anna-3.3.jar is2.util.Split  'labelled-all-sentences.txt' > 'one-word-per-line.txt'
java -cp anna-3.3.jar is2.lemmatizer.Lemmatizer -model 'lemmatizer.model' -test 'one-word-per-line.txt' -out 'lemmatized.txt'
java -cp anna-3.3.jar is2.tag.Tagger -model 'postagger.model' -test 'lemmatized.txt' -out 'postagged.txt'
java -cp anna-3.3.jar is2.parser.Parser -model 'parser.model' -test 'postagged.txt' -out 'labelled-all-sentences.parsed.txt'
