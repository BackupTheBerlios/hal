package Server;

import java.net.InetAddress;
import java.util.HashMap;

import Events.HandShakeEvent;
import Events.LinkQualityEvent;
import Events.UdpNetworkEvent;
import Events.UpdatePeerStatusEvent;
import Utilities.DEBUG;
import Utilities.PeerStatus;
import appia.AppiaEventException;
import appia.Channel;
import appia.Direction;
import appia.Event;
import appia.Layer;
import appia.Session;
import appia.events.channel.ChannelInit;
import appia.message.Message;
import appia.protocols.common.InetWithPort;
import appia.protocols.common.RegisterSocketEvent;
import appia.xml.interfaces.InitializableSession;
import appia.xml.utils.SessionProperties;

/**
 * Appia session that implements the HAL Server.
 * 
 * @author tfd008 Jose Corte-Real, Miguel Figueiredo, Leonel Duarte
 */
public class HalServerCommSession extends Session implements InitializableSession {
	
	private int port;
	private InetWithPort local,user;
	private HashMap<Long,PeerStatus> clients;
	private Channel mainChannel;
	private long serial = 0;

	
	/**
	 * Constructs a HALServerSession object.
	 * 
	 * @param layer the HALServerLayer instance associated with this session.
	 */
	public HalServerCommSession(Layer layer) {
		super(layer);
	}
	
	/**
	 * Initialization method used by Appia XML support to fill the parameters
	 * used in this session.
	 * @param params entry params
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
			clients = new HashMap<Long,PeerStatus>();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Generic handle for all events accepted by this session.
	 * @param event entry event
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
		else if (event instanceof UpdatePeerStatusEvent)
			handleUpdatePeerStatus((UpdatePeerStatusEvent)event);
		
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
	 * the socket in the underlying execution environment (OS) 
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
	
	/**
	 * Handles HandShakeEvent 
	 * @param event handshake event
	 */
	
	public void handleShake(HandShakeEvent event){
		
		try{
			
		clients.put(serial,new PeerStatus(false,(InetWithPort)event.source));
		DEBUG.print("New User logged-in from "+((InetWithPort)event.source).toString());
		HandShakeEvent reply = new HandShakeEvent(mainChannel,Direction.DOWN,this);

		reply.getMessage().pushLong(serial);
		reply.dest=event.source;
		reply.go();
		serial++;
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles a link quality assessment event
	 * @param event link event
	 */
	
	public void handleLink(LinkQualityEvent event){
		try{
			LinkQualityEvent reply = new LinkQualityEvent(mainChannel,Direction.DOWN,this);
			reply.dest=event.source;
			//Thread.sleep(99);
			reply.go();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void handleUpdatePeerStatus( UpdatePeerStatusEvent event){
		DEBUG.print("serial "+event.getMessage().peekLong());
		DEBUG.print(((PeerStatus)clients.get(event.getMessage().peekLong())).getAddr().toString());
	}
	
}
