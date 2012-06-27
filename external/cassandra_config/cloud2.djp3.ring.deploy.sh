#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id

S1=cloud2.waitscout.com

if [ -z "$1" ]
   then
	FORMAT=""
   else
	FORMAT="$1"
fi


./cloud.djp3.ring.killcassandra.sh

sleep 2

$ECHO "$FORMAT" "Starting cassandra in ring"

cp -v /home/don/external/cassandra_config/cloud.djp3.ring.yaml /home/don/external/cassandra/conf/cassandra.yaml

cp -v /home/don/external/cassandra_config/cloud.djp3.ring.topology.properties /home/don/external/cassandra/conf/cassandra-topology.properties

pushd .

cd /home/don/external/cassandra

cassandra_home=/home/don/external/cassandra

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


bin/cassandra -p /home/don/external/cassandra_tmp/cassandra.ring.pid > /home/don/external/cassandra_tmp/cassandra.$S1.ring.output &



$ECHO "$FORMAT" ""

popd


$ECHO "$FORMAT" "Output"
$ECHO "$FORMAT" ""
sleep 2

tail -n 500 -F /home/don/external/cassandra_tmp/cassandra.$S1.ring.output 

#$cassandra_home/bin/nodetool -h 204.232.202.52 -p 8080 ring
