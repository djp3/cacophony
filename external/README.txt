This directory needs to contain various external projects such as

cassandra:
	git clone http://git-wip-us.apache.org/repos/asf/cassandra.git
	cd cassandra
	git checkout cassandra-1.1.8
	git checkout -b cassandra-1.1.8-local
	ant release

hector:
	git clone https://github.com/hector-client/hector.git
	cd hector
	git checkout hector-1.1-2
	git checkout -b hector-1.1-2-local
	mvn clean package 

