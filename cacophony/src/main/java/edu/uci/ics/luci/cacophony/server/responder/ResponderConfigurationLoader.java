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
		JSONArray incomingCNodes = null;
		try{
			incomingCNodes = (JSONArray)jo.get("c_nodes");
		} catch (ClassCastException e1) {
			appendError("Unable to make the \"c_nodes\" in the incoming request into a JSONArray\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		catch(RuntimeException e1){
			appendError("Unable to find the \"c_nodes\" parameter in the incoming JSONObject\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		
		if(incomingCNodes == null){
			appendError("Unable to make the \"c_nodes\" in the incoming request into a JSONArray\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n");
		}
		else{
			for(int i = 0; i< incomingCNodes.size(); i++){
				JSONObject incomingCNode = null;
				try{
					incomingCNode = (JSONObject)incomingCNodes.get(i);
				} catch (ClassCastException e1) {
					appendError("Unable to make the "+i+"th c_node in the incoming JSON into a JSONObject\n"+incomingCNodes.get(i).toString()+"\n"+e1);
					return;
				}
				
				String cnodeID_old = null;
				try{
					cnodeID_old = (String)incomingCNode.get("ID");
				} catch (ClassCastException e1) {
					appendError("Unable to make the c_node \"ID\" in the incoming CNodes into a String\n"+incomingCNode.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				catch(RuntimeException e1){
					appendError("Unable to find the c_node \"ID\" parameter in the incoming CNodes\n"+incomingCNode.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				
				JSONObject incomingConfiguration = null;
				try{
					incomingConfiguration = (JSONObject)incomingCNode.get("configuration");
				} catch (ClassCastException e1) {
					appendError("Unable to make the "+i+"th c_node configuration in the incoming JSON into a JSONObject\n"+incomingCNode.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				catch(RuntimeException e1){
					appendError("Unable to find the "+i+"th c_node \"configuration\" parameter in the incoming JSONObject\n"+incomingCNode.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
		
				if(cNodes.size() < parentServer.getMaxCNodes()){
					String cnodeID_new = UUID.randomUUID().toString();
					try{
						CNodeConfiguration config = new CNodeConfiguration(incomingConfiguration);
						ConfigurationsDAO.initializeDBIfNecessary();
						ConfigurationsDAO.store(cnodeID_new, config);
						CNode cNode = new CNode(config, cnodeID_new);
						cNodes.put(cnodeID_new, cNode);
						parentServer.launch(cnodeID_new);
						
						JSONObject response = new JSONObject();
						response.put("status", "OK");
						response.put("source_ID", cnodeID_old);
						response.put("clone_ID", cnodeID_new);
						appendResponse(response);
					}catch(RuntimeException e){
						JSONObject response = new JSONObject();
						response.put("status", "FAIL");
						response.put("source_ID", cnodeID_old);
						response.put("clone_ID", cnodeID_new);
						appendResponse(response);
					}catch (StorageException e) {
						JSONObject response = new JSONObject();
						response.put("status", "FAIL");
						response.put("source_ID", cnodeID_old);
						response.put("clone_ID", cnodeID_new);
						appendResponse(response);
					}
				}
				else{
					appendError("Maximum number of CNodes already running:"+parentServer.getMaxCNodes());
				}
			}
		}
	}

}
