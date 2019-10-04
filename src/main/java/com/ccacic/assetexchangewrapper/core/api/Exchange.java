package com.ccacic.assetexchangewrapper.core.api;

import java.io.IOException;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;

/**
 * Exchange relevant methods where actions can be performed on the exchange
 * @author Cameron Cacic
 *
 */
public interface Exchange extends ReadOnlyExchange {
	
	/**
	 * Gets the commission rate of the exchange
	 * @return the commission rate
	 */
	double getCommissionRate();
	
	/**
	 * Attempts to get the Market specified by the passed currency names. Throws
	 * a MissingMarketException if no such market exists
	 * @param curr1 the first currency of the market
	 * @param curr2 the second currency of the market
	 * @return the corresponding Market
	 * @throws MissingMarketException if the market is missing
	 * @throws IOException if one occurs while fetching the data
	 */
	Market getMarket(String curr1, String curr2) throws MissingMarketException, IOException;
	
	/**
	 * Attempts to get the Market specified with the passed name. Throws a
	 * MissingMarketException if no such market exists
	 * @param marketName the name of the market
	 * @return the corresponding Market
	 * @throws MissingMarketException if the market is missing
	 * @throws IOException if one occurs while fetching the data
	 */
	Market getMarket(String marketName) throws MissingMarketException, IOException;
	
}
