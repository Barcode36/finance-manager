package com.ccacic.assetexchangewrapper.core;

import java.io.IOException;

import com.ccacic.assetexchangewrapper.core.api.TimeStampValue;

/**
 * Interface for methods relevent to the bidding process
 * @author Cameron Cacic
 *
 */
public interface Bidding {

	/**
	 * Gets the most recent price where a bid matched an ask or vice versa
	 * @return the last price as a TimeStampValue
	 * @throws IOException
	 */
	TimeStampValue<Double> getLast() throws IOException;
	
	/**
	 * Gets the most recent bid
	 * @return the most recent bid as a TimeStampValue
	 * @throws IOException
	 */
	TimeStampValue<Double> getBid() throws IOException;
	
	/**
	 * Gets the most recent ask
	 * @return the most recent ask as a TimeStampValue
	 * @throws IOException
	 */
	TimeStampValue<Double> getAsk() throws IOException;
	
}
