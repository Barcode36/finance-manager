package com.ccacic.assetexchangewrapper.btx.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.api.Market;
import com.ccacic.assetexchangewrapper.core.api.Order;
import com.ccacic.assetexchangewrapper.core.api.TimeStampValue;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A market representation of a trading pair on bittrex.com
 * @author Cameron Cacic
 *
 */
class BtxMarket implements Market {
	
	private final BtxPublicConnection connection;
	
	private final String name;
	private final String marketCurr;
	private final String baseCurr;
	
	private double minTradeSize;
	private boolean active;
	private LocalDateTime timeStamp;

	/**
	 * Creates a new BtxMarket with the passed name
	 * @param name the name
	 * @throws MissingMarketException if the market is missing
	 * @throws IOException if one occurs while fetching the data
	 */
	public BtxMarket(String name) throws MissingMarketException, IOException {
		this.name = name;
		String[] currs = name.split("-");
		baseCurr = currs[0];
		marketCurr = currs[1];
		
		connection = new BtxPublicConnection();
		refreshConstants();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void refreshConstants() throws IOException {
		JsonArray array = connection.getMarkets();
		
		for (JsonElement elem: array) {
			
			JsonObject market = elem.getAsJsonObject();
			String marketCurr = market.get("MarketCurrency").getAsString();
			String baseCurr = market.get("BaseCurrency").getAsString();
			if (this.marketCurr.equals(marketCurr) && this.baseCurr.equals(baseCurr)) {
				minTradeSize = market.get("MinTradeSize").getAsDouble();
				active = market.get("IsActive").getAsBoolean();
				timeStamp = LocalDateTime.now();
			}
			
		}
	}
	
	@Override
	public double getMinTradeSize() {
		return minTradeSize;
	}

	@Override
	public String getMarketName() {
		return name;
	}

	@Override
	public TimeStampValue<Double> getVolume() throws IOException {
		JsonObject obj = connection.getMarketSummary(name);
		double volume = obj.get("Volume").getAsDouble();
		return new TimeStampValue<>(LocalDateTime.now(), volume);
	}

	@Override
	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	@Override
	public List<Order> getBuyOrders() {
		return getOrdersByType("buy");
	}

	@Override
	public List<Order> getSellOrders() {
		return getOrdersByType("sell");
	}
	
	/**
	 * Gets all the Orders with the passed type
	 * @param type the type
	 * @return all the Orders with the passed type
	 */
	private List<Order> getOrdersByType(String type) {
		try {
			List<Order> orders = new ArrayList<>();
			JsonArray array = connection.getOrderBook(name, type);
			for (JsonElement elem: array) {
				
				JsonObject orderJ = elem.getAsJsonObject();
				orders.add(new BtxOrder(orderJ.get("Quantity").getAsDouble(),
						orderJ.get("Rate").getAsDouble(),
						null));
				
			}
			return orders;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Order fillBuyOrder(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order fillSellOrder(Order order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order ask(double price, double quantity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order bid(double price, double quantity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cancel(Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TimeStampValue<Double> getLast() throws IOException {
		JsonObject obj = connection.getTicker(name);
		return new TimeStampValue<>(LocalDateTime.now(),
				obj.get("Last").getAsDouble());
	}

	@Override
	public TimeStampValue<Double> getHigh() throws IOException {
		JsonObject obj = connection.getMarketSummary(name);
		return new TimeStampValue<>(LocalDateTime.parse(obj.get("TimeStamp").getAsString()),
				obj.get("High").getAsDouble());
	}

	@Override
	public TimeStampValue<Double> getLow() throws IOException {
		JsonObject obj = connection.getMarketSummary(name);
		return new TimeStampValue<>(LocalDateTime.parse(obj.get("TimeStamp").getAsString()),
				obj.get("Low").getAsDouble());
	}

	@Override
	public TimeStampValue<Double> getBid() throws IOException {
		JsonObject obj = connection.getTicker(name);
		return new TimeStampValue<>(LocalDateTime.now(),
				obj.get("Bid").getAsDouble());
	}

	@Override
	public TimeStampValue<Double> getAsk() throws IOException {
		JsonObject obj = connection.getTicker(name);
		return new TimeStampValue<>(LocalDateTime.now(),
				obj.get("Ask").getAsDouble());
	}

	@Override
	public TimeStampValue<Double> getBaseVolume() throws IOException {
		JsonObject obj = connection.getMarketSummary(name);
		double baseVolume = obj.get("BaseVolume").getAsDouble();
		return new TimeStampValue<>(LocalDateTime.now(), baseVolume);
	}

	@Override
	public Map<String, String> getMarketSummary() throws IOException {
		JsonObject obj = connection.getMarketSummary(name);
		Map<String, String> map = new HashMap<>();
		for (String key: obj.keySet()) {
			map.put(key, obj.get(key).getAsString());
		}
		return map;
	}

	@Override
	public TimeStampValue<Double> getOpen() throws IOException {
		return getLast();
	}

	@Override
	public TimeStampValue<Double> getClose() throws IOException {
		return getLast();
	}

}
