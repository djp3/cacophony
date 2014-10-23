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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((categoriesAsList == null) ? 0 : categoriesAsList.hashCode());
		result = prime * result
				+ ((categoriesAsSet == null) ? 0 : categoriesAsSet.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TranslatorCategorical))
			return false;
		TranslatorCategorical other = (TranslatorCategorical) obj;
		if (categoriesAsList == null) {
			if (other.categoriesAsList != null)
				return false;
		} else if (!categoriesAsList.equals(other.categoriesAsList))
			return false;
		if (categoriesAsSet == null) {
			if (other.categoriesAsSet != null)
				return false;
		} else if (!categoriesAsSet.equals(other.categoriesAsSet))
			return false;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

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
