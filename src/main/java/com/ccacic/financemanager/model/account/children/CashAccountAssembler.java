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
 * Assembles CashAccounts. Tagged with CASH and uses the Fiat currency set by default
 * @author Cameron Cacic
 *
 */
public class CashAccountAssembler extends AccountAssembler<CashAccount> {
	
	protected static final String defDisplayName = "Cash Account";
	protected static final String defEntryType = FiatCurrEntry.class.getSimpleName();
	protected static final Set<Currency> defCurrencies = FiatCurrEntry.getFiatCurrs();
	protected static final Set<Tag> defTags = new HashSet<>();

	/**
	 * Creates a new CashAccountAssembler with default values
	 */
	public CashAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.CASH);
	}
	
	/**
	 * Creates a new CashAccountAssembler with the passed values
	 * @param displayName the display name of CashAccount
	 * @param entryType the allowed Entry type of CashAccount
	 * @param currencies the supported Currencies of CashAccount
	 * @param tags the Tags associated with CashAccount
	 */
	public CashAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
			Set<Tag> tags) {
		super(CashAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public CashAccount assembleAccount(ParamMap paramMap) {
		return new CashAccount();
	}
	
	@Override
	public void modifyAccount(CashAccount account, ParamMap paramMap, Delta delta) {
		return;
	}

	@Override
	public ParamMap disassembleAccount(CashAccount account) {
		return new ParamMap();
	}

}
