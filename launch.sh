#!/bin/bash

java -Dfile.encoding=UTF-8 -Duser.timezone=GMT -Djava.net.preferIPv6Stack=true -jar directory.jar --port 1776 --config "cacophony/cacophony.properties"

