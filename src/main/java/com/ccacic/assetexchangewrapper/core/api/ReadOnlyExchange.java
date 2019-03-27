package com.ccacic.assetexchangewrapper.core.api;

import java.io.IOException;
import java.util.Set;

import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;

/**
 * Read only exchange relevant methods for either exchanges that only observe price action
 * or for wrapping around normal exhanges
 * @author Cameron Cacic
 *
 */
public interface ReadOnlyExchange {
	
	/**
	 * Checks if the exchange has the market corresponding to the passed currencies
	 * @param curr1 the first currency
	 * @param curr2 the second currency
	 * @return if the specified market exists
	 */
	boolean hasMarket(String curr1, String curr2);
	
	/**
	 * Checks if the exchange has the market corresponding to the passed name
	 * @param marketName the market name
	 * @return if the specified market exists
	 */
	boolean hasMarket(String marketName);
	
	/**
	 * Attempts to fetch the market specified by the passed currencies. Throws a 
	 * MissingMarketException if the specified market does not exist
	 * @param curr1 the first currency
	 * @param curr2 the second currency
	 * @return the specified market
	 * @throws MissingMarketException
	 * @throws IOException
	 */
	ReadOnlyMarket getReadOnlyMarket(String curr1, String curr2) throws MissingMarketException, IOException;
	
	/**
	 * Attempts to fetch the market specified by the passed name. Throws a
	 * MissingMarketException if the specified market does not exist
	 * @param marketName the market name
	 * @return the specified market
	 * @throws MissingMarketException
	 * @throws IOException
	 */
	ReadOnlyMarket getReadOnlyMarket(String marketName) throws MissingMarketException, IOException;
	
	/**
	 * Gets the name of the exchange
	 * @return the name
	 */
	String getExchangeName();
	
	/**
	 * Gets the currencies a market trades between by the market name
	 * @param name the name
	 * @return the currencies
	 */
	String[] getCurrsFromMarketName(String name);
	
	/**
	 * Gets all the known markets in the exchange
	 * @return a Set of market names
	 * @throws IOException
	 */
	Set<String> getKnownMarkets() throws IOException;
	
}
