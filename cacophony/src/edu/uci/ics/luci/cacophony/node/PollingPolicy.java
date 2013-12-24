package edu.uci.ics.luci.cacophony.node;

public enum PollingPolicy {
	ON_CHANGE;
	
	static PollingPolicy fromString(String x){
		String y = x.toUpperCase().trim();
		if(y.equals("ON_CHANGE")){
			return ON_CHANGE;
		}
		else{
			throw new IllegalArgumentException("Can't interpret a polling policy of type:"+x);
		}
	}
	
	static String toString(PollingPolicy p){
		switch(p){
			case ON_CHANGE: return("ON_CHANGE");
			default:return null;
		}
	}

}
