package com.ccacic.assetexchangewrapper.core.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.Pricing;

/**
 * Methods relevant to observing the price action in a market
 * @author Cameron Cacic
 *
 */
public interface ReadOnlyMarket extends Pricing {

	/**
	 * Checks if the market is currently active
	 * @return if the market is active
	 */
	boolean isActive();
	
	/**
	 * Refreshes any constants associated with the market
	 * @throws IOException if one occurs while fetching the data
	 */
	void refreshConstants() throws IOException;

	/**
	 * Gets the name of the market
	 * @return teh name
	 */
	String getMarketName();
	
	/**
	 * Gets the current volume in the currency being traded
	 * @return the volume as a TimeStampValue
	 * @throws IOException if one occurs while fetching the data
	 */
	TimeStampValue<Double> getVolume() throws IOException;
	
	/**
	 * Gets the current volume in the currency being used to price
	 * @return the volume as a TimeStampValue
	 * @throws IOException if one occurs while fetching the data
	 */
	TimeStampValue<Double> getBaseVolume() throws IOException;
	
	/**
	 * Gets the LocalDateTime of the last time the market was fully updated
	 * @return the LocalDateTime of the last update
     */
	LocalDateTime getTimeStamp();
	
	/**
	 * Gets summary information about the market
	 * @return a Map of information
	 * @throws IOException if one occurs while fetching the data
	 */
	Map<String, String> getMarketSummary() throws IOException;
	
}
