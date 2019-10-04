package com.ccacic.financemanager.model.account.children;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.FiatCurrEntry;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Assembles CheckingAccounts. Tagged with CASH and uses the Fiat currency set by default
 * @author Cameron Cacic
 *
 */
public class CheckingAccountAssembler extends AccountAssembler<CheckingAccount> {
	
	private static final String defDisplayName = "Checking Account";
	private static final String defEntryType = FiatCurrEntry.class.getSimpleName();
	private static final Set<Currency> defCurrencies = FiatCurrEntry.getFiatCurrs();
	private static final Set<Tag> defTags = new HashSet<>();
	
	public static final String APY = "apy";

	/**
	 * Creates a new CheckingAccountAssembler with the default values
	 */
	public CheckingAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.CASH);
	}
	
	/**
	 * Creates a new CheckingAccountAssembler with the passed values
	 * @param displayName the display name of CheckingAccount
	 * @param entryType the Entry type allowed for CheckingAccount
	 * @param currencies the Currencies supported by CheckingAccount
	 * @param tags the Tags associated with CheckingAccount
	 */
    private CheckingAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
                                     Set<Tag> tags) {
		super(CheckingAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public CheckingAccount assembleAccount(ParamMap paramMap) {
		CheckingAccount checkingAccount = new CheckingAccount();
		checkingAccount.APY(paramMap.getAsDouble(APY));
		return checkingAccount;
	}
	
	@Override
	public void modifyAccount(CheckingAccount account, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(APY)) {
			delta.addPartialDelta(APY, account.getAPY());
			account.APY(paramMap.getAsDouble(APY));
			delta.addPartialDelta(APY, account.getAPY());
		}
		
	}

	@Override
	public ParamMap disassembleAccount(CheckingAccount account) {
		ParamMap paramMap = new ParamMap();
		paramMap.put(APY, account.getAPY() + "");
		return paramMap;
	}

}
