package Events;

import appia.AppiaEventException;
import appia.AppiaException;
import appia.Channel;
import appia.Direction;
import appia.Session;
import appia.events.channel.PeriodicTimer;

public class LinkTimer extends PeriodicTimer {
	
	/**
	 * Constructs a MoveBallTimer object.
	 */
	public LinkTimer() {
		super();
	}

	/**
	 * Constructs a MoveBallTimer object.
	 * 
	 * @param period the period of the event (in milliseconds)
	 * @param channel the Appia channel where the timer will be sent.
	 * @param source the Session that issues this timer.
	 * @param qualifier the qualifier of the timer (accepts values {@link EventQualifier.ON}
	 * 	and {@link EventQualifier.OFF}.
	 * @throws AppiaEventException
	 * @throws AppiaException
	 */
	public LinkTimer(
		long period,
		Channel channel,
		Session source,
		int qualifier)
		throws AppiaEventException, AppiaException {
		super("HAL Timer", period, channel, Direction.DOWN, source, qualifier);
	}

}
