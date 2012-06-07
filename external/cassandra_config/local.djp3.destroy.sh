#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id
CASSANDRA_CLI=/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra/bin/cassandra-cli
CASSANDRA_PORT=9160

echo "drop keyspace CacophonyKeyspaceV1_0;" | $CASSANDRA_CLI -p$CASSANDRA_PORT


