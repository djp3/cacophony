package com.quub.util;

/* This is used for the shutdown manager */
public interface Quittable {
	
	/** Consider making this synchronized*/
	void setQuitting(boolean quitting);

}
