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
 * Assembles SavingsAccounts. Tagged with CASH and uses the Fiat currency set
 * @author Cameron Cacic
 *
 */
public class SavingsAccountAssembler extends AccountAssembler<SavingsAccount> {
	
	private static final String defDisplayName = "Savings Account";
	private static final String defEntryType = FiatCurrEntry.class.getSimpleName();
	private static final Set<Currency> defCurrencies = FiatCurrEntry.getFiatCurrs();
	private static final Set<Tag> defTags = new HashSet<>();
		
	public static final String APY = "apy";

	/**
	 * Creates a new SavingsAccountAssembler with the default values
	 */
	public SavingsAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.CASH);
	}
	
	/**
	 * Creates a new SavingsAccountAssembler with the passed values
	 * @param displayName the display name for SavingsAccount
	 * @param entryType the Entry type allowed for SavingsAccount
	 * @param currencies the supported Currencies for SavingsAccount
	 * @param tags the Tags associated with SavingsAccount
	 */
    private SavingsAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
                                    Set<Tag> tags) {
		super(SavingsAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public SavingsAccount assembleAccount(ParamMap paramMap) {
		SavingsAccount savingsAccount = new SavingsAccount();
		savingsAccount.APY(paramMap.getAsDouble(APY));
		return savingsAccount;
	}
	
	@Override
	public void modifyAccount(SavingsAccount account, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(APY)) {
			delta.addPartialDelta(APY, account.getAPY());
			account.APY(paramMap.getAsDouble(APY));
			delta.addPartialDelta(APY, account.getAPY());
		}
		
	}

	@Override
	public ParamMap disassembleAccount(SavingsAccount account) {
		ParamMap paramMap = new ParamMap();
		paramMap.put(APY, account.getAPY() + "");
		return paramMap;
	}

}
