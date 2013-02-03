#! /bin/bash

source ./env.local.sh

$ECHO "Destroy Schema?"
$ECHO "    Cassandra must be running on $CASSANDRA_HOST_IP:$CASSANDRA_PORT for this to work"
read -p "(Y|n)?"
if [ ! -z "$REPLY" ] && [[ "$REPLY" != "y" ]] && [[ "$REPLY" != "Y" ]]
	then
		exit 1
fi

$ECHO "    Destroying keyspaces at $CASSANDRA_HOST_IP:$CASSANDRA_PORT"
if [ -e $CACOPHONY_TMP/cassandra.single.pid ]
    then
		if [ ! -z $CACOPHONY_TMP/cassandra.single.pid ]
			then
				$ECHO "*** CacophonyKeyspaceV1_0_test"
				$ECHO "drop keyspace CacophonyKeyspaceV1_0_test;" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null
				$ECHO ""

				$ECHO "*** CacophonyKeyspaceV1_0_r"
				$ECHO "drop keyspace CacophonyKeyspaceV1_0r;" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null
				$ECHO ""

				$ECHO "*** CacophonyKeyspaceV1_0_s"
				$ECHO "drop keyspace CacophonyKeyspaceV1_0s;" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null
				$ECHO ""

				$ECHO "*** CacophonyKeyspaceV1_0"
				$ECHO "drop keyspace CacophonyKeyspaceV1_0;" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null
				$ECHO ""
		fi
	else
		echo "        Cassandra is not running"
fi




