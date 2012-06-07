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


./local.djp3.killcassandra.sh

sleep 2

$ECHO "$FORMAT" "Starting cassandra"

cp -v /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_config/cassandra_djp3_local.yaml /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra/conf/cassandra.yaml

cp -v /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_config/cassandra_djp3-topology.properties /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra/conf/cassandra-topology.properties

pushd .

cd /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra

cassandra_home=/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra

# The directory where Cassandra's configs live (required)
CASSANDRA_CONF=$cassandra_home/conf

# This can be the path to a jar file, or a directory containing the 
# compiled classes. NOTE: This isn't needed by the startup script,
# it's just used here in constructing the classpath.
cassandra_bin=$cassandra_home/build/classes/main
cassandra_bin=$cassandra_bin:$cassandra_home/build/classes/thrift
#cassandra_bin=$cassandra_home/build/cassandra.jar


# The java classpath (required)
CLASSPATH=$CASSANDRA_CONF:$cassandra_bin

for jar in $cassandra_home/lib/*.jar $cassandra_home/build/lib/jars/*.jar; do
    CLASSPATH=$CLASSPATH:$jar
done


bin/cassandra -p /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_tmp/cassandra.pid > /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_tmp/cassandra.$S1.output &


#$ECHO "$FORMAT" ""
#$ECHO "$FORMAT" "Pausing to get ring"
#sleep 1

#bin/nodetool -h 127.0.0.1 -p 8080 ring

$ECHO "$FORMAT" ""

popd

$ECHO "$FORMAT" "Output"
$ECHO "$FORMAT" ""
sleep 2

tail -n 500 -F /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/cassandra_tmp/cassandra.$S1.output 
