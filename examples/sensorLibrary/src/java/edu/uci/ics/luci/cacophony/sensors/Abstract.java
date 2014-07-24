package edu.uci.ics.luci.cacophony.sensors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uci.ics.luci.cacophony.sensors.Environment.OSTypes;
import edu.uci.ics.luci.utility.Quittable;

/**
 * The basic idea for the class structure is that Abstract is extended to sense a particular thing, for example the
 * power source.
 * Abstract -> PowerSource
 * 
 * PowerSource is OS agnostic and wraps the functionality of the OS specific needs.
 * Getting the actual sensor is done by calling PowerSource.getSensor() which returns an OS specific variant
 * which is extended from PowerSource
 * 
 * PowerSource is then extended to implement the specific OS interfaces.  
 *     PowerSource (defines sensor specific methods) -> 
 *     PowerSourceMac (implements the sensor specific methods, possibly defining native methods)
 *     PowerSourceWindows (implements the sensor specific methods, possibly defining native methods)
 *     
 * @author djp3
 *
 */
public abstract class Abstract implements Quittable{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = LogManager.getLogger(Abstract.class);
		}
		return log;
	}
	
	protected static transient volatile OSTypes os = null;
	static{
		os = Environment.getInstance().getOSType();
	}
	
	/*********************************************************************************/
	/** These should be managed by OS agnostic classes like "PowerSource"
	/**
	 * "Native Load Lock" is an object that is synchronized on to prevent simultaneous native library loading
	 * by multiple instantiations of the same sensor
	 * @return
	 */
	protected abstract Object getNativeLoadLock();
	
	/**
	 * Stores whether or not a native load has been successfully accomplished.  
	 * @return
	 */
	protected abstract void setNativeLoadComplete(boolean x);
	protected abstract boolean getNativeLoadComplete();
	
	/*********************************************************************************/
	/** These should be managed by OS specific classes like "PowerSourceMac"
	
	/**
	 * 
	 * @return the name of the jni that supports the functionality of the implementation
	 * for example "processsensor_mac" for libprocesssensor_mac.jnilib"
	 * If the result is null, then no native library is loaded
	 */
	protected abstract String getNativeLibraryName();
	
	protected abstract Object getSensingAvailableLock(); 
	protected abstract Boolean getSensingAvailable();
	protected abstract void setSensingAvailable(Boolean sensingAvailable);
	/*********************************************************************************/

	public Abstract(){
		super();
		nativeLoad();
		if(!isInitialized()){
			if(!sensingAvailable()){
				getLog().fatal("Something has gone wrong with sensor initialization");
				throw new RuntimeException("Something has gone wrong with sensor initialization");
			}
		}
		initialized = isInitialized();
	}
	
	void nativeLoad(){
		synchronized(getNativeLoadLock()){
			if(!getNativeLoadComplete()){
				try{
					String name = getNativeLibraryName();
					if(name != null){
						ClassLoaderNative.loadClasspath(name);
					}
					setNativeLoadComplete(true);
				}
				catch (RuntimeException e){
					e.printStackTrace();
					throw e;
				}
			}
		}
	}
	
	
    private Boolean initialized = false;
	private Object quittingLock = new Object();
    private Boolean quitting = false;
    
    /**
     * 
     * @return an Object that is not null if sensing is working. It should also be what the class is supposed to sense.
     */
    public abstract Object sense();
    
	/**
	 *  Return whether or not things are initialized. 
	 */
	boolean isInitialized(){
		return initialized;
	}
	
	/**
	 *  Execute any functionality that needs to be done before calls to this class begins.
	 *  Return the result.
	 */
	abstract protected boolean initialize();
	
	
	/**
	 * Execute any functionality that needs to be done for a clean shutdown
	 * Shutting down should be accomplished by calling setQuitting.
	 * setQuitting should only be called once per item retrieved from getSensor
	 */
	abstract protected void shutdown();
	

	public void setQuitting(boolean quitting) {
		synchronized(quittingLock){
			if(!this.quitting){
				if(quitting){
					synchronized(getSensingAvailableLock()){
						this.quitting = true;
						try{
							shutdown();
						}
						catch(RuntimeException e){
							getLog().error("shutdown caused an exception: "+e);
						}
						initialized = false;
						setSensingAvailable(null);
					}
				}
				else{
					getLog().error("Can't unquit");
				}
			}
			else{
				if(quitting){
					getLog().info("Multiple quits called");
				}
				else{
					getLog().debug("Thanks for asserting that we really aren't quitting");
				}
			}
		}
	}
	
	public boolean isQuitting(){
		synchronized(quittingLock){
			return quitting;
		}
	}

	
	
	
	
	public boolean sensingAvailable() {
		synchronized(getSensingAvailableLock()){
			if(getSensingAvailable() == null){
				try {
					if(initialize()){
						initialized = true;
						setSensingAvailable(true);
					} 
					else{
						initialized = true;
						setSensingAvailable(false);
					}
				} catch (UnsatisfiedLinkError e) {
					setSensingAvailable(false);
					getLog().log(Level.WARN,"Failed to start the active process sensor.  Native module didn't work.", e);
				}
			}
			return(getSensingAvailable());
		}
	}
}

