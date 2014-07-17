package edu.uci.ics.luci.cacophony.node;

public class StorageException extends Exception {
	private static final long serialVersionUID = -3611799932297500476L;

	public StorageException(String message) {
    super(message);
	}
	
	public StorageException(String message, Throwable t) {
    super(message, t);
	}
}
