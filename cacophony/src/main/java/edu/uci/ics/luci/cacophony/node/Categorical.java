package edu.uci.ics.luci.cacophony.node;

import java.util.Set;

public class Categorical<T> {
	
	private T category;
	
	private Set<T> possibleCategories;
	
	public Categorical(T category, Set<T> possibleCategories) {
		
		if (!possibleCategories.contains(category)) {
			throw new IllegalArgumentException("The given category is missing from the list of possible categories.");
		}
		
		this.category = category;
		this.possibleCategories = possibleCategories;
	}
	
	public T getCategory() {
		return category;
	}
	
	public Set<T> getPossibleCategories() {
		return possibleCategories;
	}
}
