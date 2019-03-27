package com.ccacic.assetexchangewrapper.core.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory style class intended to be a singleton for getting ReadOnlyExchanges
 * @author Cameron Cacic
 *
 */
public abstract class ReadOnlyExchangeFactory {

	protected final Map<String, ReadOnlyExchange> exchangeMap;
	
	/**
	 * Creates a new ReadOnlyExchangeFactory
	 */
	protected ReadOnlyExchangeFactory() {
		exchangeMap = new HashMap<>();
	}
	
	/**
	 * Adds the passed ReadOnlyExchange to be associated with the passed ID
	 * @param id the ID
	 * @param exchange the ReadOnlyExchange
	 */
	public void addExchange(String id, ReadOnlyExchange exchange) {
		exchangeMap.put(id, exchange);
	}
	
	/**
	 * Gets the ReadOnlyExchange corresponding to the passed ID
	 * @param id the ID
	 * @return the ReadOnlyExchange, or null if none exists
	 */
	public ReadOnlyExchange getExchange(String id) {
		return exchangeMap.get(id);
	}
	
}
