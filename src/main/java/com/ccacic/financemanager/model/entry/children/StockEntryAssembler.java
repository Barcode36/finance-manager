package com.ccacic.financemanager.model.entry.children;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.entry.EntryAssembler;

/**
 * Assembles StockEntries
 * @author Cameron Cacic
 *
 */
public class StockEntryAssembler extends EntryAssembler<StockEntry> {

	protected static final String defDisplayName = "Stock Entry";
	protected static final boolean defShowsTime = false;
	
	public static final String SHARES = "shares";
	
	/**
	 * Creates a StockEntryAssembler with the default values
	 */
	public StockEntryAssembler() {
		this(defDisplayName, defShowsTime);
	}
	
	/**
	 * Creates a StockEntryAssembler with the passed values
	 * @param displayName the name to display for StockEntry
	 * @param showsTime if StockEntry displays a time alongside a date
	 */
	protected StockEntryAssembler(String displayName, boolean showsTime) {
		super(StockEntry.class.getSimpleName(), displayName, showsTime);
	}

	@Override
	public StockEntry assembleEntry(ParamMap paramMap) {
		StockEntry stockEntry = new StockEntry();
		stockEntry.shares(paramMap.getAsDouble(SHARES));
		return stockEntry;
	}

	@Override
	public void modifyEntry(StockEntry entry, ParamMap paramMap, Delta delta) {
		if (paramMap.contains(SHARES)) {
			delta.addPartialDelta(SHARES, entry.getShares());
			entry.shares(paramMap.getAsDouble(SHARES));
			delta.addPartialDelta(SHARES, entry.getShares());
		}
	}

	@Override
	public ParamMap disassembleEntry(StockEntry entry) {
		ParamMap paramMap = new ParamMap();
		paramMap.put(SHARES, entry.getShares() + "");
		return paramMap;
	}
	
}
