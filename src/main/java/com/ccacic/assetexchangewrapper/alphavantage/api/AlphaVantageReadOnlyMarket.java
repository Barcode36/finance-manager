package com.ccacic.assetexchangewrapper.alphavantage.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.ccacic.assetexchangewrapper.core.api.TimeStampValue;
import com.ccacic.assetexchangewrapper.core.exceptions.MissingMarketException;
import com.google.gson.JsonObject;

/**
 * A market representation for alphavantage.co
 * @author Cameron Cacic
 *
 */
class AlphaVantageReadOnlyMarket implements ReadOnlyMarket {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String TIME_PROPERTY = "time_property";
	
	private static final LocalTime OPEN_TIME = LocalTime.of(9, 30);
	private static final LocalTime CLOSE_TIME = LocalTime.of(15, 55);
	
	private AlphaVantagePublicConnection connection;
	
	private final String name;
	private final String stockTicker;
	private final Interval interval;
	
	private TimeStampValue<Double> mostRecentOpen;
	private TimeStampValue<Double> mostRecentHigh;
	private TimeStampValue<Double> mostRecentLow;
	private TimeStampValue<Double> mostRecentClose;
	private TimeStampValue<Double> mostRecentVolume;
	
	private boolean marketActive;
	
	/**
	 * Creates a new AlphVantageReadOnlyMarket
	 * @param name the name of the market
	 * @param stockTicker the stock ticker
	 * @param interval the interval for updates
	 */
	public AlphaVantageReadOnlyMarket(String name, String stockTicker, Interval interval) {
		
		connection = new AlphaVantagePublicConnection();
		
		this.name = name;
		this.stockTicker = stockTicker;
		this.interval = interval;
		
		try {
			updateRecentValues();
		} catch (IOException e) {
			marketActive = false;
		}
		
	}
	
	@Override
	public TimeStampValue<Double> getOpen() throws IOException {
		
		if (newEntryNeeded(mostRecentOpen)) {
			updateRecentValues();
		}
		
		return mostRecentOpen;
		
	}

	@Override
	public TimeStampValue<Double> getClose() throws IOException {
		
		if (newEntryNeeded(mostRecentClose)) {
			updateRecentValues();
		}
		
		return mostRecentClose;
		
	}

	@Override
	public TimeStampValue<Double> getHigh() throws IOException {
		
		if (newEntryNeeded(mostRecentHigh)) {
			updateRecentValues();
		}
		
		return mostRecentHigh;
		
	}

	@Override
	public TimeStampValue<Double> getLow() throws IOException {

		if (newEntryNeeded(mostRecentLow)) {
			updateRecentValues();
		}
		
		return mostRecentLow;
		
	}
	
	@Override
	public TimeStampValue<Double> getVolume() throws IOException {
		
		if (newEntryNeeded(mostRecentOpen)) {
			updateRecentValues();
		}
		
		return mostRecentVolume;
		
	}

	@Override
	public TimeStampValue<Double> getBaseVolume() throws IOException {
		return getVolume();
	}
	
	@Override
	public boolean isActive() {
		return marketActive;
	}

	@Override
	public void refreshConstants() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMarketName() {
		return name;
	}

	@Override
	public LocalDateTime getTimeStamp() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getMarketSummary() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Checks if a new value is avalible for the passed TimeStampValue
	 * @param timeStampValue the value to check for
	 * @return if a new entr is needed
	 */
	private boolean newEntryNeeded(TimeStampValue<?> timeStampValue) {
		
		if (timeStampValue == null) {
			return true;
		}
		
		if (LocalTime.now().isBefore(OPEN_TIME) || LocalTime.now().isAfter(CLOSE_TIME)) {
			return false;
		}
			
		long minutesDifference = timeStampValue.getTimeStamp().until(LocalDateTime.now(), ChronoUnit.MINUTES);
		return minutesDifference >= interval.getTime();
		
	}
	
	/**
	 * Gets the most recent entry from the market
	 * @return the most recent entry as a JsonObject
	 * @throws IOException
	 */
	private JsonObject getMostRecentEntry() throws IOException {
		
		JsonObject obj = connection.getTimeSeriesIntraday(stockTicker, interval);
		obj = obj.getAsJsonObject("Time Series (" + interval.getTime() + "min)");
		
		if (obj == null) {
			if (mostRecentClose == null || mostRecentHigh == null || mostRecentLow == null || mostRecentOpen == null
					|| mostRecentVolume == null) {
				throw new MissingMarketException("One or more values not initialized, cannot supplement missing market");
			}
			return null;
		}
		
		long minDistance = Long.MAX_VALUE;
		String mostRecentKey = "";
		for (String key: obj.keySet()) {
			LocalDateTime time = LocalDateTime.parse(key, FORMATTER);
			long distance = time.until(LocalDateTime.now(), ChronoUnit.MINUTES);
			if (distance < minDistance) {
				mostRecentKey = key;
				minDistance = distance;
			}
		}
		obj = obj.get(mostRecentKey).getAsJsonObject();
		obj.addProperty(TIME_PROPERTY, mostRecentKey);
		return obj;
		
	}
	
	/**
	 * Updates all the values
	 * @throws IOException
	 */
	private void updateRecentValues() throws IOException {
		
		JsonObject entry = getMostRecentEntry();
		if (entry == null) {
			return;
		}
		LocalDateTime time = LocalDateTime.parse(entry.get(TIME_PROPERTY).getAsString(), FORMATTER);
		
		double value = entry.get("1. open").getAsDouble();
		mostRecentOpen = new TimeStampValue<Double>(time, value);
		
		value = entry.get("2. high").getAsDouble();
		mostRecentHigh = new TimeStampValue<Double>(time, value);
		
		value = entry.get("3. low").getAsDouble();
		mostRecentLow = new TimeStampValue<Double>(time, value);
		
		value = entry.get("4. close").getAsDouble();
		mostRecentClose = new TimeStampValue<Double>(time, value);
		
		double valueInt = entry.get("5. volume").getAsDouble();
		mostRecentVolume = new TimeStampValue<Double>(time, valueInt);
		
		marketActive = true;
		
	}

}
