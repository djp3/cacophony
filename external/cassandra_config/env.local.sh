#! /bin/bash

###
#Adjust the following to match your local environment

CACOPHONY_BASE=/Users/djp3/Development/Mac/EclipseWorkspaceCacophony
LOCAL_HOSTNAME=localmac.djp3

#By default this will try and figure out your ipaddress automatically
#	if this doesn't work, then set it manually as per the second line
CASSANDRA_HOST_IP=`ifconfig | grep "\<inet\>" | grep broadcast | cut -d" " -f2 | head -n1`
#CASSANDRA_HOST_IP=127.0.0.1

#default is 9160
CASSANDRA_PORT=9160

CACOPHONY_TMP=$CACOPHONY_BASE/tmp
CACOPHONY_EXTERNAL=$CACOPHONY_BASE/external

CASSANDRA_BASE=$CACOPHONY_EXTERNAL/cassandra
CASSANDRA_CONF=$CASSANDRA_BASE/conf

CASSANDRA_CLI=$CASSANDRA_BASE/bin/cassandra-cli

CASSANDRA_LOCAL_CONF=$CACOPHONY_EXTERNAL/cassandra_config

#You should make a copy of the files below, edit them as appropriate, then
# change these variables to point to your copies
MY_YAML=$CASSANDRA_LOCAL_CONF/local.djp3.single.yaml
MY_TOPOLOGY=$CASSANDRA_LOCAL_CONF/local.djp3.single.topology.properties
MY_RACKDC=$CASSANDRA_LOCAL_CONF/local.djp3.single.rackdc.properties


###
#Adjust the following command locations if necessary
ECHO=/bin/echo
ID=/usr/bin/id

