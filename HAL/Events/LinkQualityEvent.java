package Events;

import appia.AppiaEventException;
import appia.Channel;
import appia.Session;
import appia.events.SendableEvent;

public class LinkQualityEvent extends SendableEvent {

	private Object encapsulatedDatagram;
	
	public LinkQualityEvent(){
		super();
	}
	
	public LinkQualityEvent(Channel channel, int dir, Session source) throws AppiaEventException {
		super(channel, dir, source);
	}
	
	public Object getPayload(){
		return encapsulatedDatagram;
	}
	
	public void setPayload(Object in){
		encapsulatedDatagram=in;
	}

}
