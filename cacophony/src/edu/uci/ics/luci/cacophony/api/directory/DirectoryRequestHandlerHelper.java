package edu.uci.ics.luci.cacophony.api.directory;

import java.util.Map;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.directory.Directory;

public abstract class DirectoryRequestHandlerHelper extends CacophonyRequestHandlerHelper {
	
	private Directory d;

	protected DirectoryRequestHandlerHelper(Directory d){
		setDirectory(d);
	}
	
	protected Directory getDirectory(){
		return d;
	}
	
	protected void setDirectory(Directory d){
		this.d = d;
	}
	
	protected String testNamespaceEquality(String trueNamespace, String namespace) {
		String noerror = null;
		String error = "Namespace is: \""+trueNamespace+"\". REST call requested: \""+namespace+"\".";
		if(trueNamespace != null){
			if(namespace != null){
				if(trueNamespace.equals(namespace)){
					return noerror;
				}
				else{
					return error;
				}
			}
			else{
				return error;
			}
		}
		else{
			if(namespace != null){
				return error;
			}
			else{
				return noerror;
			}
		}
	}
	
	protected String namespaceOK(Map<String, String> parameters)
	{
		String trueNamespace = getDirectory().getDirectoryNamespace();
		String namespace = parameters.get("namespace");
		return testNamespaceEquality(trueNamespace, namespace);
	}
	

	protected String directoryAPIOK(Map<String,String> parameters) {
		String noerror = null;
		String reason;
		if((reason = versionOK(parameters)) != null){
			return reason;
		}
		
		if((reason = namespaceOK(parameters)) != null){
			return reason;
		}
		
		return noerror;
	}
}
