rm -r bin
mkdir bin
javac -cp src:bin:../CompBase/bin:../lib/sqlite-jdbc-3.7.2.jar -d bin src/de/uni_stuttgart/ims/expansion/ExpandTrainingSet.java
javac -cp bin:../CompBase/bin -d bin src/de/uni_stuttgart/ims/expansion/similarity/VectorSpaceSimCreateVectors.java
# sh scripts/runExpansionTest.sh