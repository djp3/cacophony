#! /bin/bash

read -p "Stop Cassandra Process (Y|n)?"
if [ ! -z "$REPLY" ] && [[ "$REPLY" != "y" ]] && [[ "$REPLY" != "Y" ]]
	then
		exit 1
fi

source ./env.local.sh

$ECHO "    Stopping cassandra single"
if [ -e $CACOPHONY_TMP/cassandra.single.pid ]
    then
		if [ ! -z $CACOPHONY_TMP/cassandra.single.pid ]
			then
				PID=`cat $CACOPHONY_TMP/cassandra.single.pid`
				echo "        Killing job: $PID"
				kill $PID
				sleep 5
		fi
	else
		echo "        Cassandra is not running"
fi


$ECHO "    Checking for running instances of cassandra that we couldn't kill (please kill manually if they are listed below)"
$ECHO ""

ps auxwww | grep org.apache.cassandra.service.CassandraDaemon | grep -v grep

$ECHO ""
$ECHO "    Done checking"



