#!/bin/bash
git stash -q --keep-index

VERSION=`git log -1 --pretty=format:%H`
#echo $VERSION

cat > ./cacophony/src/edu/uci/ics/luci/cacophony/GitRevision.java << EOF
package edu.uci.ics.luci.cacophony;

public class GitRevision {
	public static final String SYSTEM_REVISION = "$VERSION";
}
EOF

git log -n 1 --pretty=tformat:%s%n%n%b | git commit --file - --amend --no-verify ./cacophony/src/edu/uci/ics/luci/cacophony/GitRevision.java 

git stash pop -q
