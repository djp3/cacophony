#! /bin/bash

DEST=cloud3.waitscout.com

echo "***   "
echo "***   Building jar"
ant -f ant_node.xml 

echo "***   "
echo "***   Moving files"
rsync -e "ssh -p 31415 -l don" --delete --progress -arz --files-from=deploy.batch.txt . don@$DEST:/home/don

echo "***   "
echo "***   Logging on"
ssh -p 31415 don@$DEST
