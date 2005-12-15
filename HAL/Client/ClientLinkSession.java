package Client;

import java.net.InetAddress;
import java.util.Calendar;

import Events.HandShakeEvent;
import Events.LinkQualityEvent;
import Events.LinkTimer;
import Events.UpdatePeerStatusEvent;
import Utilities.DEBUG;
import appia.AppiaEventException;
import appia.Channel;
import appia.Direction;
import appia.Event;
import appia.EventQualifier;
import appia.Layer;
import appia.Session;
import appia.events.channel.ChannelInit;
import appia.protocols.common.InetWithPort;
import appia.xml.interfaces.InitializableSession;
import appia.xml.utils.SessionProperties;

/**
 * HAL Link assessment layer
 * 
 * The main logic behind per-zone client groups is, that typically a MMO concerns itself with updating
 * the FOV of a certain player, out-of-range changes are of no concern to him, and thus, should not 
 * increase the burden of communication. As such, it would be optimal to create the link adaptation 
 * behind this very concept - (in-game peer position) - where a number of communication optimizations
 * could take place (e.g. server sends one state to designated peer, that peer 'multicasts' to the
 * remainder of the peers in his designated zone, therefor decreasing server load and potentially
 * improving client latency if latency to designated peer is lesser than the latency to the server).
 * As of this moment , this layer needs access to a 3 coordinate value representing the client's 
 * in-game location - While this seems esoterical , the primary algorithm is based on in-game
 * peer-proximity, so user data must accomodate to this requirement. Due to diferent languages constraints
 * An optimal solution for this matter is still being formulated.
 * 
 * *UPDATE* An alternative would be that the HAL server alone needs to know player locations, but a 
 * similar problem persists.
 * 
 * *CURRENT IMPLEMENTATION* As of this moment, no in-game position is being considered , server broadcast
 * Low Ping peers to peers with high latencies - This algorithm is now named SR (Selective Re-Routing)
 * 
 * @author Jose Real
 *
 */

public class ClientLinkSession extends Session implements InitializableSession

{
	/**
	 *  HAL timer timeout
	 */
	private static final int HAL_TIMER_TIMEOUT = 4000;
	
	private static final long LOWPING = 200;
	
	private static final int LATENCY_ERROR_MARGIN = 1000;
	
	private long prePing,postPing;
	private Channel mainChannel;
	private InetWithPort server;
	private long link=-1;
	private static boolean LP = false;
	private long serial=-1;
	
	/**
	 * Main class constructor.
	 * @param layer the corresponding layer.
	 */
	public ClientLinkSession(Layer layer) {
		super(layer);
		
	}
	
	/**
	 * Main event handler.
	 * @param ev evento de entrada
	 */
	public void handle(Event ev) {
				
		if (ev instanceof ChannelInit)
			handleChannelInit((ChannelInit) ev);
		else if (ev instanceof LinkQualityEvent)
			handleLink((LinkQualityEvent)ev);
		else if (ev instanceof LinkTimer)
			handleLinkTimer((LinkTimer)ev);
		else if (ev instanceof HandShakeEvent)
			handleShake((HandShakeEvent)ev);
		
		else
			// unexpected event. Forwarding it.
			try {
				ev.go();
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
		int remotePort = -1;
		String remoteHost = "";
		try{
			if (params.containsKey("port")){
				remotePort = params.getInt("port");
			}
			if (params.containsKey("host")){
				remoteHost = params.getString("host");
			}
			server = new InetWithPort(InetAddress.getByName(remoteHost),remotePort);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void handleLink(LinkQualityEvent ev){
		postPing=Calendar.getInstance().getTimeInMillis();
		setLink((postPing-prePing));
		DEBUG.print("Link latency to server "+link);
	}
	
	/**
	 * Metodo de inicializacao do canal
	 * @param init
	 */
	private void handleChannelInit(ChannelInit init) {
		
		try {
			mainChannel = init.getChannel();
			
			try {
				LinkTimer timer = new LinkTimer(HAL_TIMER_TIMEOUT,mainChannel,this,EventQualifier.ON);
				timer.go();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			init.go();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void handleLinkTimer(LinkTimer event){
		try{
			LinkQualityEvent ping = new LinkQualityEvent(mainChannel,Direction.DOWN,this);
			ping.dest=server;
			ping.go();
			prePing=Calendar.getInstance().getTimeInMillis();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles reply from handShake Event from server
	 * @param event HandShake receipt event
	 */
	
	public void handleShake(HandShakeEvent event){
		if(event.getDir()==(Direction.UP)){
			try{
				serial = event.getMessage().peekLong();
				event.go();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		else{
			try{
				event.go();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Tests and Sets the new Link value
	 * 
	 *  The test portion of this method aims at ignoring outlier values to maintain some precision 
	 *  in latency measurements.
	 * @param in latency to server
	 */
	
	public void setLink(long in){
		
		if(link!=-1){
			if(Math.abs(in-link) <= LATENCY_ERROR_MARGIN){
				DEBUG.printAnoying("Link update within error margin - updating");
				link = (link+in)/2;
			}
			else{
				DEBUG.printAnoying("Discarding value");
			}
		}
		else{
			link=in;
		}
		
		if(link <= LOWPING)
			LP=true;

		else
			LP=false;
		
		updateStatus();
		}
	
	/**
	 * Update peer status in central server - imperative to adaptation algorithm -
	 *
	 */
	
	public void updateStatus(){
		try{
			UpdatePeerStatusEvent update = new UpdatePeerStatusEvent(mainChannel,Direction.DOWN,this);
			update.getMessage().pushBoolean(LP);
			update.getMessage().pushLong(serial);
			update.dest=server;
			update.go();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
