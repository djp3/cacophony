#! /bin/bash

ECHO=/bin/echo
ID=/usr/bin/id
CASSANDRA_CLI=/home/don/external/cassandra/bin/cassandra-cli
CASSANDRA_HOST=cloud3.waitscout.com
CASSANDRA_PORT=9160

echo "
create keyspace CacophonyKeyspaceV1_0r
    with strategy_options = {DC1:1}
    and placement_strategy = 'org.apache.cassandra.locator.NetworkTopologyStrategy';

use CacophonyKeyspaceV1_0r;
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
	" | $CASSANDRA_CLI -h$CASSANDRA_HOST -p$CASSANDRA_PORT
    


