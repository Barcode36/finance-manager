package com.ccacic.assetexchangewrapper.core.api;

import java.time.LocalDateTime;

/**
 * Represents a value that comes with a timestamp
 * @author Cameron Cacic
 *
 * @param <T> the type of data to timestamp
 */
public class TimeStampValue<T> {
	
	private LocalDateTime dateTime;
	private T value;

	/**
	 * Creates a new TimeStampValue at the passed LocalDateTime with the passed data
	 * @param dateTime the LocalDateTime as the timestamp
	 * @param value the data value
	 */
	public TimeStampValue(LocalDateTime dateTime, T value) {
		this.dateTime = dateTime;
		this.value = value;
	}
	
	/**
	 * Returns the timestamp
	 * @return the LocalDateTime of the value
	 */
	public LocalDateTime getTimeStamp() {
		return dateTime;
	}
	
	/**
	 * Returns the data value
	 * @return the data value
	 */
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "TimeStamp: " + dateTime + " Value: " + value;
	}
}
