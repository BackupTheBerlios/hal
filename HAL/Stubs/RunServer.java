package Stubs;
import Server.HalServerCommLayer;
import Server.HalServerCommSession;
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
 * BootStrap Class for the PongServer
 * @author tfd008 Jos� C�rte-Real, Miguel Figueiredo, Leonel Duarte
 */
public class RunServer {

	
	/**
	 * Start's up the channel(s)
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * Arguments
		 */
		if(args.length != 3){
			invalidArgs();
		    System.exit(1);	
		}
		int port = Integer.parseInt(args[0]);
		String userHost = args[1];
		int userPort = Integer.parseInt(args[2]);
		/*
		 * First thing we have to do is to create an array with the
		 * layers that we want to put on the channel.
		 * Index 0 is the lower layer of the channel.
		 */
		/*
		 * Definição dos Layers do Servidor Pong
		 */
		Layer[] qos = {
			new UdpSimpleLayer(),
			new HalServerCommLayer(),
		}; 
		
		/*
		 * Criação da Qos so Servidor Pong
		 */
		QoS myQoS = null;
		try {
			myQoS = new QoS("HAL Server QoS",qos);
		} catch (AppiaInvalidQoSException e) {
			e.printStackTrace();
		}
		
		/*
		 * The Channel is created from the QoS.
		 * The Channel is NOT running yet and is not initialized.
		 */
		Channel channel = myQoS.createUnboundChannel("HAL");
		
		/*
		 * If we need to pass parameters to one of the Sessions, we must create it
		 * from the corresponding Layer and initialize it.
		 * In this case, we need to pass remote host and port.
		 */
		
		HalServerCommSession session = (HalServerCommSession) qos[qos.length-1].createSession();

		/*
		 * Arguments are passed here.
		 */
		
		SessionProperties params = new SessionProperties();
		params.put("port",new Integer(port).toString());
		params.put("userHost",new String(userHost));
		params.put("userPort",new Integer(userPort).toString());
		session.init(params);
		
		
		/*
		 * We must use a cursor to instanciate the session in the channel.
		 */
		ChannelCursor cc = channel.getCursor();
		/*
		 * The session is in the top of the channel.
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
			channel.start();
		} catch (AppiaDuplicatedSessionsException e1) {
			e1.printStackTrace();
		}
		/*
		 * All is set. Every thing is ready to run Appia.
		 */
		Appia.run();
	}

	/**
	 * Prints a invalid arguments exception to the scren
	 */
	private static void invalidArgs() {
		System.err.println("Invalid number of arguments. Usage:");
		System.err.println("java RunServer <server_port> <user_host> <user_port>");
		System.exit(1);
	}
}

