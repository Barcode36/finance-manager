package com.ccacic.assetexchangewrapper.core.api;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory style class intended to be a singleton for getting Exchanges
 * @author Cameron Cacic
 *
 */
public abstract class ExchangeFactory {

	protected final Map<String, Exchange> exchangeMap;
	
	/**
	 * Creates the new ExchangeFactory
	 */
	protected ExchangeFactory() {
		exchangeMap = new HashMap<>();
	}
	
	/**
	 * Adds the given exchange and associates it with the passed ID
	 * @param id the ID to associate the Exchange with
	 * @param exchange the Exchange to add
	 */
	public void addExchange(String id, Exchange exchange) {
		exchangeMap.put(id, exchange);
	}
	
	/**
	 * Returns the Exchange corresponding to the passed ID
	 * @param id the ID to fetch the Exchange with
	 * @return the Exchange, or null if none exists with that ID
	 */
	public Exchange getExchange(String id) {
		return exchangeMap.get(id);
	}
	
}
