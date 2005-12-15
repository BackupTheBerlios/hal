package Events;

import appia.AppiaEventException;
import appia.Channel;
import appia.Session;
import appia.events.SendableEvent;

public class UpdatePeerStatusEvent extends SendableEvent {


	
	public UpdatePeerStatusEvent(){
		super();
	}
	
	public UpdatePeerStatusEvent(Channel channel, int dir, Session source) throws AppiaEventException {
		super(channel, dir, source);

	}


}
