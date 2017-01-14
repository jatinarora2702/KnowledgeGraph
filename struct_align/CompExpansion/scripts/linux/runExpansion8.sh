# change the script for running on some other embeddings (currently using 'elecrev8' embeddings)

JAVAPARAMS="-Xmx12G -cp bin/:../CompBase/bin:lib/sqlite-jdbc-3.7.2.jar"

# Uncomment the settings you want to use
# ALLOPTS="-actype labeled,paths -simargs vs,dep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000" # 1
# ALLOPTS="-actype labeled,ancdesc -simargs vs,dep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000" # 2
# ALLOPTS="-actype labeled,paths -simargs vs,window,dep,position,level,pathdep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts leok,2,2000" # 3
ALLOPTS="-actype labeled,ancdesc -simargs vs,window,dep,position,level,pathdep -simpreds 0 -aftype matrix -seltype comppos -n 30 -vsopts elecrev8,2,300" # 4

SEEDS="data/mate-all-labelled-cleaned.txt"
UNLABELED="data/unlabelled/8.parsed.txt"
OUTFOLDER="data/out/8/"
LOGFILE="logs/8.log"


mkdir $OUTFOLDER
java $JAVAPARAMS de.uni_stuttgart.ims.expansion.ExpandTrainingSet $ALLOPTS $SEEDS $UNLABELED $OUTFOLDER/expansion $LOGFILE
