#!/bin/bash

java -Dfile.encoding=UTF-8 -Duser.timezone=GMT -Djava.net.preferIPv6Stack=true -jar directory.jar --url.external "cloud2.waitscout.com" --server.guid "cloud2" --port 1776 --config "cacophony/cacophony.properties"

