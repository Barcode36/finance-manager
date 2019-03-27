package com.ccacic.assetexchangewrapper.btx.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.PublicConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A PublicConnection for bittrex.com
 * @author Cameron Cacic
 *
 */
class BtxPublicConnection extends PublicConnection {

	private static final String BTX_URL = "https://bittrex.com/api/v1.1/public/";
	
	/**
	 * Creates a new BtxPublicConnection
	 */
	public BtxPublicConnection() {
		super(BTX_URL);
	}
	
	/**
	 * Verifies the successful aquirement of the passed JsonObject. Throws an IOException
	 * if it failed and returns the data element of the obect if it succeeded
	 * @param object the JsonObject to check
	 * @return the data JsonElement
	 * @throws IOException
	 */
	private JsonElement verifySuccess(JsonObject object) throws IOException {
		if (!object.get("success").getAsBoolean()) {
			throw new IOException("Request report came back as unsuccessful: "
					+ object.get("message").getAsString());
		}
		return object.get("result");
	}
	
	/**
	 * Gets all the markets as a JsonArray
	 * @return a JsonArray of market names
	 * @throws IOException
	 */
	public JsonArray getMarkets() throws IOException {
		String result = executeRequest("getmarkets", null, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray();
	}
	
	/**
	 * Gets all the currencies
	 * @return a JsonArray of currencies
	 * @throws IOException
	 */
	public JsonArray getCurrencies() throws IOException {
		String result = executeRequest("getcurrencies", null, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray();
	}
	
	/**
	 * Gets the ticker of the passed market name
	 * @param marketName the market name
	 * @return the ticker as a JsonObject
	 * @throws IOException
	 */
	public JsonObject getTicker(String marketName) throws IOException {
		Map<String, String> queries = new HashMap<>();
		queries.put("market", marketName);
		String result = executeRequest("getticker", queries, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonObject();
	}
	
	/**
	 * Gets all the market summaries
	 * @return the market summaries as a JsonArray
	 * @throws IOException
	 */
	public JsonArray getMarketSummaries() throws IOException {
		String result = executeRequest("getmarketsummaries", null, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray();
	}
	
	/**
	 * Gets the market summary of the passed market name
	 * @param marketName the name of the market
	 * @return the market summary as a JsonObject
	 * @throws IOException
	 */
	public JsonObject getMarketSummary(String marketName) throws IOException {
		Map<String, String> queries = new HashMap<>();
		queries.put("market", marketName);
		String result = executeRequest("getmarketsummary", queries, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray().get(0).getAsJsonObject();
	}
	
	/**
	 * Gets the current orderbook for the passed market where all the orders are of the passed type
	 * @param marketName the name of the market
	 * @param type the type of the orders
	 * @return the order book as a JsonArray
	 * @throws IOException
	 */
	public JsonArray getOrderBook(String marketName, String type) throws IOException {
		Map<String, String> queries = new HashMap<>();
		queries.put("market", marketName);
		queries.put("type", type);
		String result = executeRequest("getorderbook", queries, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray();
	}
	
	/**
	 * Gets the history of the market specified by the passed market name
	 * @param marketName the market name
	 * @return the market history as a JsonArray
	 * @throws IOException
	 */
	public JsonArray getMarketHistory(String marketName) throws IOException {
		Map<String, String> queries = new HashMap<>();
		queries.put("market", marketName);
		String result = executeRequest("getmarkethistory", queries, RequestType.GET, null);
		JsonObject object = parseJsonString(result);
		return verifySuccess(object).getAsJsonArray();
	}

}
