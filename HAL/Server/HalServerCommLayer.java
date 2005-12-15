package Server;

import Events.HandShakeEvent;
import Events.LinkQualityEvent;
import Events.UdpNetworkEvent;
import Events.UpdatePeerStatusEvent;
import appia.Layer;
import appia.Session;
import appia.events.channel.ChannelInit;
import appia.protocols.common.RegisterSocketEvent;

/**
 * The layer associated with the Pong server session. This layer is used to create
 * instances of PongServerSession and states the provided, required and accepted
 * events for that session.
 * 
 * @author jmocito
 */
public class HalServerCommLayer extends Layer {

	public HalServerCommLayer() {
		super();
		
		/*
		 * Para integrar compatibilidde com comunicaçao em grupo, temos de fazer provide 
		 * de um evento GroupInit e aceitar eventos de View e de BlockOk
		 */
		
		// The provided events.
		evProvide=new Class[] {
				RegisterSocketEvent.class,
				UdpNetworkEvent.class,
				HandShakeEvent.class,
				LinkQualityEvent.class,
		};
		
		// The required events.
		evRequire=new Class[] {
			ChannelInit.class,
			RegisterSocketEvent.class,
		};
		
		// The accepted events.
		evAccept=new Class[] {
			ChannelInit.class,
			RegisterSocketEvent.class,
			UdpNetworkEvent.class,
			HandShakeEvent.class,
			LinkQualityEvent.class,
			UpdatePeerStatusEvent.class,
		};
	}

	/**
	 * Creates a new PongServerSession associated with this layer.
	 */
	public Session createSession() {
		return new HalServerCommSession(this);
	}

}
