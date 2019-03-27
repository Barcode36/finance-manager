package com.ccacic.financemanager.event;

/**
 * A functional interface for recieving Events
 * @author Cameron Cacic
 *
 */
public interface EventListener {
	
	/**
	 * Recieves an Event when one occurs
	 * @param event the Event
	 */
	public void onEvent(Event event);

}
