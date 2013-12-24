package edu.uci.ics.luci.cacophony.node;

public class CNode implements Runnable{
	
	CNodeConfiguration configuration;
	
	public CNodeConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(CNodeConfiguration configuration) {
		this.configuration = configuration;
	}

	public CNode(CNodeConfiguration configuration){
		this.configuration = configuration;
	}

	@Override
	public void run() {
//		Initialize
//			Weka classifer
//			
//		Start Polling
//			Jeff's code
//			Store the sample
//				sqlite
//			Run the machine learning classifier
//						

		
	}

}
