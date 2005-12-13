package Stubs;


import Client.HalClientCommLayer;
import Client.HalClientCommSession;
import appia.Appia;
import appia.AppiaCursorException;
import appia.AppiaDuplicatedSessionsException;
import appia.AppiaInvalidQoSException;
import appia.Channel;
import appia.ChannelCursor;
import appia.Layer;
import appia.QoS;
import appia.protocols.udpsimple.UdpSimpleLayer;
import appia.xml.utils.SessionProperties;

/**
 * @author nuno
 */
public class RunClient {
	

	
	public static void main(String[] args) {
		/*
		 * Arguments
		 */
		if(args.length != 4)
			invalidArgs();
		String remoteHost = args[0];
		int remotePort = Integer.parseInt(args[1]);
		String userHost = args[2];
		int userPort = Integer.parseInt(args[3]);
		/*
		 * First thing we have to do is to create an array with the
		 * layers that we want to put on the channel.
		 * Index 0 is the lower layer of the channel.
		 */
		Layer[] mainQos = {
				new UdpSimpleLayer(), // Camada de comunicacao UDP
				new HalClientCommLayer(), //Camada Hal
		}; 

		/*
		 * With the array of channels, we create the QoS's
		 */
		
		QoS myMainQoS = null;
		try {
			myMainQoS = new QoS("HAL Client QoS",mainQos);
			
		} catch (AppiaInvalidQoSException e) {
			e.printStackTrace();
		}
		
		
		/*
		 * The Channel is created from the QoS.
		 * The Channel is NOT running yet and is not initialized.
		 */
		Channel mainChannel = myMainQoS.createUnboundChannel("HAL");
		
		HalClientCommSession session = (HalClientCommSession) mainQos[mainQos.length-1].createSession();
		
		/*
		 * Arguments are passed here.
		 */		

		SessionProperties params = new SessionProperties();
		params.put("host",new String(remoteHost));
		params.put("port",new Integer(remotePort).toString());
		params.put("userHost",new String(userHost));
		params.put("userPort",new Integer(userPort).toString());
		session.init(params);

		/*
		 * We must use a cursor to instanciate sessions in the channel.
		 */
		ChannelCursor cc = mainChannel.getCursor();
		
		/*
		 * Set the cursor at the top ot the stack
		 */
		cc.top();
		

		try {	
			cc.setSession(session);
		} 
		catch (AppiaCursorException e3) {
			e3.printStackTrace();
		}
	
		
		/*
		 * Uninitialized Sessions are set here. They are created by Appia.
		 */
		try {
			mainChannel.start();
			
		} catch (AppiaDuplicatedSessionsException e1) {
			e1.printStackTrace();
		}
		
		
		/*
		 * All is set. Every thing is ready to run Appia.
		 */
		Appia.run();
	}
	
	/**
	 * 
	 */
	private static void invalidArgs() {
		System.err.println("Invalid number of arguments. Usage:");
		System.err.println("java RunClient <server_host> <server_port> <user_host> <user_port>");
		System.exit(1);
	}
}
