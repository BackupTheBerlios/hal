/*
 * Created on 15-Sep-2003
 * PongClientSession.java 
 */
package Client;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import Events.HandShakeEvent;
import Events.LinkQualityEvent;
import Events.UdpNetworkEvent;
import Utilities.DEBUG;
import appia.AppiaEventException;
import appia.Channel;
import appia.Direction;
import appia.Event;
import appia.Layer;
import appia.Session;
import appia.events.channel.ChannelInit;
import appia.protocols.common.InetWithPort;
import appia.protocols.common.RegisterSocketEvent;
import appia.xml.interfaces.InitializableSession;
import appia.xml.utils.SessionProperties;

/**
 * Client application. This application was built as a protocol. It creates the Game environment
 * (to the player) and exchanges messages with the game server.
 * 
 * 
 * @author Jose Real , Miguel Raposo, Leonel Duarte
 */



public class HalClientCommSession extends Session implements InitializableSession

{
	private long prePing,postPing;
	private Channel mainChannel = null;
	private InetWithPort user,server,local;
	private Vector<InetWithPort> peers;
	private long link = -1;
	
	/**
	 * Main class constructor.
	 * @param layer the corresponding layer.
	 */
	public HalClientCommSession(Layer layer) {
		super(layer);
		
	}
	
	/**
	 * Main event handler.
	 * @param ev evento de entrada
	 */
	public void handle(Event ev) {
		
		
		if (ev instanceof ChannelInit)
			handleChannelInit((ChannelInit) ev);
		else if (ev instanceof UdpNetworkEvent)
			handleNetwork((UdpNetworkEvent)ev);
		else if (ev instanceof RegisterSocketEvent)
			handleRSE((RegisterSocketEvent)ev);
		else if (ev instanceof HandShakeEvent)
			handleShake((HandShakeEvent)ev);
		else if (ev instanceof LinkQualityEvent)
			handleLink((LinkQualityEvent)ev);
		
		else
			// unexpected event. Forwarding it.
			try {
				ev.go();
			} catch (AppiaEventException e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * Metodo de inicializacao do canal
	 * @param init
	 */
	private void handleChannelInit(ChannelInit init) {
		
		try {
			mainChannel = init.getChannel();
			init.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			RegisterSocketEvent rse = new RegisterSocketEvent(mainChannel, Direction.DOWN, this);
			rse.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Initialization method used by Appia XML support to fill the parameters
	 * used in this session.
	 * @param params parametros de entrada
	 * 
	 */
	public void init(SessionProperties params) {
		
		int userPort=0;
		int remotePort=0;
		String remoteHost="";
		String userHost="";
		
		try{
			if (params.containsKey("port")){
				remotePort = params.getInt("port");
			}
			if (params.containsKey("host")){
				remoteHost = params.getString("host");
			}
			if (params.containsKey("userHost")){
				userHost = params.getString("userHost");
			}
			if (params.containsKey("userPort")){
				userPort = params.getInt("userPort");
			}
			
			user = new InetWithPort(InetAddress.getByName(userHost),userPort);
			server = new InetWithPort(InetAddress.getByName(remoteHost),remotePort);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleNetwork(UdpNetworkEvent ev){
		if(ev.source.equals(user))
			DEBUG.print("Message from user");
		else if (ev.source.equals(server))
			DEBUG.print("Message from Server");
	}
	
	public void handleRSE(RegisterSocketEvent event){
		
		try{
			
			if(!event.error)
				local = new InetWithPort(event.localHost,event.port);
			HandShakeEvent login = new HandShakeEvent(mainChannel,Direction.DOWN,this);
			login.source=local;
			DEBUG.print(("SERVER ADDRESS "+server.toString()));
			login.dest=server;
			login.go();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleShake(HandShakeEvent event){
		try{
			DEBUG.print("Hand-Shake complete");
			LinkQualityEvent ping = new LinkQualityEvent(mainChannel,Direction.DOWN,this);
			ping.dest=server;
			ping.go();
			prePing=Calendar.getInstance().getTimeInMillis();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleLink(LinkQualityEvent ev){
		postPing=Calendar.getInstance().getTimeInMillis();
		DEBUG.print("Link latency to server "+(postPing-prePing));
	}
	
}
