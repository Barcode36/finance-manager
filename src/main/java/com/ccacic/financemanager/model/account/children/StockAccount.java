package com.ccacic.financemanager.model.account.children;

import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.launcher.Launcher;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.model.entrychunk.children.StockEntryChunk;

/**
 * An implementation of Account for representing a position in a stock. Note that
 * this is not an account for holding multiple stocks, but a position in a single
 * stock. This is so that the Entries in StockAccount can represent changes in
 * position for a single stock rather than being a jumbled bag of changes in
 * position for multiple stocks. The unfortunate side effect is confusing the
 * model heirarchy, with AccountHolder becoming the "real" StockAccount holding
 * multiple positions. Adds a stock ticker, yield, and stock curency field, and
 * overrides getTotal to supply the current value of position rather than the
 * quantity of shares, which has been given its own method in getShareTotal
 * @author Cameron Cacic
 *
 */
public class StockAccount extends Account{
	
	private String ticker;
	private double yield;
	private Currency stockTickerCurr;
	
	/**
	 * Assembler method for setting the stock ticker. Uses the stock
	 * ticker to find its Currency representation to set stockTickerCurr
	 * @param ticker the stock ticker
	 * @return this StockAccount, for chaining
	 */
	public StockAccount ticker(String ticker) {
		this.ticker = ticker;
		try {
			this.stockTickerCurr = Currency.getCurrency(ticker);
		} catch (InvalidCurrencyCodeException e) {
			Logger.getInstance().logException(e);
			Launcher.exitImmediately();
		}
		return this;
	}
	
	/**
	 * Assembler method for setting the yield
	 * @param yield the yield
	 * @return this StockAccount, for chaining
	 */
	public StockAccount yield(double yield) {
		this.yield = yield;
		return this;
	}
	
	/**
	 * Returns the stock ticker
	 * @return the stock ticker
	 */
	public String getTicker() {
		return ticker;
	}
	
	/**
	 * Returns the yield
	 * @return the yield
	 */
	public double getYield() {
		return yield;
	}
	
	/**
	 * Gets the total number of shares in the position of this StockAccount
	 * @return the number of shares
	 */
	private double getShareTotal() {
		double total = 0;
		for (EntryChunk entryChunk: entryChunkManager.getEntryChunks()) {
			StockEntryChunk stockEntryChunk = (StockEntryChunk) entryChunk;
			total += stockEntryChunk.getShareTotal();
		}
		return total;
	}
	
	@Override
	public String formattedRawTotal() {
		return stockTickerCurr.format(getShareTotal());
	}
	
	@Override
	public double getTotal(Currency curr) {
		CurrencyExchangeFactory factory = CurrencyExchangeFactory.getInstance();
		double exchangeRate = factory.getCurrencyConversionRate(curr, stockTickerCurr, exchangeID);
		return getShareTotal() * exchangeRate;
	}
	
}
