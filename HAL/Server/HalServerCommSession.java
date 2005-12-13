package Server;

import java.net.InetAddress;
import java.util.HashMap;

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
 * Appia session that implements the Pong server.
 * 
 * @author tfd008 Jose Corte-Real, Miguel Figueiredo, Leonel Duarte
 */
public class HalServerCommSession extends Session implements InitializableSession {
	
	private int port;
	private InetWithPort local,user;
	private HashMap<Long,InetWithPort> clients;
	private Channel mainChannel;
	private long serial = 0;

	
	/**
	 * Constructs a PongServerSession object.
	 * 
	 * @param layer the PongServerLayer instance associated with this session.
	 */
	public HalServerCommSession(Layer layer) {
		super(layer);
	}
	
	/**
	 * Initialization method used by Appia XML support to fill the parameters
	 * used in this session.
	 * @param params parametros de entrada
	 * 
	 */
	public void init(SessionProperties params) {
		
		int userPort=0;
		String userHost="";
		
		if (params.containsKey("port")){
			port = params.getInt("port");
		}
		if (params.containsKey("userHost")){
			userHost = params.getString("userHost");
		}
		if (params.containsKey("userPort")){
			userPort = params.getInt("userPort");
		}
		try{
			user = new InetWithPort(InetAddress.getByName(userHost),userPort);
			clients = new HashMap<Long,InetWithPort>();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Generic handle for all events accepted by this session.
	 * @param event evento de entrada
	 */
	public void handle(Event event) {
		
		if (event instanceof ChannelInit)
			handleChannelInit((ChannelInit) event);
		else if (event instanceof RegisterSocketEvent)
			handleRSE((RegisterSocketEvent)event);
		else if (event instanceof UdpNetworkEvent)
			handleNetwork((UdpNetworkEvent)event);
		else if (event instanceof HandShakeEvent)
			handleShake((HandShakeEvent)event);
		else if (event instanceof LinkQualityEvent)
			handleLink((LinkQualityEvent)event);
		
		else
			try {
				DEBUG.print("unknown event "+event.toString()+" source "+event.getSource()+" DIR: "+event.getDir());
				
				event.go();
			} catch (AppiaEventException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Handler for the ChannelInit event.
	 * 
	 * This event is received only once when the channel is created. It tries to register
	 * the socket in the underlying execution environment (OS) and starts the the periodic
	 * timer that drives the ball movement.
	 * 
	 * @param init the ChannelInit event received in this session.
	 */
	
	private void handleChannelInit(ChannelInit init) {	
		
		try {
			mainChannel = init.getChannel();
			init.go();
		}catch (Exception ex){
			ex.printStackTrace();
		}
		
		try {
			RegisterSocketEvent rse = new RegisterSocketEvent(mainChannel, Direction.DOWN, this,port);
			rse.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
		
		DEBUG.print("Open mainChannel with name " + init.getChannel().getChannelID());
	}
	
	public void handleNetwork(UdpNetworkEvent ev){
	}
	
	public void handleRSE(RegisterSocketEvent event){
		
		try{
			if(!event.error){
				local = new InetWithPort(event.localHost,event.port);
				DEBUG.print("Server Online");
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleShake(HandShakeEvent event){
		try{
		clients.put(serial,(InetWithPort)event.source);
		DEBUG.print("New User logged-in from "+((InetWithPort)event.source).toString());
		HandShakeEvent reply = new HandShakeEvent(mainChannel,Direction.DOWN,this);
		reply.dest=event.source;
		reply.go();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleLink(LinkQualityEvent event){
		try{
			LinkQualityEvent reply = new LinkQualityEvent(mainChannel,Direction.DOWN,this);
			reply.dest=event.source;
			Thread.sleep(100);
			reply.go();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
