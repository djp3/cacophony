package com.quub.webserver;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;

public abstract class RequestHandler implements Runnable{
	
	private WebServer webServer = null;
	private AccessControl accessControl = null;
	private ExecutorService threadExecutor = null;
	private QuubDBConnectionPool connectionPool = null;
	private Socket socket = null;
	private Boolean testing = null;
	
	public abstract Globals getGlobals();
	public abstract void setGlobals(Globals globals);
	
	public WebServer getWebServer() {
		return webServer;
	}
	public void setWebServer(WebServer webServer) {
		this.webServer = webServer;
	}
	protected AccessControl getAccessControl() {
		return accessControl;
	}
	protected void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
	}
	protected ExecutorService getThreadExecutor() {
		return threadExecutor;
	}
	protected void setThreadExecutor(ExecutorService threadExecutor) {
		this.threadExecutor = threadExecutor;
	}
	protected QuubDBConnectionPool getConnectionPool() {
		return connectionPool;
	}
	protected void setConnectionPool(QuubDBConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}
	protected Socket getSocket() {
		return socket;
	}
	protected void setSocket(Socket soc) {
		this.socket = soc;
	}
	protected Boolean getTesting() {
		return testing;
	}
	protected void setTesting(Boolean testing) {
		this.testing = testing;
	}
	
}
