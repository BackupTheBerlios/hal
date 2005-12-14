package Client;

import java.net.InetAddress;
import java.util.Calendar;

import Events.LinkQualityEvent;
import Events.LinkTimer;
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

public class ClientLinkSession extends Session implements InitializableSession

{
	/**
	 *  HAL timer timeout
	 */
	private static final int HAL_TIMER_TIMEOUT = 4000;
	
	private static final int LATENCY_ERROR_MARGIN = 100;
	
	private long prePing,postPing;
	private Channel mainChannel;
	private InetWithPort server;
	private long link;
	
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
	
	public void setLink(long in){

			if(Math.abs(in-link) <= LATENCY_ERROR_MARGIN)
				link = (link+in)/2;
		}
	
}
