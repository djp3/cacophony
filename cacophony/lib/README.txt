This is what is in this directory on a working distribution

	jars from other places:
	
		jtidy-r938.jar
			JTidy: http://jtidy.sourceforge.net
			 "JTidy is a Java port of HTML Tidy, a HTML syntax checker and
			 pretty printer. Like its non-Java cousin, JTidy can be used as a
			 tool for cleaning up malformed and faulty HTML. In addition, JTidy
			 provides a DOM parser for real-world HTML." 
			 
		luci-utility.jar
			https://github.com/djp3/luci-utility
			A collection of utilities to support LUCI projects

		p2p4java.jar
			The p2p4java implementation by the LUCI lab

			p2p4java.jar requires that you include:
				The source code for p2p4java has info on how to get them
					bcprov-ext-jdk15on-147.jar
					httptunnel-1.0.jar
					netty-3.2.3.Final.jar

		sqlite4java:
			The base library is included from maven, but you need to put an
			appropriate binary into the lib folder from here:
				https://code.google.com/p/sqlite4java/wiki/UsingWithMaven
			then change the pom.xml to reflect your local binary


			
	jars without a well described pedigree :
		
	maven dependencies (see pom.xml):
	
	Not currently necessary to build, but likely in the future
		JSAP:
			Java Simple Argument Parser

