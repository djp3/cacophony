#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id
CASSANDRA_CLI=/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra/bin/cassandra-cli
CASSANDRA_HOST=128.195.59.158
CASSANDRA_PORT=9160

echo "drop keyspace CacophonyKeyspaceV1_0s;" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT
echo "drop keyspace CacophonyKeyspaceV1_0;" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT


