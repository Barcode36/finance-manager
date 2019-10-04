package com.ccacic.financemanager.model.entry.children;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.entry.EntryAssembler;

/**
 * Assembles CrypCurrEntries
 * @author Cameron Cacic
 *
 */
public class CrypCurrEntryAssembler extends EntryAssembler<CrypCurrEntry> {
	
	private static final String defDisplayName = "Cryptocurrency Entry";
	private static final boolean defShowsTime = true;
	
	public static final String TRANSACTION_ID = "trans_id";
	public static final String HISTORIC_FIAT_PRICE = "hist_fiat_value";
	
	/**
	 * Creates a new CrypCurrEntryAssembler with the default values
	 */
	public CrypCurrEntryAssembler() {
		this(defDisplayName, defShowsTime);
	}
	
	/**
	 * Creates a new CrypCurrEntryAssembler with the passed values
	 * @param displayName the name to display for CrypCurrEntry
	 * @param showsTime if CrypCurrEntry should display a time along with a date
	 */
	private CrypCurrEntryAssembler(String displayName, boolean showsTime) {
		super(CrypCurrEntry.class.getSimpleName(), displayName, showsTime);
	}

	@Override
	public CrypCurrEntry assembleEntry(ParamMap paramMap) {
		CrypCurrEntry entry = new CrypCurrEntry();
		
		entry.transactID(paramMap.get(TRANSACTION_ID));
		
		if (paramMap.get(HISTORIC_FIAT_PRICE) != null) {
			entry.histFiatPrice(paramMap.getAsDouble(HISTORIC_FIAT_PRICE));
		}
		
		return entry;
	}
	
	@Override
	public void modifyEntry(CrypCurrEntry entry, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(TRANSACTION_ID)) {
			delta.addPartialDelta(TRANSACTION_ID, entry.getTransactID());
			entry.transactID(paramMap.get(TRANSACTION_ID));
			delta.addPartialDelta(TRANSACTION_ID, entry.getTransactID());
		}
		
		if (paramMap.contains(HISTORIC_FIAT_PRICE)) {
			delta.addPartialDelta(HISTORIC_FIAT_PRICE, entry.getHistFiatValue());
			entry.histFiatPrice(paramMap.getAsDouble(HISTORIC_FIAT_PRICE));
			delta.addPartialDelta(HISTORIC_FIAT_PRICE, entry.getHistFiatValue());
		}
		
	}

	@Override
	public ParamMap disassembleEntry(CrypCurrEntry entry) {
		ParamMap paramMap = new ParamMap();
		
		paramMap.put(TRANSACTION_ID, entry.getTransactID());
		paramMap.put(HISTORIC_FIAT_PRICE, entry.getHistFiatValue() + "");
		
		return paramMap;
	}

}
