package com.quub.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class MyShutdown extends Thread{
	
	private List<Quittable> q=null;

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MyShutdown.class);
		}
		return log;
	}
	

	public MyShutdown(List<Quittable> q ){
		super();
		this.q = new ArrayList<Quittable>();
		this.q.addAll(q);
	}
	
	public void add(Quittable q){
		this.q.add(q);
	}

	public void run() {
		getLog().info("MyShutdown shutting down (probably from signal)");
		if(q != null){
			for(Quittable x: q){
				if(x != null){
					x.setQuitting(true);
					synchronized(x){
						x.notifyAll();
					}
				}
			}
		}
		getLog().info("Done shutting down");
	}
}
