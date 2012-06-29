#! /bin/bash

ECHO=/bin/echo

if [ -z "$1" ]
   then
	FORMAT=""
   else
	FORMAT="$1"
fi


$ECHO "$FORMAT" "Stopping cassandra ring"
PID=`cat /home/don/external/cassandra_tmp/cassandra.ring.pid`

if [ -z "$PID" ]
   then
		echo "Cassandra is not running"
   else
		echo "killing job: $PID"
		kill $PID
fi

sleep 5

$ECHO "$FORMAT" ""

ps auxwww | grep cass

$ECHO "$FORMAT" ""
$ECHO "$FORMAT" ""
$ECHO "$FORMAT" ""
exit 0


