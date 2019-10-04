package com.ccacic.assetexchangewrapper.core;

/**
 * Represents standard time intervals
 * @author Cameron Cacic
 *
 */
public enum Interval {
	ONE_MIN(1),
	FIVE_MIN(5),
	FIFTEEN_MIN(15),
	THIRTY_MIN(30),
	SIXTY_MIN(60);
	
	private final int time;
	
	/**
	 * Creates a new Interval with the passed time in minutes
	 * @param time the time of the Interval
	 */
	Interval(int time) {
		this.time = time;
	}
	
	/**
	 * Returns the time of the Interval
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
}