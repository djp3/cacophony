#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id

S1=localmac.djp3

if [ -z "$1" ]
   then
	FORMAT=""
   else
	FORMAT="$1"
fi


$ECHO "$FORMAT" "Stopping cassandra"
PID=`cat /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_tmp/cassandra.pid`

if [ -z "$PID" ]
   then
		echo "Cassandra is not runnning"
   else
		echo "killing job: $PID"
		kill $PID
fi

sleep 1

$ECHO "$FORMAT" ""

ps auxwww | grep cass

$ECHO "$FORMAT" ""
$ECHO "$FORMAT" ""
$ECHO "$FORMAT" ""
exit 0


