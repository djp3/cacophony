package edu.uci.ics.luci.cacophony.node;

import java.util.Random;

import edu.uci.ics.luci.p2pinterface.P2PInterface;

public class CNode{
	
	private P2PInterface p2p;

	public CNode(){
		Random r = new Random();
		String providerName = "edu.ics.uci.luci."+r.nextLong();
		
		CustomerService cs = new CustomerService();
		
		p2p = new P2PInterface(providerName,cs);
		p2p.start();
	}

	public static void main(String[] args) {
		
		CNode cn = new CNode();
		
	}
		
		
		/*
		MLObject
			contstructor that receives the algorithm and initializes
					     receives a data store
			reclassify that rebuilds a model
			predict that give a new set of predictors returns a target
			threadsafe
			
		Datastore
			threadsafe
				smart about read/write locking
				(timestamp,p:weka,p:weka,p:weka,t:weka)
				
		Distillation Object
			constructor configuration
			parse(a bunch of text) and returns a weka data type 
			
		Polling object
			constructor receives a datastore object
				Distillation Object
				frequency stuff
				predictors
				when do you use cached values
			"start"
			"stop"
				
		CustomerService
			constructor
				configurationobject
				MLobject
				Datastore
				PollingObject
				
			requests from the p2pnetwork and answers them
			*/
			
		
//		[ { "name" : "temperature/cnn",
//			"machine_learning": {"classifier","com.weka.classifier.decisiontree"}
//			"predictors":[ "p2p://provider/temperature/msn","p2p://provider/weather/yahoo"],
//			"target":{ "url": "http://cnn.com",
//					   "format": "html"
//					   "path_expression": "/*/[class=temp]",
//					   "reg_ex": "temp=(.*)",
//					   "translator": { "classname": "edu.uci.ics.luci",
//									   "options": "a:thing"},
//			"polling" : { "policy":"on_change",
//						  "min_interval": "5000"}
//		 }
//		]
		
//		Get available to receive a configuration
//			Write some code to understand the configuration message
//			
//		Initialize
//			Weka classifer
//			
//		Start Polling
//			Jeff's code
//			Store the sample
//				sqlite
//			Run the machine learning classifier
//						
//		Available for querying by other people
//			last change value
//			predicted value
//			configuration file
//			give me your samples
//			predictors
//			serialized weka type

}
