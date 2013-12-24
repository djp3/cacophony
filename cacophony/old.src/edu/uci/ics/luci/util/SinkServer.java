package edu.uci.ics.luci.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;

public class SinkServer implements PipeMsgListener {

    private PeerGroup netPeerGroup = null;

    /**
     * Network is JXTA platform wrapper used to configure, start, and stop the
     * the JXTA platform
     */
    transient NetworkManager manager;
    private PipeService pipeService;
    private PipeAdvertisement pipeAdv;
    private InputPipe inputPipe = null;

    public SinkServer() {
        manager = null;
        try {
            manager = new net.jxta.platform.NetworkManager(NetworkManager.ConfigMode.EDGE, "SinkServer",
                    new File(new File(".cache"), "SinkServer").toURI());
            
            NetworkConfigurator configurator = manager.getConfigurator();
    		
    		URI TheSeed = URI.create(RelayServer.SUPER_URI);
            configurator.addSeedRendezvous(TheSeed);
            configurator.addSeedRelay(TheSeed);
            
            manager.startNetwork();
            
            // Get the NetPeerGroup
            netPeerGroup = manager.getNetPeerGroup();
            // get the pipe service, and discovery
            pipeService = netPeerGroup.getPipeService();
            // create the pipe advertisement
            pipeAdv = SourceServer.getPipeAdvertisement();
            
        } catch (RuntimeException | IOException | PeerGroupException e) {
            e.printStackTrace();
            stop();
        }

    }

   

    /**
     * Dumps the message content to stdout
     *
     * @param msg     the message
     * @param verbose dumps message element content if true
     */
    public static void printMessageStats(Message msg, boolean verbose) {
        try {
            ElementIterator it = msg.getMessageElements();

            System.out.println("------------------Begin Message---------------------");
            WireFormatMessage serialed = WireFormatMessageFactory.toWire(msg, new MimeMediaType("application/x-jxta-msg"), null);

            System.out.println("Message Size :" + serialed.getByteLength());
            while (it.hasNext()) {
                MessageElement el = it.next();
                String eName = el.getElementName();

                System.out.println("Element " + eName);
                if (verbose && eName.equals(SourceServer.MESSAGE_NAME_SPACE)) {
                    System.out.println("[" + el.toString() + "]");
                }
            }
            System.out.println("-------------------End Message----------------------");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * Creates the input pipe with this as the message listener
     */
    public void start() {

        try {
            System.out.println("Creating pipe sink");
            // Create the InputPipe and register this for message arrival
            // notification call-back
            inputPipe = pipeService.createInputPipe(pipeAdv, this);
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
        if (inputPipe == null) {
            System.out.println(" cannot open InputPipe");
            stop();
        }
        System.out.println("Waiting for msgs on input pipe");
    }

    /**
     * Closes the output pipe and stops the platform
     */
    public void stop() {
    	if(netPeerGroup != null){
    		netPeerGroup.stopApp();
    		netPeerGroup = null;
    	}
    	if(pipeService != null){
    		pipeService.stopApp();
    		pipeService = null;
    	}
    	if(inputPipe != null){
    		inputPipe.close();
    		inputPipe = null;
    	}
    	if(manager != null){
    		manager.stopNetwork();
    		manager = null;
    	}
    }

    /**
     * PipeMsgListener interface for asynchronous message arrival notification
     *
     * @param event the message event
     */
    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg;
        try {
            // Obtain the message from the event
            msg = event.getMessage();
            if (msg == null) {
                System.out.println("Received an empty message");
                return;
            }
            else{
            	Date date = new Date(System.currentTimeMillis());
            	System.out.println("\nMessage received at :" + date.toString());
            	// dump the message content to screen
            	extractMessage(msg, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
    
    public static void main(String args[]) {
		String c = java.nio.charset.Charset.defaultCharset().name();
		if(!c.equals("UTF-8")){
			throw new IllegalArgumentException("The character set is not UTF-8:"+c);
		}
		
        SinkServer server = new SinkServer();
        server.start();
    }
}

