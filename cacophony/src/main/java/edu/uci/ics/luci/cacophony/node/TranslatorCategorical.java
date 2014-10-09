package edu.uci.ics.luci.cacophony.node;

import java.util.Arrays;
import java.util.List;

import net.minidev.json.JSONObject;

public class TranslatorCategorical implements Translator<Categorical<String>> {
	
	public void initialize(JSONObject jo) {
	}
	
	public Categorical<String> translate(String s) {
		if (s == null) {
			return null;
		}
		
		List<String> possibleCategories = Arrays.asList("red", "green", "blue"); // TODO: figure out how to get list of possible categories automatically
		Categorical<String> categorical = new Categorical<String>(s, possibleCategories);
		return categorical;
	}
}
