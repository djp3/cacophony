#!/bin/bash
git stash -q --keep-index

VERSION=`git log -1 --pretty=format:%H`
#echo $VERSION

cat > /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/cacophony/src/edu/uci/ics/luci/cacophony/GitRevision.java << EOF
package edu.uci.ics.luci.cacophony;

public class GitRevision {
	public static final String SYSTEM_REVISION = "$VERSION";
}
EOF

git stash pop -q

