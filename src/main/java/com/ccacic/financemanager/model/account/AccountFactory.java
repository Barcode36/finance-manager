package com.ccacic.financemanager.model.account;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.Factory;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * A singleton Factory for producing Accounts
 * @author Cameron Cacic
 *
 */
public class AccountFactory extends Factory<Account> {
	
	private static final AccountFactory instance = new AccountFactory();
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static AccountFactory getInstance() {
		return instance;
	}
	
	/**
	 * Restricts construction to AccountFactory
	 */
	private AccountFactory() {
		super();
	}
	
	/**
	 * Returns the display name of the passed Account
	 * @param a the Account to get the display name for
	 * @return the display name
	 */
	public String getDisplayName(Account a) {
		return getDisplayName(a.getClass().getSimpleName());
	}
	
	/**
	 * Returns the Entry type of the passed Account
	 * @param a the Account to get the Entry type for
	 * @return the Entry type
	 */
	public String getEntryType(Account a) {
		return getEntryType(a.getClass().getSimpleName());
	}
	
	/**
	 * Returns the Currencies supported by the passed Account
	 * @param a the Account to get the supported Currencies for
	 * @return the supported Currencies
	 */
	public Set<Currency> getCurrencies(Account a) {
		return getCurrencies(a.getClass().getSimpleName());
	}
	
	/**
	 * Returns the Tags associated with the passed Account
	 * @param a the Account to get the associated Tags for
	 * @return the associated Tags
	 */
	public Set<Tag> getTags(Account a) {
		return getTags(a.getClass().getSimpleName());
	}
	
	/**
	 * Returns the display name for the passed Account type
	 * @param key the Account type
	 * @return the display name
	 */
	public String getDisplayName(String key) {
		AccountAssembler<? extends Account> assembler = (AccountAssembler<? extends Account>) assemblerMap.get(key);
		if (assembler == null) {
			return "";
		}
		return assembler.getDisplayName();
	}
	
	/**
	 * Returns the Entry type for the passed Account type
	 * @param key the Account type
	 * @return the Entry type
	 */
	public String getEntryType(String key) {
		AccountAssembler<? extends Account> assembler = (AccountAssembler<? extends Account>) assemblerMap.get(key);
		if (assembler == null) {
			return "";
		}
		return assembler.getEntryType();
	}
	
	/**
	 * Returns the supported Currencies for the passed Account type
	 * @param key the Account type
	 * @return the supported Currencies
	 */
	public Set<Currency> getCurrencies(String key) {
		AccountAssembler<? extends Account> assembler = (AccountAssembler<? extends Account>) assemblerMap.get(key);
		if (assembler == null) {
			Logger.getInstance().logError("AccountAssembler not found for '" + key + "'");
			return new HashSet<>();
		}
		return assembler.getCurrencies();
	}
	
	/**
	 * Returns the associated Tags for the passed Account type
	 * @param key the Account type
	 * @return the associated Tags
	 */
	private Set<Tag> getTags(String key) {
		AccountAssembler<? extends Account> assembler = (AccountAssembler<? extends Account>) assemblerMap.get(key);
		if (assembler == null) {
			Logger.getInstance().logError("AccountAssembler not found for '" + key + "'");
			return new HashSet<>();
		}
		return assembler.getTags();
	}
	
	@Override
	public Account requestItem(ParamMap paramMap) {
		return super.requestItem(paramMap);
	}
	
}
