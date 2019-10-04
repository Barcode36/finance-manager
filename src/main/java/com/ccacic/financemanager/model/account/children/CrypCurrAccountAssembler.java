package com.ccacic.financemanager.model.account.children;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntry;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Assembles CrypCurrAccounts. Tagged with ASSET and uses the CrypCurr currency set by default
 * @author Cameron Cacic
 *
 */
public class CrypCurrAccountAssembler extends AccountAssembler<CrypCurrAccount> {
	
	private static final String defDisplayName = "Cryptocurrency Wallet";
	private static final String defEntryType = CrypCurrEntry.class.getSimpleName();
	private static final Set<Currency> defCurrencies = CrypCurrEntry.getCrypCurrs();
	private static final Set<Tag> defTags = new HashSet<>();
	
	/**
	 * Creates a new CrypCurrAccountAssembler with the default values
	 */
	public CrypCurrAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.ASSET);
	}

	/**
	 * Creates a new CrypCurrAccountAssembler with the passed values
	 * @param displayName the display name for CrypCurrAccount
	 * @param entryType the Entry type allowed for CrypCurrAccount
	 * @param currencies the currencies supported by CrypCurrAccount
	 * @param tags the Tags associated with CrypCurrAccouunt
	 */
    private CrypCurrAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
                                     Set<Tag> tags) {
		super(CrypCurrAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public CrypCurrAccount assembleAccount(ParamMap paramMap) {
		return new CrypCurrAccount();
	}

	@Override
	public void modifyAccount(CrypCurrAccount account, ParamMap paramMap, Delta delta) {

	}

	@Override
	public ParamMap disassembleAccount(CrypCurrAccount account) {
		return new ParamMap();
	}

}
