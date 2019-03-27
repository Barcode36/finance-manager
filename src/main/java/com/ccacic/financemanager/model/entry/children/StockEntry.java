package com.ccacic.financemanager.model.entry.children;

import com.ccacic.financemanager.model.entry.Entry;

/**
 * An implementation of Entry for changes in stock positions. Adds a share count field and
 * a price per share method
 * @author Cameron Cacic
 *
 */
public class StockEntry extends Entry {

	private double shares;
	
	/**
	 * Assembler method for setting the shares
	 * @param shares the shares
	 * @return this StockEntry, for chaining
	 */
	StockEntry shares(double shares) {
		this.shares = shares;
		return this;
	}
	
	/**
	 * Returns the shares
	 * @return the shares
	 */
	public double getShares() {
		return shares;
	}
	
	/**
	 * Returns the price per share, which is calculated as the amount
	 * of the StockEntry divided by the number of shares
	 * @return the price per share
	 */
	public double getPricePerShare() {
		return amount / shares;
	}

}
