#! /bin/bash

#Adjust the following 
CACOPHONY_BASE=/Users/djp3/Development/Mac/EclipseWorkspaceCacophony
LOCAL_HOSTNAME=localmac.djp3

#Try and figure out ipaddress, if this doesn't work, then set it manually
#CASSANDRA_HOST_IP=127.0.0.1
CASSANDRA_HOST_IP=`ifconfig | grep "\<inet\>" | grep broadcast | cut -d" " -f2 | head -n1`
CASSANDRA_PORT=9160

#Adjust the following paths, if necessary.  The default is the expected file structure

CACOPHONY_TMP=$CACOPHONY_BASE/tmp
CACOPHONY_EXTERNAL=$CACOPHONY_BASE/external

CASSANDRA_BASE=$CACOPHONY_EXTERNAL/cassandra
CASSANDRA_CONF=$CASSANDRA_BASE/conf

CASSANDRA_CLI=$CASSANDRA_BASE/bin/cassandra-cli

CASSANDRA_LOCAL_CONF=$CACOPHONY_EXTERNAL/cassandra_config

#Adjust the following command locations if necessary
ECHO=/bin/echo
ID=/usr/bin/id

