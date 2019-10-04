package com.ccacic.assetexchangewrapper.alphavantage.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.PublicConnection;
import com.google.gson.JsonObject;

/**
 * A PublicConnection for alphavantage.co
 * @author Cameron Cacic
 *
 */
class AlphaVantagePublicConnection extends PublicConnection {
	
	private static final String ALPHA_VAN_URL = "https://www.alphavantage.co/";
	private static final String API_KEY = "YHZUTKSTWJHWU16M";
	
	/**
	 * Creates a new AlphaVantagePublicConnection
	 */
	public AlphaVantagePublicConnection() {
		super(ALPHA_VAN_URL);
	}
	
	/**
	 * Creates a query map
	 * @param function the function
	 * @param symbol the symbol
	 * @param interval the interval
	 * @return a new query map
	 */
	private Map<String, String> getQueryMap(String function, String symbol, String interval) {
		Map<String, String> queries = new HashMap<>();
		queries.put("function", function);
		queries.put("symbol", symbol);
		queries.put("interval", interval);
		queries.put("apikey", API_KEY);
		return queries;
	}
	
	/**
	 * Gets the intraday time series for the passed symbol and interval
	 * @param symbol the symbol
	 * @param interval the interval
	 * @return the intraday time series as a JsonObject
	 * @throws IOException if an IOException occurs in fetching the data
	 */
	public JsonObject getTimeSeriesIntraday(String symbol, Interval interval) throws IOException {
		
		Map<String, String> queries = getQueryMap("TIME_SERIES_INTRADAY", symbol, interval.getTime() + "min");
		String result = executeRequest("query", queries, RequestType.GET, null);
		return parseJsonString(result);

	}
	
}
