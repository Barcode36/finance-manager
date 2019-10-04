package com.ccacic.assetexchangewrapper.btx.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ccacic.assetexchangewrapper.core.api.Exchange;
import com.ccacic.assetexchangewrapper.core.api.Market;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * An Exchange representation of bittrex.com
 * @author Cameron Cacic
 *
 */
public class BittrexExchange implements Exchange {
	
	private static final double COMMISSION_RATE = 0.0025;
	
	private final BtxPublicConnection connection;
	
	/**
	 * Creates a new Bittrex Exchange
	 */
	public BittrexExchange() {
		connection = new BtxPublicConnection();
	}
	
	@Override
	public double getCommissionRate() {
		return COMMISSION_RATE;
	}

	@Override
	public Market getMarket(String curr1, String curr2) throws IOException {
		return new BtxMarket(formatMarketName(curr1, curr2));
	}
	
	@Override
	public Market getMarket(String marketName) throws IOException {
		return new BtxMarket(marketName);
	}
	
	@Override
	public ReadOnlyMarket getReadOnlyMarket(String curr1, String curr2)
			throws IOException {
		return getMarket(curr1, curr2);
	}

	@Override
	public ReadOnlyMarket getReadOnlyMarket(String marketName) throws IOException {
		return getMarket(marketName);
	}
	
	@Override
	public boolean hasMarket(String curr1, String curr2) {
		return hasMarket(formatMarketName(curr1, curr2));
	}

	@Override
	public boolean hasMarket(String marketName) {
		try {
			return getActiveMarketNames().contains(marketName);
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String getExchangeName() {
		return "Bittrex";
	}
	
	/**
	 * Gets a List of all the active market names
	 * @return a List of active market names
	 * @throws IOException if one occurs while fetching the data
	 */
	private List<String> getActiveMarketNames() throws IOException {
		JsonArray array = connection.getMarkets();
		List<String> activeNames = new ArrayList<>();
		for (JsonElement j: array) {
			JsonObject elem = j.getAsJsonObject();
			if (elem.get("IsActive").getAsBoolean()) {
				activeNames.add(elem.get("MarketName").getAsString());
			}
		}
		return activeNames;
	}

	/**
	 * Formats the passed currencies into a market name
	 * @param curr1 the base currency
	 * @param curr2 the sale currency
	 * @return the market name for the currrencies
	 */
	private String formatMarketName(String curr1, String curr2) {
		return curr2 + "-" + curr1;
	}
	
	/**
	 * Breaks the passed market name into the currencies it trades between
	 */
	public String[] getCurrsFromMarketName(String name) {
		String[] split = name.split("-");
		String[] flipped = new String[2];
		flipped[0] = split[1];
		flipped[1] = split[0];
		return flipped;
	}

	@Override
	public Set<String> getKnownMarkets() throws IOException {
		Set<String> marketNames = new HashSet<>();
		for (JsonElement elem: connection.getMarkets()) {
			JsonObject obj = elem.getAsJsonObject();
			marketNames.add(obj.get("MarketName").getAsString());
		}
		return marketNames;
	}

}
