package com.ccacic.financemanager.model.currency.conversion;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.ccacic.assetexchangewrapper.core.api.ReadOnlyExchange;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyExchangeFactory;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.currency.conversion.graph.Edge;
import com.ccacic.financemanager.model.currency.conversion.graph.Graph;

/**
 * A singleton factory style class for finding the conversion rate between
 * two Currencies
 * @author Cameron Cacic
 *
 */
public class CurrencyExchangeFactory extends ReadOnlyExchangeFactory {
	
	private static final String SUPPLEMENTAL_RATE_REQUEST = "supp_rate_request";
	private static final String SUPPLEMENTAL_RATE_RESPONSE = "supp_rate_response";
	
	private static final ReentrantLock supplementingLock = new ReentrantLock();
	private static boolean supplementing = false;
	
	private static final CurrencyExchangeFactory instance = new CurrencyExchangeFactory();
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static CurrencyExchangeFactory getInstance() {
		return instance;
	}
	
	/**
	 * Represents a pairing between two Currencies and an exchange that can be
	 * used to convert between them. Overrides hashCode and equals so that
	 * combining the same two Currencies and exchange in a new MarketKey
	 * causes it to unlock the same value in a HashMap. The order of the
	 * Currencies matters
	 * @author Cameron Cacic
	 *
	 */
	private class MarketKey {
		
		private final Currency curr1;
		private final Currency curr2;
		private final String exchangeId;
		
		/**
		 * Creates a new MarketKey
		 * @param curr1 the first Currency
		 * @param curr2 the second Currency
		 * @param exchangeId the exchange
		 */
		MarketKey(Currency curr1, Currency curr2, String exchangeId) {
			this.curr1 = curr1;
			this.curr2 = curr2;
			this.exchangeId = exchangeId;
		}
		
		@Override
		public int hashCode() {
			return curr1.hashCode() + curr2.hashCode() + exchangeId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !getClass().equals(obj.getClass())) {
				return false;
			}
			MarketKey key = (MarketKey) obj;
			return curr1.equals(key.curr1) && curr2.equals(key.curr2) && exchangeId.equals(key.exchangeId);
		}
		
	}
	
	private final HashMap<MarketKey, ReadOnlyMarket> markets;
	private final Map<String, Graph<Currency>> rateGraphs;
	
	/**
	 * Reserved for singleton instance
	 */
	private CurrencyExchangeFactory() {
		markets = new HashMap<>();
		rateGraphs = new HashMap<>();
	}
	
	/**
	 * Returns the conversion rate from the first currency to the second currency,
	 * using the passed exchange. Returns 0.0 if no rate could be obtained. Throws
	 * an IllegalArgumentException if the passed exchange is unrecognized
	 * @param curr1 the first currency
	 * @param curr2 the second currency
	 * @param exchangeID the exchangeID
	 * @return the conversion rate
	 * @throws IllegalArgumentException if the passed Currency is unknown
	 */
	public double getCurrencyConversionRate(Currency curr1, Currency curr2, String exchangeID) 
			throws IllegalArgumentException {
		
		if (curr1.equals(curr2)) {
			return 1.0;
		}
		
		final Graph<Currency> rateGraph;
		if (rateGraphs.containsKey(exchangeID)) {
			rateGraph = rateGraphs.get(exchangeID);
		} else {
			rateGraph = new Graph<>();
			rateGraphs.put(exchangeID, rateGraph);
		}
		
		double rate;
		if (!(rateGraph.containsNode(curr1) && rateGraph.containsNode(curr2))) {
			
			ReadOnlyExchange exchange = exchangeMap.get(exchangeID);
			if (exchange == null) {
				throw new IllegalArgumentException(exchangeID + " is not a recognized exchange");
			}
			
			try {
				
				ReadOnlyMarket market = exchange.getReadOnlyMarket(curr1.getCode(), curr2.getCode());
				markets.put(new MarketKey(curr1, curr2, exchangeID), market);
				
				try {
					
					rate = market.getClose().getValue();
					rateGraph.addEdge(curr1, curr2, rate);
					rateGraph.addEdge(curr2, curr1, 1.0 / rate);
					
				} catch (MissingMarketException e) {
					
					if (supplementingLock.tryLock()) {
						
						if (!supplementing) {
							
							supplementing = true;
							
							EventManager.addListener(null, e2 -> {
								
								double r = (Double) e2.getData();
								rateGraph.addEdge(curr1, curr2, r);
								rateGraph.addEdge(curr2, curr1, 1.0 / r);
								
								String id = EventManager.getUniqueID(this);
								EventManager.fireEvent(new Event(Event.RATES_REFRESHED, id));
								
								supplementing = false;
								supplementingLock.unlock();
								
							}, SUPPLEMENTAL_RATE_RESPONSE);
							EventManager.fireEvent(new Event(SUPPLEMENTAL_RATE_REQUEST, new Object[] {exchange, market}));
							
						} else {
							supplementingLock.unlock();
						}
						
					}
					
					rate = 0.0;
					
				}
				
			} catch (IOException e) {
				Logger.getInstance().logError(e.getMessage());
				rate = 0.0;
			}
			
		} else {
			
			rate = rateGraph.pathProduct(curr1, curr2);

		}
		
		return rate;		
		
	}
	
	/**
	 * Returns the conversion rate from the first currnecy to the second currency, or 0.0
	 * if no rate could be found
	 * @param curr1 the first currency
	 * @param curr2 the second currency
	 * @return the conversion rate
	 */
	public double getCurrencyConversionRate(Currency curr1, Currency curr2) {
		try {
			return getCurrencyConversionRate(curr1, curr2, findExchange(curr1.getCode(), curr2.getCode()));
		} catch (MissingMarketException e) {
			Logger.getInstance().logError(e.getMessage());
			return 0.0;
		}
	}
	
	/**
	 * Finds an exchange for conversion between the provided currencies
	 * @param currCode1 the first currency
	 * @param currCode2 the second currency
	 * @return the exchangeID
	 * @throws MissingMarketException if the market is missing
	 */
	private String findExchange(String currCode1, String currCode2) throws MissingMarketException {
		
		CurrencyExchangeFactory exchangeFactory = CurrencyExchangeFactory.getInstance();
		for (String id: exchangeFactory.getExchangeIDs()) {
			
			ReadOnlyExchange exchange = exchangeFactory.getExchange(id);
			if (exchange.hasMarket(currCode1, currCode2)) {
				return id;
			}
			
		}
		
		throw new MissingMarketException("Could not find a market for " + currCode1 + " to " + currCode2
				+ " in any exchange");
		
	}
	
	/**
	 * Returns all the known exchanges
	 * @return all the known exchanges
	 */
	public Set<String> getExchangeIDs() {
		return new HashSet<>(exchangeMap.keySet());
	}
	
	/**
	 * Updates the rate graphs using the stored markets
	 */
	public synchronized void refreshRates() {
		for (String exchangeId: rateGraphs.keySet()) {
			
			Graph<Currency> rateGraph = rateGraphs.get(exchangeId);
			Set<Edge<Currency>> edges = rateGraph.getEdges();
			for (Edge<Currency> edge: edges) {
				
				Currency curr1 = edge.getHead();
				Currency curr2;
				boolean flipped = false;
				
				if (markets.containsKey(new MarketKey(curr1, edge.getTail(), exchangeId))) {
					curr2 = edge.getTail();
				} else {
					curr2 = curr1;
					curr1 = edge.getTail();
					flipped = true;
				}
				
				ReadOnlyMarket market = markets.get(new MarketKey(curr1, curr2, exchangeId));
				double newRate;
				try {
					newRate = market.getClose().getValue();
				} catch (IOException e) {
					Logger.getInstance().logError(e.getMessage());
					newRate = edge.getWeight();
				}
				
				edge.setWeight(flipped ? 1 / newRate : newRate);
			}
		}
		
		String id = EventManager.getUniqueID(this);
		EventManager.fireEvent(new Event(Event.RATES_REFRESHED, id));
	}
	
	@Override
	public void addExchange(String id, ReadOnlyExchange exchange) {
		
		super.addExchange(id, exchange);
		Graph<Currency> graph = new Graph<>();
		
		try {
			
			for (String marketName: exchange.getKnownMarkets()) {
				
				String[] currs = exchange.getCurrsFromMarketName(marketName);
				if (Currency.isValidCurrencyCode(currs[0]) && Currency.isValidCurrencyCode(currs[1])) {
					
					Currency curr1 = Currency.getCurrency(currs[0]);
					Currency curr2 = Currency.getCurrency(currs[1]);
					ReadOnlyMarket market = exchange.getReadOnlyMarket(marketName);
					markets.put(new MarketKey(curr1, curr2, id), market);
					
					double rate;
					try {
						rate = market.getClose().getValue();
					} catch (MissingMarketException e) {
						Logger.getInstance().logError(e.getMessage());
						rate = 0.0;
					}
					
					graph.addEdge(curr1, curr2, rate);
					graph.addEdge(curr2, curr1, 1.0 / rate);
					
				}
				
			}
			
		} catch (IOException | InvalidCurrencyCodeException e) {
			e.printStackTrace();
		}

		rateGraphs.put(id, graph);
	}
	
}
