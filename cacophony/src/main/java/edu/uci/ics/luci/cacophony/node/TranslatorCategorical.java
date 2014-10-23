package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.minidev.json.JSONObject;

public class TranslatorCategorical implements Translator<Categorical<String>> {
	
	HashMap<String,Integer> map = new HashMap<String,Integer>();
	
	private Set<String> categoriesAsSet;
	private ArrayList<String> categoriesAsList;
	
	public void initialize(JSONObject jo) {
		
		/** First collect all the possible categories into a set of Strings**/ 
		categoriesAsSet = new TreeSet<String>();
		for(Object o: jo.values()){
			categoriesAsSet.add((String)o);
		}
		
		/** Convert to an array for indexing **/
		categoriesAsList = new ArrayList<String>(categoriesAsSet);
		
		
		/** Remap using int indices **/
		for(Entry<String, Object> e:jo.entrySet()){
			int index = categoriesAsList.indexOf((String)e.getValue());
			map.put(e.getKey(), index);
		}
	}
	
	public Categorical<String> translate(String s) {
		
		Integer index = map.get(s);
		if(index == null){
			return null;
		}
		
		String mappedValue = categoriesAsList.get(index);
		return new Categorical<String>(mappedValue, categoriesAsSet);
	}
}
