package com.ccacic.assetexchangewrapper.core;

import java.io.IOException;

import com.ccacic.assetexchangewrapper.core.api.TimeStampValue;

/**
 * Interface for methods relevant to pricing an asset
 * @author Cameron Cacic
 *
 */
public interface Pricing {

	/**
	 * Gets the price at the openning of the current session
	 * @return the open price
	 * @throws IOException
	 */
	TimeStampValue<Double> getOpen() throws IOException;
	
	/**
	 * Gets the price at the close of the current session
	 * @return the close price
	 * @throws IOException
	 */
	TimeStampValue<Double> getClose() throws IOException;
	
	/**
	 * Gets the highest price of the current session
	 * @return the high price
	 * @throws IOException
	 */
	TimeStampValue<Double> getHigh() throws IOException;
	
	/**
	 * Gets the lowest price of the current session
	 * @return the low price
	 * @throws IOException
	 */
	TimeStampValue<Double> getLow() throws IOException;
	
}
