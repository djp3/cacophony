package edu.uci.ics.luci.cacophony.node;

public class WekaAttributeTypeValuePair {
	private int mWekaAttributeType;
	private Object mValue;
	
	public WekaAttributeTypeValuePair(int wekaAttributeType, Object value) {
		mWekaAttributeType = wekaAttributeType;
		mValue = value;
	}
	
	public int getWekaAttributeType() {
		return mWekaAttributeType;
	}
	
	public Object getValue() {
		return mValue;
	}
}
