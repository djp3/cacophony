# Makefile for Kyoto Cabinet for Java



#================================================================
# Setting Variables
#================================================================


# Generic settings
SHELL = /bin/sh

# Package information
PACKAGE = kyotocabinet-java
VERSION = 1.24
PACKAGEDIR = $(PACKAGE)-$(VERSION)
PACKAGETGZ = $(PACKAGE)-$(VERSION).tar.gz
LIBVER = 1
LIBREV = 1

# Targets
JARFILES = kyotocabinet.jar
JAVAFILES = Loader.java Utility.java Error.java Visitor.java FileProcessor.java Cursor.java DB.java MapReduce.java ValueIterator.java Test.java
LIBRARYFILES =  libjkyotocabinet.1.1.0.dylib libjkyotocabinet.1.dylib libjkyotocabinet.dylib libjkyotocabinet.jnilib
LIBOBJFILES = kyotocabinet.o

# Install destinations
prefix = /Users/djp3/Development/Mac/EclipseWorkspaceCacophony/kyotocabinet-java-install
exec_prefix = ${prefix}
datarootdir = ${prefix}/share
LIBDIR = ${exec_prefix}/lib
DESTDIR =

# Building configuration
JAVAC = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javac
JAVACFLAGS = -d .
JAR = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/jar
JAVAH = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javah
JAVADOC = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javadoc
JAVADOCFLAGS = -locale en -tag note:a:Note: -nodeprecated -nohelp -quiet -noqualifier all
JAVARUN = /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java
JAVARUNFLAGS = -cp kyotocabinet.jar -Djava.library.path=.:/Users/djp3/lib:/usr/local/lib:/usr/local/lib:/sw/lib:/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib
CXX = g++
CPPFLAGS = -I. -I$(INCLUDEDIR) -I/Users/djp3/include -I/usr/local/include -DNDEBUG -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/include -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/include/mac -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/Headers -I/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/Headers/mac -I/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/include
CXXFLAGS = -m64 -Wall -fPIC -fsigned-char -O2
LDFLAGS = -L. -L$(LIBDIR) -L/Users/djp3/lib -L/usr/local/lib -L/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib
LIBS = -lkyotocabinet -lz -lstdc++ -lpthread -lm -lc 
LDENV = LD_RUN_PATH=/lib:/usr/lib:$(LIBDIR):$(HOME)/lib:/usr/local/lib:$(LIBDIR):/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib:.
RUNENV = DYLD_LIBRARY_PATH=.:/lib:/usr/lib:$(LIBDIR):$(HOME)/lib:/usr/local/lib:$(LIBDIR):/Users/djp3/Development/Mac/EclipseWorkspaceCacophony/external/kyotocabinet-install/lib



#================================================================
# Suffix rules
#================================================================


.SUFFIXES :
.SUFFIXES : .cc .o

.cc.o :
	$(CXX) -c $(CPPFLAGS) $(CXXFLAGS) $<



#================================================================
# Actions
#================================================================


all : $(JARFILES) $(LIBRARYFILES)
	@printf '\n'
	@printf '#================================================================\n'
	@printf '# Ready to install.\n'
	@printf '#================================================================\n'


clean :
	rm -rf $(JARFILES) $(LIBRARYFILES) $(LIBOBJFILES) \
	  *.o a.out *.class check.in check.out gmon.out *.vlog \
	  casket* *.kch *.kct *.kcd *.kcf *.wal *.tmpkc* *.kcss *.log *~ hoge moge tako ika


untabify :
	ls *.cc *.h *.java | while read name ; \
	  do \
	    sed -e 's/\t/        /g' -e 's/ *$$//' $$name > $$name~; \
	    [ -f $$name~ ] && mv -f $$name~ $$name ; \
	  done


install :
	mkdir -p $(DESTDIR)$(LIBDIR)
	cp -Rf $(JARFILES) $(DESTDIR)$(LIBDIR)
	cp -Rf $(LIBRARYFILES) $(DESTDIR)$(LIBDIR)
	@printf '\n'
	@printf '#================================================================\n'
	@printf '# Thanks for using Kyoto Cabinet for Java.\n'
	@printf '#================================================================\n'


uninstall :
	cd $(DESTDIR)$(LIBDIR) && rm -f $(JARFILES)
	cd $(DESTDIR)$(LIBDIR) && rm -f $(LIBRARYFILES)


dist :
	$(MAKE) untabify
	$(MAKE) distclean
	cd .. && tar cvf - $(PACKAGEDIR) | gzip -c > $(PACKAGETGZ)
	sync ; sync


distclean : clean
	cd example && $(MAKE) clean
	rm -rf Makefile config.cache config.log config.status config.tmp autom4te.cache


head : kyotocabinet.jar
	rm -rf kyotocabinet_*.h
	CLASSPATH=kyotocabinet.jar $(JAVAH) -jni kyotocabinet.Utility kyotocabinet.Error \
	  kyotocabinet.Cursor kyotocabinet.DB kyotocabinet.MapReduce kyotocabinet.ValueIterator


check :
	$(MAKE) DBNAME=":" RNUM="10000" check-each
	$(MAKE) DBNAME="*" RNUM="10000" check-each
	$(MAKE) DBNAME="%" RNUM="10000" check-each
	$(MAKE) DBNAME="casket.kch" RNUM="10000" check-each
	$(MAKE) DBNAME="casket.kct" RNUM="10000" check-each
	$(MAKE) DBNAME="casket.kcd" RNUM="1000" check-each
	$(MAKE) DBNAME="casket.kcf" RNUM="10000" check-each
	@printf '\n'
	@printf '#================================================================\n'
	@printf '# Checking completed.\n'
	@printf '#================================================================\n'


check-each :
	rm -rf casket*
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -rnd "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -etc "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -rnd -etc "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -th 4 "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -th 4 -rnd "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -th 4 -etc "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test order \
	  -th 4 -rnd -etc "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test wicked \
	  "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test wicked \
	  -it 4 "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test wicked \
	  -th 4 "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test wicked \
	  -th 4 -it 4 "$(DBNAME)" "$(RNUM)"
	$(RUNENV) $(JAVARUN) $(JAVARUNFLAGS) kyotocabinet.Test misc \
	  "$(DBNAME)"
	rm -rf casket*


check-forever :
	while true ; \
	  do \
	    $(MAKE) check || break ; \
	  done


doc :
	$(MAKE) docclean
	mkdir -p doc
	mkdir -p doctmp
	\ls *.java | while read file ; \
	  do \
	    sed -e 's/public static class X/private static class X/' $$file > doctmp/$$file ; \
	  done
	$(JAVADOC) $(JAVADOCFLAGS) -windowtitle kyotocabinet -overview overview.html \
	  -d doc doctmp/*.java
	rm -rf doctmp


docclean :
	rm -rf doc doctmp


.PHONY : all clean install casket check doc



#================================================================
# Building binaries
#================================================================


kyotocabinet.jar : $(JAVAFILES)
	$(JAVAC) $(JAVACFLAGS) $(JAVAFILES)
	$(JAR) cvf $@ kyotocabinet/*.class
	rm -rf kyotocabinet


libjkyotocabinet.so.$(LIBVER).$(LIBREV).0 : $(LIBOBJFILES)
	$(CXX) $(CXXFLAGS) -shared -Wl,-soname,libjkyotocabinet.so.$(LIBVER) -o $@ \
	  $(LIBOBJFILES) $(LDFLAGS) $(LIBS)


libjkyotocabinet.so.$(LIBVER) : libjkyotocabinet.so.$(LIBVER).$(LIBREV).0
	ln -f -s libjkyotocabinet.so.$(LIBVER).$(LIBREV).0 $@


libjkyotocabinet.so : libjkyotocabinet.so.$(LIBVER).$(LIBREV).0
	ln -f -s libjkyotocabinet.so.$(LIBVER).$(LIBREV).0 $@


libjkyotocabinet.$(LIBVER).$(LIBREV).0.dylib : $(LIBOBJFILES)
	$(CXX) $(CXXFLAGS) -dynamiclib -o $@ \
	  -install_name $(LIBDIR)/libjkyotocabinet.$(LIBVER).dylib \
	  -current_version $(LIBVER).$(LIBREV).0 -compatibility_version $(LIBVER) \
	  $(LIBOBJFILES) $(LDFLAGS) $(LIBS)


libjkyotocabinet.$(LIBVER).dylib : libjkyotocabinet.$(LIBVER).$(LIBREV).0.dylib
	ln -f -s libjkyotocabinet.$(LIBVER).$(LIBREV).0.dylib $@


libjkyotocabinet.dylib : libjkyotocabinet.$(LIBVER).$(LIBREV).0.dylib
	ln -f -s libjkyotocabinet.$(LIBVER).$(LIBREV).0.dylib $@


libjkyotocabinet.jnilib : libjkyotocabinet.dylib
	ln -f -s libjkyotocabinet.dylib $@


kyotocabinet.o : kyotocabinet_Utility.h kyotocabinet_Error.h \
  kyotocabinet_Cursor.h kyotocabinet_DB.h kyotocabinet_MapReduce.h kyotocabinet_ValueIterator.h



# END OF FILE
