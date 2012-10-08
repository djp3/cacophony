package edu.uci.ics.luci.cacophony.model;

import java.io.Serializable;

import com.quub.util.Quittable;

public abstract class ModelStorage<K extends Serializable, V extends Serializable> implements Quittable{

	
	/**
	 * 
	 * @param deleteExisting, true if an existing database should be deleted before opening
	 * @param isTesting
	 * @return
	 */
	public abstract boolean open(boolean deleteExisting,boolean isTesting);
	
	
	
	/**
	 *	A key may be null.  That is considered a unique key.
	 *  A value may be null.
	 * @param key
	 * @param value
	 * @return true if it worked.
	 */
	public abstract boolean set(K key, V value);
	
	/**
	 * This method helps to distinguish a record whose value is null and a record which does not exist.
	 * @param key
	 * @return true if a record exists for key
	 */
	public abstract boolean contains(K key);
	
	public abstract V get(K key);
	
	/**
	 * 
	 * @param key
	 * @return true if a record was removed, false if it wasn't present to remove
	 */
	public abstract boolean remove(K key);
	
	 /**
	   * Iterate to accept a visitor for each record.
	   * @param visitor a visitor object which implements the ModelStorageVisitor interface.
	   * @param writable true for writable operation, or false for read-only operation.
	   * @return true on success, or false on failure.
	   * @note The whole iteration is performed atomically and other threads are blocked.  To avoid
	   * deadlock, any explicit database operation must not be performed in this method.
	   */
	public abstract boolean iterate(ModelStorageVisitor<K,V> visitor, boolean writable);
	
	public abstract Error error();
}
