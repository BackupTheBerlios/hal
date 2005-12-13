package Events;

import appia.AppiaEventException;
import appia.Channel;
import appia.Session;
import appia.events.SendableEvent;

public class HandShakeEvent extends SendableEvent {

	private Object encapsulatedDatagram;
	
	public HandShakeEvent(){
		super();
	}
	
	public HandShakeEvent(Channel channel, int dir, Session source) throws AppiaEventException {
		super(channel, dir, source);
	}
	
	public Object getPayload(){
		return encapsulatedDatagram;
	}
	
	public void setPayload(Object in){
		encapsulatedDatagram=in;
	}

}
