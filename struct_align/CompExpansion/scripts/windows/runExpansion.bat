set "arg1=%1"
mkdir "data/windows/%arg1%/windows/"
copy models\wordvectors_elecrev_2_300.ser "models/wordvectors_elecrev%arg1%_2_300.ser"
java -Xmx12G -cp "bin/;../CompBase/bin;lib/sqlite-jdbc-3.7.2.jar" de.uni_stuttgart.ims.expansion.ExpandTrainingSet -actype labeled,ancdesc -simargs vs,window,dep,position,level,pathdep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts elecrev%arg1%,2,300 "data/mate-all-labelled-cleaned.txt" "data/unlabelled/%arg1%.parsed.txt" "data/windows/%arg1%/expansion" "logs/win-%arg1%.log"
