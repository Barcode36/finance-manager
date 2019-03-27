package com.ccacic.financemanager.model.entry.children;
import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * An implementation of Entry for cryptocurrency entries. Adds a transaction ID and the fiat value
 * of the CrypCurrEntry at the date and time it occured. Maintains the Set of Currencies representing
 * cryptocurrencies
 * @author Cameron Cacic
 */
public class CrypCurrEntry extends Entry {
	
	private static final Set<Currency> cryptoCurrs = new HashSet<>();
	
	/**
	 * Gets the Currencies representing all known cryptocurrencies
	 * @return a Set of Currencies
	 */
	public static Set<Currency> getCrypCurrs() {
		if (cryptoCurrs.isEmpty()) {
			cryptoCurrs.addAll(Currency.getAllCurrencies(Tag.CRYPTO));
		}
		return cryptoCurrs;
	}
	
	private String transactID;
	private double histFiatValue;
	
	/**
	 * Assembler method for setting the transaction ID
	 * @param transactID the transaction ID
	 * @return this CrypCurrEntry, for chaining
	 */
	CrypCurrEntry transactID(String transactID) {
		this.transactID = transactID;
		return this;
	}
	
	/**
	 * Assembler method for setting the histFiatValue
	 * @param histFiatValue the historical fiat value of the CrypCurrEntry
	 * @return this CrypCurrEntry, for chaining
	 */
	CrypCurrEntry histFiatPrice(double histFiatValue) {
		this.histFiatValue = histFiatValue;
		return this;
	}
	
	/**
	 * Returns the transaction ID
	 * @return the transaction ID
	 */
	public String getTransactID() {
		return transactID;
	}
	
	/**
	 * Returns the historical fiat value
	 * @return the historical fiat value
	 */
	public double getHistFiatValue() {
		return histFiatValue;
	}
	
}
