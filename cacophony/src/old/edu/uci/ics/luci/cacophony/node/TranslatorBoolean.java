package edu.uci.ics.luci.cacophony.node;

public class TranslatorBoolean implements Translator<Boolean> {

	@Override
	public boolean translatable(String x) {
		if((x != null) && ((x.toLowerCase().equals("false")) || (x.toLowerCase().equals("true")))){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"NP_BOOLEAN_RETURN_NULL"}, justification="It makes sense to be able to return null")
	public Boolean translation(String x) {
		if(x == null){
			return null;
		}
		else{
			if(x.toLowerCase().equals("false")){
				return false;
			}
			else if(x.toLowerCase().equals("true")){
				return true;
			}
			else{
				return null;
			}
		}
	}
}
