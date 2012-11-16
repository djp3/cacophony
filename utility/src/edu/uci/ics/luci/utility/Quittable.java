package edu.uci.ics.luci.utility;

/* This is used for the shutdown manager */
public interface Quittable {
	
	/** Consider making this synchronized*/
	void setQuitting(boolean quitting);

}
