package com.ccacic.financemanager.model.entry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.UniqueAssembler;

/**
 * Assembles Entries and contains static data about them. See Assembler for more details
 * @author Cameron Cacic
 *
 * @param <T> the type to assemble, extends Entry
 */
public abstract class EntryAssembler<T extends Entry> extends UniqueAssembler<Entry> {
	
	public static final String DATE_TIME = "date_time";
	public static final String DESCRIPTION ="description";
	public static final String AMOUNT = "amount";
	public static final String FILES = "files";

	private final String displayName;
	private final boolean showsTime;
	
	/**
	 * Creates a new EntryAssembler with static data about its type
	 * @param assemblerName the name of the Assembler
	 * @param displayName the name to show to the user for the Entry type
	 * @param showsTime if the Entry type should display its time alongside its date
	 */
	protected EntryAssembler(String assemblerName, String displayName, boolean showsTime) {
		super(assemblerName);
		this.displayName = displayName;
		this.showsTime = showsTime;
	}
	
	/**
	 * Begins the assembly of the Entry. EntryAssembler will continue the
	 * assembly with Entry specific fields with the returned value
	 * @param paramMap the ParamMap to assemble from
	 * @return the partially assembled Entry instance
	 */
	protected abstract T assembleEntry(ParamMap paramMap);
	
	/**
	 * Begins the modification of the passed Entry instance using the passed
	 * ParamMap to source new values from. Changes made are to be recorded in
	 * the pass-through Delta instance
	 * @param entry the Entry to modify
	 * @param paramMap the ParamMap to source new values from
	 * @param delta the Delta to record all changes made in
	 */
	protected abstract void modifyEntry(T entry, ParamMap paramMap, Delta delta);
	
	/**
	 * Begins disassembly of the passed Entry instance. EntryAssembler will
	 * continue the disassembly with Entry specific fields into the returned
	 * ParamMap
	 * @param entry the Entry instance to disassemble
	 * @return the partially filled ParamMap
	 */
	protected abstract ParamMap disassembleEntry(T entry);
	
	/**
	 * Returns the display name
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Checks if the Entry type shows time
	 * @return if the Entry type shows time
	 */
	public boolean showsTime() {
		return showsTime;
	}
	
	@Override
	protected Entry assembleUniqueItem(ParamMap paramMap) {
		Entry entry = assembleEntry(paramMap);
		
		if (entry == null) {
			Logger.getInstance().logError("A null Entry is being assembled");
			return null;
		}
		
		EntryFactory entryFactory = EntryFactory.getInstance();
		
		if (paramMap.contains(DATE_TIME)) {
			entry.dateTime(paramMap.getAsLocalDateTime(DATE_TIME));
		}
		
		entry.description(paramMap.getAsBracketed(DESCRIPTION))
		.amount(paramMap.getAsDouble(AMOUNT));
		
		List<File> files = new ArrayList<>();
		String filesStr = paramMap.getAsBracketed(FILES);
		if (filesStr != null) {
			String[] filesSplit = filesStr.split(",");
			for (String fileStr: filesSplit) {
				File file = new File(fileStr.trim());
				if (file.isFile()) {
					files.add(file);
				}
			}
		}
		entry.files(files);
		
		return entry;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void modifyItem(Entry entry, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(DATE_TIME)) {
			delta.addPartialDelta(DATE_TIME, entry.getDateTime());
			entry.dateTime(paramMap.getAsLocalDateTime(DATE_TIME));
			delta.addPartialDelta(DATE_TIME, entry.getDateTime());
		}
		
		if (paramMap.contains(DESCRIPTION)) {
			delta.addPartialDelta(DESCRIPTION, entry.getDescription());
			entry.description(paramMap.getAsBracketed(DESCRIPTION));
			delta.addPartialDelta(DESCRIPTION, entry.getDescription());
		}
		
		if (paramMap.contains(AMOUNT)) {
			delta.addPartialDelta(AMOUNT, entry.getAmount());
			entry.amount(paramMap.getAsDouble(AMOUNT));
			delta.addPartialDelta(AMOUNT, entry.getAmount());
		}
		
		if (paramMap.contains(FILES)) {
			delta.addPartialDelta(FILES, entry.getFiles());
			List<File> files = new ArrayList<>();
			String filesStr = paramMap.getAsBracketed(FILES);
			if (filesStr != null) {
				String[] filesSplit = filesStr.split(",");
				for (String file: filesSplit) {
					files.add(new File(file));
				}
			}
			entry.files(files);
			delta.addPartialDelta(FILES, entry.getFiles());
		}
		modifyEntry((T) entry, paramMap, delta);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ParamMap disassembleUniqueItem(Entry entry) {
		ParamMap paramMap = disassembleEntry((T) entry);
		
		paramMap.putType(entry.getClass().getSimpleName());
		
		EntryFactory entryFactory = EntryFactory.getInstance();
		
		paramMap.put(DATE_TIME, entry.getDateTime().toString());
		paramMap.put(DESCRIPTION, "{" + entry.getDescription() + "}");
		paramMap.put(AMOUNT, entry.getAmount() + "");
		paramMap.put(FILES, entry.getFiles());
		
		return paramMap;
	}
	
}
