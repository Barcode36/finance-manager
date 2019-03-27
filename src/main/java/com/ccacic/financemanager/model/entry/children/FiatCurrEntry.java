package com.ccacic.financemanager.model.entry.children;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * An implementation of Entry for fiat entries. Maintains the fiat Currency set
 * @author Cameron Cacic
 *
 */
public class FiatCurrEntry extends Entry {
	
	private static final Set<Currency> fiatCurrs = new HashSet<>();
	
	/**
	 * Returns the fiat Currency set
	 * @return the fiat Currency set
	 */
	public static Set<Currency> getFiatCurrs() {
		if (fiatCurrs.isEmpty()) {
			fiatCurrs.addAll(Currency.getAllCurrencies(Tag.FIAT));
			Logger.getInstance().logInfo("Fiat currencies loaded into the fiat currency set");
		}
		return fiatCurrs;
	}
}
