package com.ccacic.assetexchangewrapper.alphavantage.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyExchange;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;

/**
 * An Exchange for alphavantage.co
 * @author Cameron Cacic
 *
 */
public class AlphaVantageReadOnlyExchange implements ReadOnlyExchange {
	
	private final Interval interval;
	private final Map<String, ReadOnlyMarket> markets;
	
	/**
	 * Creates a new AlphaVantageReadOnlyExchange
	 * @param interval the interval of the exchange
	 */
	public AlphaVantageReadOnlyExchange(Interval interval) {
		this.interval = interval;
		markets = new HashMap<>();
	}

	@Override
	public ReadOnlyMarket getReadOnlyMarket(String baseCurr, String convCurr) throws IOException {
		if ("USD".equals(baseCurr)) {
			return getReadOnlyMarket(baseCurr + "-" + convCurr);
		}
		throw new MissingMarketException(baseCurr + "-" + convCurr);
	}

	@Override
	public ReadOnlyMarket getReadOnlyMarket(String marketName) {
		
		if (markets.containsKey(marketName)) {
			return markets.get(marketName);
		}
		
		ReadOnlyMarket market = new AlphaVantageReadOnlyMarket(marketName, extractStockTicker(marketName), interval);
		markets.put(marketName, market);
		return market;
	}
	
	@Override
	public boolean hasMarket(String baseCurr, String convCurr) {
		
		if (markets.containsKey(baseCurr + "-" + convCurr)) {
			return true;
		}
		
		ReadOnlyMarket market;
		try {
			market = getReadOnlyMarket(baseCurr, convCurr);
		} catch (IOException e) {
			return false;
		}
		
		return market.isActive();
		
	}

	@Override
	public boolean hasMarket(String marketName) {
		
		if (markets.containsKey(marketName)) {
			return true;
		}
		
		ReadOnlyMarket market = getReadOnlyMarket(marketName);
		
		return market.isActive();
		
	}

	@Override
	public String getExchangeName() {
		return "Alpha Vantage";
	}
	
	/**
	 * Extracts the stock ticker from the passed String
	 * @param str the String to extract from
	 * @return the stock ticker
	 */
	private String extractStockTicker(String str) {
		return str.substring(str.indexOf('-') + 1);
	}

	@Override
	public Set<String> getKnownMarkets() {
		return new HashSet<>();
	}

	@Override
	public String[] getCurrsFromMarketName(String name) {
		return name.split("-");
	}

}
