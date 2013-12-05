package edu.uci.ics.luci.cacophony.sensors;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.log4j.Logger;

public class ClassLoaderNative {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(ClassLoaderNative.class);
		}
		return log;
	}

    private static URL[] urls;
    
    static{
    	File file = new File("build/native/");
    	try {
			URL url = file.toURI().toURL();
			urls = new URL[]{url};
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
	private static ClassLoader cl;
	static{
		cl = AccessController.doPrivileged(
				new PrivilegedAction<ClassLoader>(){
					public ClassLoader run() {
						return new URLClassLoader(urls);
					};
				}
		);
	}
	private static final String[] extensions = new String[] { "dylib","jnilib" };
	private static final String prefix = "lib";
		
	public static void loadClasspath(String lib) {
		String path = null;
		
		for (String extension : extensions) {
			URL url = cl.getResource(new StringBuilder(prefix).append(lib).append(".").append(extension).toString());	
			if (url != null) {
				path = url.getFile();
				break;
			}
		}		
		if (path == null) 
			throw new UnsatisfiedLinkError(new StringBuilder(lib).append(" not found in class path").toString());
		
		getLog().debug("This is what I'm trying "+path);
		try{
			System.load(path);
		}
		catch(RuntimeException e){
			getLog().error("Couldn't load "+lib+", "+e);
		}
	}
}
