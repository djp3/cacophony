#! /bin/bash

source ./env.local.sh

$ECHO "Rebuild Schema?"
$ECHO "    Cassandra must be running on $CASSANDRA_HOST_IP:$CASSANDRA_PORT for this to work"
$ECHO "    If schema is already present you'll get an error"
read -p "(Y|n)?"
if [ ! -z "$REPLY" ] && [[ "$REPLY" != "y" ]] && [[ "$REPLY" != "Y" ]]
	then
		exit 1
fi


$ECHO "Building CacophonyKeyspaceV1_0_test"
$ECHO "
create keyspace CacophonyKeyspaceV1_0_test
    with strategy_options = {DC1:1}
    and placement_strategy = 'org.apache.cassandra.locator.NetworkTopologyStrategy';

use CacophonyKeyspaceV1_0_test;
create column family directory_server
    with column_type = 'Standard'
    with comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type'
    and key_validation_class = 'UTF8Type'
	and column_metadata = [
		{	column_name : 'heartbeat',
			validation_class : LongType}]; 

create column family cnode_list
    with column_type = 'Standard'
    with comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type'
    and key_validation_class = 'UTF8Type'
	and column_metadata = [
		{	column_name : 'heartbeat',
			validation_class : LongType},
		{	column_name : 'json_data',
			validation_class : UTF8Type},
			]; 
	" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null

$ECHO "Building CacophonyKeyspaceV1_0"
$ECHO "
create keyspace CacophonyKeyspaceV1_0
    with strategy_options = {DC1:1}
    and placement_strategy = 'org.apache.cassandra.locator.NetworkTopologyStrategy';

use CacophonyKeyspaceV1_0;
create column family directory_server
    with column_type = 'Standard'
    with comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type'
    and key_validation_class = 'UTF8Type'
	and column_metadata = [
		{	column_name : 'heartbeat',
			validation_class : LongType}]; 

create column family cnode_list
    with column_type = 'Standard'
    with comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type'
    and key_validation_class = 'UTF8Type'
	and column_metadata = [
		{	column_name : 'heartbeat',
			validation_class : LongType},
		{	column_name : 'json_data',
			validation_class : UTF8Type},
			]; 
	" | $CASSANDRA_CLI -h$CASSANDRA_HOST_IP -p$CASSANDRA_PORT > /dev/null
    


