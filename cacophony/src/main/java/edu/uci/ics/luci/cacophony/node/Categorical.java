package edu.uci.ics.luci.cacophony.node;

import java.util.List;

public class Categorical<T> {
	private T category;
	private List<T> possibleCategories;
	public Categorical(T category, List<T> possibleCategories) {
		boolean found = false;
		for (T possibleCategory : possibleCategories) {
			if (possibleCategory.equals(category)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("The given category is missing from the list of possible categories.");
		}
		
		this.category = category;
		this.possibleCategories = possibleCategories;
	}
	
	public T getCategory() {
		return category;
	}
	
	public List<T> getPossibleCategories() {
		return possibleCategories;
	}
}
