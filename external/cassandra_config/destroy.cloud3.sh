#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id
CASSANDRA_CLI=/home/don/external/cassandra/bin/cassandra-cli
CASSANDRA_HOST=cloud3.waitscout.com
CASSANDRA_PORT=9160

echo "drop keyspace CacophonyKeyspaceV1_0r;" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT
echo "drop keyspace CacophonyKeyspaceV1_0s;" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT
echo "drop keyspace CacophonyKeyspaceV1_0;" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT


