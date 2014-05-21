/**
 * 
 */
package edu.uci.ics.luci.cacophony.node;

public class SqlSanitizingException extends Exception {
	private static final long serialVersionUID = -1495460682005140317L;

	/**
	 * @param message
	 */
	public SqlSanitizingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SqlSanitizingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SqlSanitizingException(String message, Throwable cause) {
		super(message, cause);
	}

}
