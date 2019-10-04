package com.ccacic.financemanager.model.account;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.UniqueAssembler;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryFactory;
import com.ccacic.financemanager.model.entrychunk.DateResolution;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.model.entrychunk.EntryChunkManager;
import com.ccacic.financemanager.model.entrychunk.EntryChunkProducer;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Assembles Accounts and contains static data about them. See Assembler for more details
 * @author Cameron Cacic
 *
 * @param <T> the type to assemble, extends Account
 */
public abstract class AccountAssembler<T extends Account> extends UniqueAssembler<Account> {
	
	public static final String TIME_CREATED = "time_created";
	public static final String NAME = "name";
	public static final String CURRENCY = "currency";
	public static final String EXCHANGE_ID = "exchangeid";
	public static final String ENTRY_CHUNK_IDS = "entry_chunk_ids";
	public static final String ENTRY_CHUNK_HASHES = "entry_chunk_hashes";
	public static final String DATE_RESOLUTION = "date_resolution";
	public static final String ACCT_HOLD_ID = "acct_hold_id";
	
	/**
	 * Purges all the EventListeners associated with the passed Account
	 * @param account the Account to purge EventListeners from
	 */
	public static void purgeEventListeners(Account account) {
		final String id = EventManager.getUniqueID(account);
		ReadOnlyList<Entry> entries = account.entryChunkManager.getEntries();
		String[] identifiers = new String[entries.size() + 1];
		identifiers[identifiers.length - 1] = id;
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			identifiers[i] = EventManager.getUniqueID(entry);
		}
		EventManager.removeListenersByIdentifiers(identifiers);
	}
	
	
	private final String displayName;
	private final String entryType;
	private final Set<Currency> currencies;
	private final Set<Tag> tags;
	
	/**
	 * Creates a new AccountAssembler with static data about
	 * its type
	 * @param assemblerName the name of the Assembler
	 * @param displayName the name to show to the user for the Account type
	 * @param entryType the type of Entry the Account type can hold
	 * @param currencies the Currencies allowed for the Account type
	 * @param tags the Tags associated with the Account type
	 */
	protected AccountAssembler(String assemblerName, String displayName,
							   String entryType, Set<Currency> currencies, Set<Tag> tags) {
		super(assemblerName);
		this.displayName = displayName;
		this.entryType = entryType;
		this.currencies = currencies;
		this.tags = tags;
	}
	
	/**
	 * Begins the assembly of the Account. AccountAssembler will continue
	 * the assembly with Account specific fields with the returned value
	 * @param paramMap the ParamMap to assemble from
	 * @return the partially assembled Account instance
	 */
	protected abstract T assembleAccount(ParamMap paramMap);
	
	/**
	 * Begins the modification of the passed Account instance using the
	 * passed ParamMap to source new values from. Changes made are to be
	 * recorded in the pass-through Delta instance
	 * @param account the Account instance to modify
	 * @param paramMap the ParamMap to source new values from
	 * @param delta the Delta to record all changes made in
	 */
	protected abstract void modifyAccount(T account, ParamMap paramMap, Delta delta);
	
	/**
	 * Begins the disassembly of the passed Account instance.
	 * AccountAssembler will continue the disassembly with Account
	 * specific fields into the returned ParamMap
	 * @param account the Account instance to disassemble
	 * @return the partially filled ParamMap
	 */
	protected abstract ParamMap disassembleAccount(T account);
	
	/**
	 * Returns the display name of the Account type for this Assembler,
	 * which is appropriate to display to the user
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the Entry type supported by the Account type for this Assembler
	 * @return the Entry type
	 */
	public String getEntryType() {
		return entryType;
	}
	
	/**
	 * Returns the Currencies supported by the Account type for this Assembler
	 * @return the supported Currencies
	 */
	public Set<Currency> getCurrencies() {
		return new HashSet<>(currencies);
	}
	
	/**
	 * Returns the Tags associated with the Account type for this Assembler
	 * @return the associated Tags
	 */
	public Set<Tag> getTags() {
		return new HashSet<>(tags);
	}
	
	/**
	 * Creates a new EntryChunkProducer to produce EntryChunks for
	 * the Account type for this Assembler
	 * @return the new EntryChunkProducer
	 */
	protected EntryChunkProducer getEntryChunkProducer() {
		return new EntryChunkProducer() {
			
			@Override
			public EntryChunk createEntryChunk(File entryChunkDirectory, Entry firstEntry) {
				return new EntryChunk(entryChunkDirectory, firstEntry);
			}
			
			@Override
			public EntryChunk createEntryChunk(File entryChunkFile, String expectedHash) {
				return new EntryChunk(entryChunkFile, expectedHash);
			}
			
		};
	}
	
	@Override
	public Account assembleUniqueItem(ParamMap paramMap) {
		Account account = assembleAccount(paramMap);
		if (account == null) {
			return null;
		}

		EntryFactory entryFactory = EntryFactory.getInstance();
		
		account.name(paramMap.get(NAME))
		.dateTimeCreated(paramMap.getAsLocalDateTime(TIME_CREATED))
		.currency(paramMap.getAsCurrency(CURRENCY))
		.exchangeID(paramMap.get(EXCHANGE_ID));

		List<String> hashes = paramMap.getAsList(ENTRY_CHUNK_HASHES);
		List<String> idStrings = paramMap.getAsList(ENTRY_CHUNK_IDS);
		Map<String, String> entryChunkIdToHashMap = new HashMap<>();
		for (int i = 0; i < hashes.size(); i++) {
			if (!"".equals(idStrings.get(i)) && !"".equals(hashes.get(i))) {
				entryChunkIdToHashMap.put(idStrings.get(i), hashes.get(i));
			}
		}
		
		preloadIdentifier(paramMap, account);
		
		EntryChunkManager entryChunkManager = new EntryChunkManager(entryChunkIdToHashMap, paramMap.get(ACCT_HOLD_ID),
				account.getIdentifier(), DateResolution.valueOf(paramMap.get(DATE_RESOLUTION)),
				getEntryChunkProducer());
		
		account.entryChunkManager(entryChunkManager);
		
		final String id = EventManager.getUniqueID(account);
		
		String managerId = EventManager.getUniqueID(entryChunkManager);
		EventListener updateListener = e -> EventManager.fireEvent(new Event(Event.UPDATE, id));
		EventManager.addListener(entryChunkManager, updateListener, Event.UPDATE, managerId);
		
		EventManager.addListener(account, e -> {
			Entry newEntry = (Entry) e.getData();
			entryChunkManager.addEntry(newEntry);
			EventManager.fireEvent(new Event(Event.UPDATE, id));
		}, Event.NEW_ENTRY, id);
		
		EventManager.addListener(account, e -> {
			Entry deleteEntry = (Entry) e.getData();
			entryChunkManager.removeEntry(deleteEntry);
			EventManager.fireEvent(new Event(Event.UPDATE, id));
		}, Event.DELETE_ENTRY, id);
		
		String currFactId = EventManager.getUniqueID(CurrencyExchangeFactory.getInstance());
		EventManager.addListener(account, updateListener, Event.RATES_REFRESHED, currFactId);
		
		return account;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void modifyItem(Account account, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(TIME_CREATED)) {
			delta.addPartialDelta(TIME_CREATED, account.getDateTimeCreated());
			account.dateTimeCreated(LocalDateTime.parse(paramMap.get(TIME_CREATED)));
			delta.addPartialDelta(TIME_CREATED, account.getDateTimeCreated());
		}
		
		if (paramMap.contains(NAME)) {
			delta.addPartialDelta(NAME, account.getName());
			account.name(paramMap.get(NAME));
			delta.addPartialDelta(NAME, account.getName());
		}
		
		if (paramMap.contains(CURRENCY)) {
			delta.addPartialDelta(CURRENCY, account.getCurrency());
			account.currency(paramMap.getAsCurrency(CURRENCY));
			delta.addPartialDelta(CURRENCY, account.getCurrency());
		}
		
		if (paramMap.contains(EXCHANGE_ID)) {
			delta.addPartialDelta(EXCHANGE_ID, account.getExchangeID());
			account.exchangeID(paramMap.get(EXCHANGE_ID));
			delta.addPartialDelta(EXCHANGE_ID, account.getExchangeID());
		}
		
		modifyAccount((T) account, paramMap, delta);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParamMap disassembleUniqueItem(Account account) {
		ParamMap paramMap = disassembleAccount((T) account);
		
		paramMap.putType(account.getClass().getSimpleName());
		
		paramMap.put(TIME_CREATED, account.getDateTimeCreated().toString());
		paramMap.put(NAME, account.getName());
		paramMap.put(CURRENCY, account.getCurrency().getCode());
		paramMap.put(EXCHANGE_ID, account.getExchangeID());
		
		EntryChunkManager entryChunkManager = account.getEntryChunkManager();
		
		paramMap.put(DATE_RESOLUTION, entryChunkManager.getDateResolution().name());
		List<String> hashes = new ArrayList<>();
		List<String> idStrings = new ArrayList<>();
		List<EntryChunk> chunks = entryChunkManager.getEntryChunks();
		for (EntryChunk chunk : chunks) {
			hashes.add(chunk.commitChanges());
			idStrings.add(chunk.getIdentifier());
		}
		paramMap.put(ENTRY_CHUNK_HASHES, hashes);
		paramMap.put(ENTRY_CHUNK_IDS, idStrings);
		
		return paramMap;
	}
	
}
