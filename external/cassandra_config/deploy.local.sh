#! /bin/bash

source ./env.local.sh

$ECHO "First, stopping old instances of cassandra"

source ./kill.local.sh


read -p "Start a new Cassandra process (Y|n)?"
if [ ! -z "$REPLY" ] && [[ "$REPLY" != "y" ]] && [[ "$REPLY" != "Y" ]]
	then
		exit 1
fi

cp -v $CASSANDRA_LOCAL_CONF/local.djp3.single.yaml $CASSANDRA_CONF/cassandra.yaml

cp -v $CASSANDRA_LOCAL_CONF/local.djp3.single.topology.properties $CASSANDRA_CONF/cassandra-topology.properties

cp -v $CASSANDRA_LOCAL_CONF/local.djp3.single.rackdc.properties $CASSANDRA_CONF/cassandra-rackdc.properties

pushd .

cd $CASSANDRA_BASE

# The java classpath (required)
CLASSPATH=$CASSANDRA_CONF:$CASSANDRA_BASE/build/classes/main:$CASSANDRA_BASE/build/classes/thrift

for jar in $CASSANDRA_BASE/lib/*.jar $CASSANDRA_BASE/build/lib/jars/*.jar; do
    CLASSPATH=$CLASSPATH:$jar
done


bin/cassandra -p $CACOPHONY_TMP/cassandra.single.pid > $CACOPHONY_TMP/cassandra.$S1.single.output &

$ECHO ""

popd

$ECHO "Tailing output, hit CTRL-C to stop tail (cassandra will continue to run)"
$ECHO ""
sleep 2

tail -n 500 -F $CACOPHONY_TMP/cassandra.$S1.single.output 
