/*
	Copyright 2007-2013
		University of California, Irvine (c/o Donald J. Patterson)
*/
/*
	This file is part of Cacophony

    Cacophony is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cacophony is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cacophony.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.sensors;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.datastructure.ListComparable;

public class ProcessMac extends Process{
	
    private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(ProcessMac.class);
		}
		return log;
	}
	
	// These polling rates are kept quick because:
	//   (a) the overhead is negligible, and
	//   (b) oftentimes changes are performed as part of a
	//       series of rapid actions with a PC, including
	//       app. switches.  We want this to be reflected
	//       accurately.
	private static final int INITIAL_INTERVAL = 1500;
	private static final int NORMAL_INTERVAL = 1500;
	private static final int END_INTERVAL = 50;
	
	static Object trackerThreadLock = new Object();
	static Thread trackerThread = null;
	private static String lastActiveProcess = null;
	private static boolean quitting = false;
	
	@Override
	protected String getNativeLibraryName() {
		return "process";
	}
	
	protected static boolean shouldIgnore(String processName) {
		// It makes no real sense to return false for a null process (or true, for that matter)
		if (processName == null)
			return false;
		String n = processName.trim();
		return n.equals("JavaApplicationStub") || n.equals("java");
	}
		
	@Override
	protected boolean initialize() {
		synchronized(trackerThreadLock){
			if(trackerThread == null){
				trackerThread = new Thread() {
					public void run() {
						try {
							while (!quitting) {
								String ap = sampleActiveProcess();
								if (!shouldIgnore(ap)){
									lastActiveProcess = ap;
								}
								synchronized(trackerThreadLock){
									if(!quitting){
										try {
											trackerThreadLock.wait(NORMAL_INTERVAL);
										} catch (InterruptedException e) {
										}
									}
								}
							}
						}
						catch (RuntimeException e) {
							getLog().log(Level.ERROR, "Failed to acquire the active process.", e);
						}
						synchronized(trackerThreadLock){
							if(quitting){
								trackerThreadLock.notifyAll();
							}
						}
					}
				};
				trackerThread.setDaemon(false); /*Force a clean shutdown */
				trackerThread.setName("ActiveProcessTrackerThread");
				trackerThread.start();
				
				while (lastActiveProcess == null){
					synchronized(trackerThreadLock){
						trackerThreadLock.notifyAll();
						try {
							if(!quitting){
								trackerThreadLock.wait(INITIAL_INTERVAL);
							}
						} catch (InterruptedException e) {
						}
					}
				}
			};
		}
		return true;
	}
	
	@Override
	protected void shutdown() {
		super.shutdown();
		
		synchronized(trackerThreadLock){
			quitting = true;
		}
		if (trackerThread != null){
			while(trackerThread.isAlive()){
				synchronized(trackerThreadLock){
					if (trackerThread.isAlive()) {
						trackerThreadLock.notifyAll();
						try {
							trackerThreadLock.wait(END_INTERVAL);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}
	
	private static native synchronized String sampleActiveProcess();
    private static native synchronized String[] sampleAllProcesses();


	@Override
	public String senseActiveProcess() {
    	return(lastActiveProcess);
	}


	@Override
	public ListComparable<String> senseAllProcesses() {
		String[] sampleAllProcesses = sampleAllProcesses();
		ListComparable<String> asList = new ListComparable<String>(new ArrayList<String>());
		asList.addAll(Arrays.asList(sampleAllProcesses));
    	return(asList);
	}
	
	
	public void main(String args[]){
		Process p = Process.getSensor();
		try {
			System.out.println(p.senseActiveProcess());
		} catch (Exception e) {
			getLog().log(Level.ERROR,"Failed to get active process",e);
		}
		p.setQuitting(true);
	}    
    
}
/*	ListComparable<String> al = new ListComparable<String>(new ArrayList<String>());
			BufferedReader out = null;
			BufferedReader err = null;
			try{
				Runtime runtime = Runtime.getRuntime();
				Process result = runtime.exec(new String[] { "ps", "-xc", "-o", "command" });

				String outputLine = null;
				String errorLine = null;
				Set<String> stdoutLines = new HashSet<String>();
				StringBuffer errorOutput = new StringBuffer();
				
				
				out = new BufferedReader(new InputStreamReader(result.getInputStream()));
				err = new BufferedReader(new InputStreamReader( result.getErrorStream()));

				while (((outputLine = out.readLine()) != null)
						|| ((errorLine = err.readLine()) != null)) {
					if (outputLine != null) {
						stdoutLines.add(outputLine);
					}
					if (errorLine != null) {
						errorOutput.append(errorLine + "\n");
					}
				}

				result.waitFor();

				if (result.exitValue() != 0) {
					getLog().warn("Failed to get running processes by running \"ps -xc -o command\":\n" + errorOutput.toString().trim());
					return null;
				} else {
					if (errorOutput.length() != 0) {
						getLog().warn(errorOutput.toString());
					}
				}

				al.addAll(stdoutLines);
				Collections.sort(al);
			} catch (IOException e) {
				//runtime.exec failed
				return null;
			} catch (InterruptedException e) {
				//result.wairFor failed
				return null;
			}
			finally{
				try{
					if(out != null){
						out.close();
					}
				} catch (IOException e) {
				}
				finally{
					out = null;
					try{
						if(err != null){
							err.close();
						}
					} catch (IOException e) {
					}
					finally{
						err = null;
					}
				}
			}

			return al; */
