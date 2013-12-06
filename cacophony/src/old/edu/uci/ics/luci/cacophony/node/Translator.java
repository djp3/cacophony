package edu.uci.ics.luci.cacophony.node;

/**
 *	This is an interface for classes which translate a String from the web into something that we can
 *  build a model from. 
 * @author djp3
 *
 * @param <T> This is the type that the Translator translates into.
 */
public interface Translator<T> {
	
	/**
	 * 
	 * @param x
	 * @return true if x can be converted to a T
	 */
	boolean translatable(String x);
	
	/**
	 * 
	 * @param x
	 * @return the value of T that corresponds to x, unless translatable(x) is false in which case return null
	 */
	T translation(String x);
}
