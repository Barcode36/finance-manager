package com.ccacic.assetexchangewrapper.currencyconverterapi.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.PublicConnection;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;
import com.google.gson.JsonObject;

/**
 * Public connection for connecting to Currency Converter API
 * @author Cameron Cacic
 *
 */
class CurrencyConverterApiPublicConnection extends PublicConnection {

	private static final String CURR_CONV_URL = "https://free.currencyconverterapi.com/api/v6/";
	
	/**
	 * Creates a new CurrencyConverterApiPublicConnection
	 */
	public CurrencyConverterApiPublicConnection() {
		super(CURR_CONV_URL);
	}
	
	/**
	 * Gets all the active currencies
	 * @return the active currencies
	 * @throws IOException if one occurs while fetching the data
	 */
	public JsonObject getActiveCurrencies() throws IOException {
		String result = executeRequest("currencies", null, RequestType.GET, null);
		return parseJsonString(result);
	}
	
	/**
	 * 
	 * Gets the latest rate for the passed marketName
	 * @param martketName the name of the market
	 * @return the latest rate
	 * @throws IOException if one occurs while fetching the data
	 */
	public double getRate(String martketName) throws IOException {
		Map<String, String> queries = new HashMap<>();
		queries.put("q", martketName);
		queries.put("compact", "ultra");
		String result = executeRequest("convert", queries, RequestType.GET, null);
		JsonObject obj = parseJsonString(result);
		if (obj == null) {
			throw new MissingMarketException(martketName + " returned a garbage result");
		}
		if (!obj.has(martketName)) {
			throw new MissingMarketException(martketName + " missing from response:\n" + obj);
		}
		return obj.get(martketName).getAsDouble();
	}

}
