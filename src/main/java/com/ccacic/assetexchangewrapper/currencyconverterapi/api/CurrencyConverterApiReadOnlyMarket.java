package com.ccacic.assetexchangewrapper.currencyconverterapi.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyMarket;
import com.ccacic.assetexchangewrapper.core.api.TimeStampValue;

/**
 * A ReadOnlyMarket for currencyconverterapi.com
 * @author Cameron Cacic
 *
 */
class CurrencyConverterApiReadOnlyMarket implements ReadOnlyMarket {
	
	private final String name;
	private final Interval interval;
	private boolean marketActive;
	
	private final CurrencyConverterApiPublicConnection connection;
	
	private TimeStampValue<Double> mostRecentRate;
	
	/**
	 * Creates a new CurrencyConverterApiReadOnlyMarket
	 * @param name the name of the market
	 * @param interval the Interval of the market
	 */
	public CurrencyConverterApiReadOnlyMarket(String name, Interval interval) {
		
		connection = new CurrencyConverterApiPublicConnection();
		
		this.name = name;
		this.interval = interval;
	}

	@Override
	public TimeStampValue<Double> getOpen() throws IOException {
		
		if (newEntryNeeded(mostRecentRate)) {
			updateRecentValues();
		}
		
		return mostRecentRate;
		
	}

	@Override
	public TimeStampValue<Double> getClose() throws IOException {
		
		if (newEntryNeeded(mostRecentRate)) {
			updateRecentValues();
		}
		
		return mostRecentRate;
		
	}

	@Override
	public TimeStampValue<Double> getHigh() throws IOException {
		
		if (newEntryNeeded(mostRecentRate)) {
			updateRecentValues();
		}
		
		return mostRecentRate;
		
	}

	@Override
	public TimeStampValue<Double> getLow() throws IOException {

		if (newEntryNeeded(mostRecentRate)) {
			updateRecentValues();
		}
		
		return mostRecentRate;
		
	}
	
	@Override
	public TimeStampValue<Double> getVolume() {
		
		return new TimeStampValue<>(LocalDateTime.now(), -1.0);
		
	}

	@Override
	public TimeStampValue<Double> getBaseVolume() {
		return getVolume();
	}
	
	@Override
	public boolean isActive() {
		return marketActive;
	}

	@Override
	public void refreshConstants() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMarketName() {
		return name;
	}

	@Override
	public LocalDateTime getTimeStamp() {
		return mostRecentRate.getTimeStamp();
	}

	@Override
	public Map<String, String> getMarketSummary() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Checks if if a new entry is avalible for the passed value
	 * @param timeStampValue the value to check
	 * @return if a new entry is needed
	 */
	private boolean newEntryNeeded(TimeStampValue<?> timeStampValue) {
		
		if (timeStampValue == null) {
			return true;
		}
		
		long minutesDifference = timeStampValue.getTimeStamp().until(LocalDateTime.now(), ChronoUnit.MINUTES);
		return minutesDifference >= interval.getTime();
		
	}
	
	/**
	 * Updates the recent values
	 * @throws IOException if one occurs while fetching the data
	 */
	private void updateRecentValues() throws IOException {
		double rate = connection.getRate(name);
		mostRecentRate = new TimeStampValue<>(LocalDateTime.now(), rate);
	}

}
