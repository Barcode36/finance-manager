package com.ccacic.financemanager.model.account.children;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.children.FiatCurrEntry;
import com.ccacic.financemanager.model.entry.children.StockEntry;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.model.entrychunk.EntryChunkProducer;
import com.ccacic.financemanager.model.entrychunk.children.StockEntryChunk;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Assembles StockAccounts. Tagged with ASSET and uses the Fiat currency set by default
 * @author Cameron Cacic
 *
 */
public class StockAccountAssembler extends AccountAssembler<StockAccount> {
	
	private static final String defDisplayName = "Stock Position";
	private static final String defEntryType = StockEntry.class.getSimpleName();
	private static final Set<Currency> defCurrencies = FiatCurrEntry.getFiatCurrs();
	private static final Set<Tag> defTags = new HashSet<>();
	
	public static final String TICKER = "ticker";
	public static final String YIELD = "yield";
	
	/**
	 * Creates a new StockAccountAssembler with the default values
	 */
	public StockAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.ASSET);
	}

	/**
	 * Creates a new StockAccountAssembler with the passed values
	 * @param displayName the display name of StockAccount
	 * @param entryType the Entry type allowed for StockAccount
	 * @param currencies the supported Currencies for StockAccount
	 * @param tags the Tags associated with StockAccount
	 */
    private StockAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
                                  Set<Tag> tags) {
		super(StockAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public StockAccount assembleAccount(ParamMap paramMap) {
		StockAccount stockAccount = new StockAccount();
		stockAccount.ticker(paramMap.get(TICKER))
		.yield(paramMap.getAsDouble(YIELD));
		
		return stockAccount;
	}

	@Override
	public void modifyAccount(StockAccount account, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(TICKER)) {
			delta.addPartialDelta(TICKER, account.getTicker());
			account.ticker(paramMap.get(TICKER));
			delta.addPartialDelta(TICKER, account.getTicker());
		}
		
		if (paramMap.contains(YIELD)) {
			delta.addPartialDelta(YIELD, account.getYield());
			account.yield(paramMap.getAsDouble(YIELD));
			delta.addPartialDelta(YIELD, account.getYield());
		}
		
	}

	@Override
	public ParamMap disassembleAccount(StockAccount account) {
		ParamMap paramMap = new ParamMap();
		paramMap.put(TICKER, account.getTicker());
		paramMap.put(YIELD, account.getYield() + "");
		return paramMap;
	}
	
	@Override
	public EntryChunkProducer getEntryChunkProducer() {
		return new EntryChunkProducer() {
			
			@Override
			public EntryChunk createEntryChunk(File entryChunkDirectory, Entry firstEntry) {
				return new StockEntryChunk(entryChunkDirectory, firstEntry);
			}
			
			@Override
			public EntryChunk createEntryChunk(File entryChunkFile, String expectedHash) {
				return new StockEntryChunk(entryChunkFile, expectedHash);
			}
		};
	}

}
