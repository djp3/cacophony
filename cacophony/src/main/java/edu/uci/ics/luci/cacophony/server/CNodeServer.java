package edu.uci.ics.luci.cacophony.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.StorageException;
import edu.uci.ics.luci.cacophony.server.responder.CNodeServerResponder;
import edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities;
import edu.uci.ics.luci.cacophony.server.responder.ResponderConfiguration;
import edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoader;
import edu.uci.ics.luci.cacophony.server.responder.ResponderShutdown;
import edu.uci.ics.luci.cacophony.web.ConfigurationWebServer;
import edu.uci.ics.luci.p2pinterface.P2PInterface;
import edu.uci.ics.luci.utility.Quittable;

/*
 * Random notes from hack meeting with John and Jason
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
	

//Available for querying by other people
//last change value
//predicted value
//configuration file
//give me your samples
//predictors
//serialized weka type

public class CNodeServer implements Quittable{
	
	private Object isQuittingMonitor = new Object();
	private boolean isQuitting = false;
	private P2PInterface p2p  = null;
	/* A map from cNode name to cNode.  The key does not include the provider piece
	 * so a full URL is p2p://cNodeServer/A/B/C
	 * the map key in this case is /A/B/C
	 * It's explicitly a HashMap because we want the key to be able to be null in
	 * case the address is p2p://cNodeServer
	 */
	private Map<String,CNode> cNodes;
	private int MAX_CNODES = 10;
	private String p2pServerName;
	
	ExecutorService threadExecutor = null;

	public CNodeServer(){
		this(null);
	}

	public CNodeServer(String cNodeServerName){
		if(cNodeServerName == null){
			Random r = new Random();
			Long x;
			while((x = r.nextLong()) < 0);
			p2pServerName = "edu.ics.uci.luci.cacophony.server.test."+x;
		}
		else{
			p2pServerName = cNodeServerName;
		}
		
		cNodes = Collections.synchronizedMap(new HashMap<String, CNode>());
		
		threadExecutor = Executors.newCachedThreadPool();
		
		/* Set up the responder on the p2p network*/
		CustomerService cs;
		{
			Map<String,CNodeServerResponder> handlers = Collections.synchronizedMap( new HashMap<String,CNodeServerResponder>(MAX_CNODES));
		
			ResponderCapabilities responderCapabilities = new ResponderCapabilities(handlers,MAX_CNODES);
			handlers.put("null", responderCapabilities);
			handlers.put("capabilities", responderCapabilities);
		
			handlers.put("load_configurations", new ResponderConfigurationLoader(this));
		
			handlers.put("configuration", new ResponderConfiguration());
			handlers.put("shutdown", new ResponderShutdown(this));
		
			cs = new CustomerService(handlers,cNodes);
		}
		
		/* Turn on the p2p interface */
		{
			p2p = new P2PInterface(p2pServerName,cs);
			cs.setP2P(p2p);
		}
		
		launchExistingCNodes();
	}
	
	private void launchExistingCNodes() {
		Map<String, CNodeConfiguration> existingCNodes;
		try {
			ConfigurationsDAO.initializeDBIfNecessary();
			existingCNodes = ConfigurationsDAO.retrieve();
			for (String cnodeID : existingCNodes.keySet()) {
				CNodeConfiguration config = existingCNodes.get(cnodeID);
				cNodes.put(cnodeID, new CNode(config, cnodeID));
				launch(cnodeID);
			}
		} catch (StorageException e) {
			// TODO: log error
			e.printStackTrace();
			stop();
			return;
		}
	}
	
	public void start(){
		p2p.start();
	}
	
	public void stop(){
		setQuitting(true);
	}


	public String getServerName() {
		return p2pServerName;
	}
	
	public Map<String,CNode> getCNodes(){
		return cNodes;
	}
	
	public int getMaxCNodes(){
		return MAX_CNODES;
	}
	
	
	public void launch(String cnodeID){
		CNode cNode = cNodes.get(cnodeID);
		if(cNode != null){
			threadExecutor.execute(cNode);
		}
	}



	public void setQuitting(boolean quitting) {
		synchronized(getQuittingMonitor()){
			if((!isQuitting) &&(quitting)){
				isQuitting = true;
				if(p2p != null){
					p2p.stop();
				}
				getQuittingMonitor().notifyAll();
			}
		}
	}

	public boolean isQuitting() {
		synchronized(getQuittingMonitor()){
			return this.isQuitting;
		}
	}
	
	public Object getQuittingMonitor(){
		return isQuittingMonitor;
	}
		
		
	
	
	public static void main(String[] args) {
		CNodeServer cn = new CNodeServer();
		ConfigurationWebServer.launch(cn, "localhost");
		
		/*Wait for the server to shutdown to shutdown */
		synchronized(cn.getQuittingMonitor()){
			while(!cn.isQuitting()){
				try {
					cn.getQuittingMonitor().wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
