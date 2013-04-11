package edu.uci.ics.luci.cacophony.node;

public class TranslatorIdentity implements Translator<String> {

	@Override
	public boolean translatable(String x) {
		if(x != null){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"NP_BOOLEAN_RETURN_NULL"}, justification="It makes sense to be able to return null")
	public String translation(String x) {
		if(x == null){
			return null;
		}
		else{
			return x;
		}
	}
}
