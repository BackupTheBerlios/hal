/*
 * Created on 15-Sep-2003
 */
package Client;

import Events.HandShakeEvent;
import Events.UdpNetworkEvent;
import Events.UpdatePeerStatusEvent;
import appia.Layer;
import appia.Session;
import appia.events.channel.ChannelInit;
import appia.protocols.common.RegisterSocketEvent;

/**
 * Client communication layer for the High Availability Link middleware
 * As of this momento, the communication layer routes incoming user data
 * into the server, and retrieves data from it and re-routes it to the 
 * user.
 * 
 * @author Jose Real
 */
public class HalClientCommLayer extends Layer {

	public HalClientCommLayer(){
		super();
		
		/*
		 * The required events are the events that the layer needs to work.
		 * Usually, they are a subset of the accepted events.
		 * The events that are generated from another thread (using the asyncGo() method
		 * of the Class appia.Event) should NOT be here. 
		 */		
		evRequire = new Class[] {
				ChannelInit.class,
				RegisterSocketEvent.class,
		};
		
		/*
		 * Events accepted by the layer. these will be the events
		 * that are going to be received in the handle of the Session.
		 */
		evAccept = new Class[]{
				ChannelInit.class,
				RegisterSocketEvent.class,
				UdpNetworkEvent.class,
				HandShakeEvent.class,
		};
		
		/*
		 * Events provided by the layer. These are only the events that are 
		 * created by this layer. The events that are received on the handle and forwarded 
		 * should NOT be here. 
		 */
		evProvide = new Class[]{
				RegisterSocketEvent.class,
				UdpNetworkEvent.class,
				HandShakeEvent.class,
				UpdatePeerStatusEvent.class,
		};
	}

	/**
	 * creates the corresponding session.
	 * @return the created session.
	 */
	public Session createSession() {
		return new HalClientCommSession(this);
	}

}
