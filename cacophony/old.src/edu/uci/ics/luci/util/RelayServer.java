package edu.uci.ics.luci.util;

import java.io.File;
import java.io.IOException;

import net.jxta.exception.PeerGroupException;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

public class RelayServer{
	
	//public final static String SUPER_URI = "tcp://128.195.59.254:9701";
	public final static String SUPER_URI = "tcp://173.255.208.77:9701";

    transient NetworkManager manager = null;

    public RelayServer() {
        try {
            manager = new net.jxta.platform.NetworkManager(NetworkManager.ConfigMode.SUPER, "RelayServer",
                    new File(new File(".cache"), "RelayServer").toURI());
            
            NetworkConfigurator configurator = manager.getConfigurator();
            manager.startNetwork();
            
            String tcpInterfaceAddress = configurator.getTcpInterfaceAddress();
            int tcpPort = configurator.getTcpPort();
            String tcpPublicAddress = configurator.getTcpPublicAddress();
            System.out.println("TCP\n\t"+tcpInterfaceAddress+"\n\t"+tcpPort+"\n\t"+tcpPublicAddress);
        } catch (IOException | RuntimeException | PeerGroupException e) {
            e.printStackTrace();
            stop();
        }
    }

    public static void main(String args[]) {
    	/* Test that we are using UTF-8 as default */
		String c = java.nio.charset.Charset.defaultCharset().name();
		if(!c.equals("UTF-8")){
			throw new IllegalArgumentException("The character set is not UTF-8:"+c);
		}
		
        RelayServer server = new RelayServer();
        server.start();
    }

    public void start() {
    	System.out.println("Creating relay server");
    }

    public void stop() {
        manager.stopNetwork();
        manager = null;
    }
}


