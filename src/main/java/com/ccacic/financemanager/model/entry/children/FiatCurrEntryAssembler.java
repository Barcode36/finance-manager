package com.ccacic.financemanager.model.entry.children;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.entry.EntryAssembler;

/**
 * Assembles FiatCurrEntries
 * @author Cameron Cacic
 *
 */
public class FiatCurrEntryAssembler extends EntryAssembler<FiatCurrEntry> {
	
	protected static final String defDisplayName = "Fiat Entry";
	protected static final boolean defShowsTime = false;

	/**
	 * Creates a new FiatCurrEntryAssembler with the default values
	 */
	public FiatCurrEntryAssembler() {
		this(defDisplayName, defShowsTime);
	}
	
	/**
	 * Creates a new FiatCurrEntryAssembler with the passed values
	 * @param displayName the name to display for FiatCurrEntry
	 * @param showsTime if FiatCurrEntry should display a time along with a date
	 */
	protected FiatCurrEntryAssembler(String displayName, boolean showsTime) {
		super(FiatCurrEntry.class.getSimpleName(), displayName, showsTime);
	}

	@Override
	public FiatCurrEntry assembleEntry(ParamMap paramMap) {
		return new FiatCurrEntry();
	}
	
	@Override
	public void modifyEntry(FiatCurrEntry entry, ParamMap paramMap, Delta delta) {
		return;
	}

	@Override
	public ParamMap disassembleEntry(FiatCurrEntry entry) {
		return new ParamMap();
	}

}
