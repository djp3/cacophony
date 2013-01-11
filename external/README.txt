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

kyotocabinet-1.2.76:
	Download the core package from http://fallabs.com/kyotocabinet/
	I used the configure command to install it in a sister directory called kyoto-install (--prefix)
		make
		make install

kyotocabinet-java-1.24
	Download the core package from http://fallabs.com/kyotocabinet/
	to configure I use:
  		./configure --prefix="/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-java-install" CPPFLAGS="-I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include  -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers" PKG_CONFIG_PATH="/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib/pkgconfig"

	Then I editted the Makefile to change < from ---- > to:
	< JAVAC = /bin/javac
	---
	> JAVAC = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javac

	< JAR = /bin/jar
	< JAVAH = /bin/javah
	< JAVADOC = /bin/javadoc
		---
	> JAR = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/jar
	> JAVAH = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javah
	> JAVADOC = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javadoc

	< JAVARUN = /bin/java
	---
	> JAVARUN = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java

	< CPPFLAGS = -I. -I$(INCLUDEDIR) -I/Users/djp3/include -I/usr/local/include -DNDEBUG -I/include -I/include/mac -I/Headers -I/Headers/mac -I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include
	---
	> CPPFLAGS = -I. -I$(INCLUDEDIR) -I/Users/djp3/include -I/usr/local/include -DNDEBUG -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/include -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/include/mac -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/Headers -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/Headers/mac -I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include
	

	to build:
		make -j8 all CPPFLAGS="-I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers" PKG_CONFIG_PATH="/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib/pkgconfig"

	to install in the sister directory specified in configure
		make install CPPFLAGS="-I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include -I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers" PKG_CONFIG_PATH="/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib/pkgconfig"

	that created a jar and some native binaries which I linked in the cacophony/lib directory
	then within Eclipse I added the jar and specified that cacophony/lib was where the native libraries were


