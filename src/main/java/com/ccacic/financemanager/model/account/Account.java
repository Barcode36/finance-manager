package com.ccacic.financemanager.model.account;
import java.time.LocalDateTime;

import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.Unique;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.model.entrychunk.EntryChunkManager;

/**
 * The second layer of the Model. Accounts are in charge of managing EntryChunks,
 * maintaining a proper name, a Currency to convert its EntryChunk totals to, a
 * Currency to apply to the totals of the EntryChunks, the time it was created,
 * and the name of the preferred exchange for Currency conversion
 * @author Cameron Cacic
 *
 */
public abstract class Account extends Unique {
	
	protected EntryChunkManager entryChunkManager;
	protected String name;
	protected Currency currency;
	protected String exchangeID;
	protected LocalDateTime dateTimeCreated;
	
	/**
	 * Assembler method for setting the dateTimeCreated
	 * @param dateTimeCreated the LocalDateTime this Account was created
	 * @return this Account, for chaining
	 */
	Account dateTimeCreated(LocalDateTime dateTimeCreated) {
		this.dateTimeCreated = dateTimeCreated;
		return this;
	}
	
	/**
	 * Assembler method for setting the name
	 * @param name the proper name of the Account
	 * @return this Account, for chaining
	 */
	Account name(String name) {
		this.name = name;
		return this;
	}
	
	/**
	 * Assembler method for setting the currency
	 * @param currency the Currency the Account will use
	 * @return this Account, for chaining
	 */
	Account currency(Currency currency) {
		this.currency = currency;
		return this;
	}
	
	/**
	 * Assembler method for setting the exchangeID
	 * @param exchangeID the name of the preferred exhange for conversion
	 * @return this Account, for chaining
	 */
	Account exchangeID(String exchangeID) {
		this.exchangeID = exchangeID;
		return this;
	}
	
	/**
	 * Assembler method for setting the entryChunkManager
	 * @param entryChunkManager the EntryChunkManager for managing the EntryChunks
	 * @return this Account, for chaining
	 */
	Account entryChunkManager(EntryChunkManager entryChunkManager) {
		this.entryChunkManager = entryChunkManager;
		return this;
	}
	
	/**
	 * Returns the date and time the Account was created
	 * @return the LocalDateTime the Account was created
	 */
	public LocalDateTime getDateTimeCreated() {
		return dateTimeCreated;
	}
	
	/**
	 * Returns the proper name of the Account
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the raw total of the EntryChunks in the Account
	 * @return the raw total
	 */
	public double getRawTotal() {
		double total = 0;
		for (EntryChunk entryChunk: entryChunkManager.getEntryChunks()) {
			total += entryChunk.getTotal();
		}
		return total;
	}
	
	/**
	 * Gets the raw total of the EntryChunks in the Account formatted
	 * with the Account's currency
	 * @return the formatted raw total
	 */
	public String formattedRawTotal() {
		return currency.format(getRawTotal());
	}
	
	/**
	 * Gets the total of the EntryChunks in the Account converted to
	 * the passed Currency
	 * @param curr the Currency to convert to
	 * @return the converted total
	 */
	public double getTotal(Currency curr) {
		CurrencyExchangeFactory factory = CurrencyExchangeFactory.getInstance();
		double exchangeRate = factory.getCurrencyConversionRate(currency, curr, exchangeID);
		return getRawTotal() * exchangeRate;
	}
	
	/**
	 * Gets the total of the EntryChunks in the Account converted and
	 * formatted to the passed Currency
	 * @param curr the Currency to convert to and format by
	 * @return the converted and formatted total
	 */
	public String formattedTotal(Currency curr) {
		return curr.format(getTotal(curr));
	}
	
	/**
	 * Returns the EntryChunks held by the Account in an
	 * unmodifiable List
	 * @return the EntryChunks of the Account
	 */
	public ReadOnlyList<EntryChunk> getEntryChunks() {
		return new ReadOnlyList<>(entryChunkManager.getEntryChunks());
	}
	
	/**
	 * Returns the EntryChunkManager of the Account
	 * @return the EntryChunkManager
	 */
	public EntryChunkManager getEntryChunkManager() {
		return entryChunkManager;
	}
	
	/**
	 * Returns the Currency of the Account
	 * @return the Currency
	 */
	public Currency getCurrency() {
		return currency;
	}
	
	/**
	 * Returns the name of the prefered exchange of the Account
	 * @return the exchange name
	 */
	public String getExchangeID() {
		return exchangeID;
	}
	
}
