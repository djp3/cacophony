#!/bin/bash

java -Djava.library.path=".:/home/don/kyotocabinet-java-install/lib" -Dfile.encoding=UTF-8 -Duser.timezone=GMT -Djava.net.preferIPv6Stack=true -Xmx1500M -jar cnode_pool.jar --config "cacophony/cnode_pool.cloud3.config.properties"

