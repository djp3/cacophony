package edu.uci.ics.luci.cacophony.server.responder;

import java.util.Map;
import java.util.UUID;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodeConfiguration;
import edu.uci.ics.luci.cacophony.node.StorageException;
import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.cacophony.server.ConfigurationsDAO;

public class ResponderConfigurationLoader extends CNodeServerResponder {

	private CNodeServer parentServer;

	/**
	 * The CNodeServer is necessary because the incoming configurations need to be launched.
	 * @param cns
	 */
	public ResponderConfigurationLoader(CNodeServer cns) {
		if(cns == null){
			throw new IllegalArgumentException("Can't initialize with a null server");
		}
		this.parentServer = cns;
	}
	
	public CNodeServer getParentServer(){
		return parentServer;
	}
	
	@Override
	public void handle(JSONObject jo, Map<String, CNode> cNodes) {
		
		JSONArray incomingConfigurations = null;
		try{
			incomingConfigurations = (JSONArray) jo.get("configurations");
		} catch (ClassCastException e1) {
			appendError("Unable to make the \"configurations\" in the incoming request into a JSONArray\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		catch(RuntimeException e1){
			appendError("Unable to find the \"configurations\" parameter in the incoming JSONObject\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		
		if(incomingConfigurations == null){
			appendError("Unable to make the \"configurations\" in the incoming request into a JSONArray\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n");
		}
		else{
			for(int i = 0; i< incomingConfigurations.size(); i++){
				JSONObject incomingConfiguration = null;
				try{
					incomingConfiguration = (JSONObject) incomingConfigurations.get(i);
				} catch (ClassCastException e1) {
					appendError("Unable to make the "+i+"th configuration in the incoming JSON into a JSONObject\n"+incomingConfigurations.get(i).toString()+"\n"+e1);
					return;
				}
				
				String cNodeName = null;
				try{
					cNodeName = (String) incomingConfiguration.get("c_node_name");
				} catch (ClassCastException e1) {
					appendError("Unable to make the \"c_node_name\" in the incoming configuration into a String\n"+incomingConfiguration.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				catch(RuntimeException e1){
					appendError("Unable to find the \"c_node_name\" parameter in the incoming configuration\n"+incomingConfiguration.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
		
				if(cNodes.containsKey(cNodeName)){
					appendError("c_node_name:\""+cNodeName+"\" is already present in this server");
					return;
				}
				else{
					if(cNodes.size() < parentServer.getMaxCNodes()){
						try{
							CNodeConfiguration config = new CNodeConfiguration(incomingConfiguration);
							String cnodeID = UUID.randomUUID().toString();
							
							ConfigurationsDAO.initializeDBIfNecessary();
							ConfigurationsDAO.store(cnodeID, config);
							CNode cNode = new CNode(config, cnodeID);
							cNodes.put(cNodeName, cNode);
							parentServer.launch(cNodeName);
							
							appendResponse(cNodeName+":OK");
						}catch(RuntimeException e){
							appendResponse(cNodeName+":FAIL");
						}catch (StorageException e) {
							appendResponse(cNodeName+":FAIL");
						}
					}
					else{
						appendError("Maximum number of c_nodes already running:"+parentServer.getMaxCNodes());
					}
				}
			}
		}
	}

}
