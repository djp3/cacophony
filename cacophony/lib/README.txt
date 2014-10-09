This is what is in this directory on a working distribution

	jars from other places:
		json-smart-1.1.jar:
			A Java JSON parser
	
		jtidy-r938.jar
			JTidy: http://jtidy.sourceforge.net
			 "JTidy is a Java port of HTML Tidy, a HTML syntax checker and
			 pretty printer. Like its non-Java cousin, JTidy can be used as a
			 tool for cleaning up malformed and faulty HTML. In addition, JTidy
			 provides a DOM parser for real-world HTML." 
			 
		json-path-0.9.0.jar
			json-path: https://code.google.com/p/json-path/
			"JsonPath is to JSON what XPATH is to XML, a simple way to extract parts of a given document. JsonPath is available in many programming languages such as Javascript, Python and PHP. Now also in Java!"

			NOTE: json-path-0.9.0.jar has dependencies as well. These can be found in dependencies zip at the above Google code site's downloads page.
				currently that includes:
					json-smart-1.2.jar
					slf4j-api-1.7.5.jar


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

		sqlite4java libraries:
			http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.axet.litedb%22
				libsqlite4java-osx.jnilib
				sqlite4java-docs.zip
				sqlite4java.jar

				
		weka.jar
			The Weka machine learning library
		
			
	jars without a well described pedigree:
		commons-configuration-1.9.jar
		commons-configuration-1.9-sources.jar
		commons-lang-2.6.jar
		commons-lang-2.6-sources.jar
		log4j-api-2.0-rc2.jar
		log4j-core-2.0-rc2.jar
		annotations.jar (From FindBugs)


Not currently necessary to build, but likely in the future
	JSAP-2.1.jar
		Java Simple Argument Parser
		http://sourceforge.net/projects/jsap/

