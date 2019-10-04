package com.ccacic.assetexchangewrapper.currencyconverterapi.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyExchange;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.google.gson.JsonObject;

/**
 * An ReadOnlyExchange representation of currencyconverterapi.com
 * @author Cameron Cacic
 *
 */
public class CurrencyConverterApiReadOnlyExchange implements ReadOnlyExchange {

	private final Interval interval;
	private final Map<String, String> currencies;
	private final CurrencyConverterApiPublicConnection connection;
	
	/**
	 * Creates a new CurrencyConverterApiReadOnlyExchange with the passed Interval
	 * for market values
	 * @param interval the interval
	 */
	public CurrencyConverterApiReadOnlyExchange(Interval interval) {
		connection = new CurrencyConverterApiPublicConnection();
		this.interval = interval;
		this.currencies = new HashMap<>();
		loadCurrencies();
	}
	
	/**
	 * Loads all valid currencies
	 */
	private void loadCurrencies() {
		JsonObject obj;
		try {
			obj = connection.getActiveCurrencies();
		} catch (IOException e) {
			return;
		}
		obj = obj.getAsJsonObject("results");
		currencies.clear();
		for (String key: obj.keySet()) {
			JsonObject curr = obj.get(key).getAsJsonObject();
			String symbol;
			if (curr.has("currencySymbol")) {
				symbol = curr.get("currencySymbol").getAsString();
			} else {
				symbol = key;
			}
			currencies.put(key, symbol);
		}
	}
	
	@Override
	public boolean hasMarket(String baseCurr, String convCurr) {
		return currencies.keySet().contains(convCurr) && currencies.keySet().contains(baseCurr);
	}

	@Override
	public boolean hasMarket(String marketName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReadOnlyMarket getReadOnlyMarket(String baseCurr, String convCurr) {
		return getReadOnlyMarket(baseCurr + "_" + convCurr);
	}

	@Override
	public ReadOnlyMarket getReadOnlyMarket(String marketName) {
		return new CurrencyConverterApiReadOnlyMarket(marketName, interval);
	}

	@Override
	public String getExchangeName() {
		return "Currency Converter API";
	}

	@Override
	public Set<String> getKnownMarkets() {
		return new HashSet<>();
	}

	@Override
	public String[] getCurrsFromMarketName(String name) {
		return name.split("_");
	}

}
