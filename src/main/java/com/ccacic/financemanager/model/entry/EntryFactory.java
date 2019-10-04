package com.ccacic.financemanager.model.entry;

import com.ccacic.financemanager.model.Factory;
import com.ccacic.financemanager.model.ParamMap;

/**
 * A singleton Factory for producing Entries
 * @author Cameron Cacic
 *
 */
public class EntryFactory extends Factory<Entry> {
	
	private static final EntryFactory instance = new EntryFactory();
	
	private static final String DEFAULT_DISPLAY_NAME = "Entry";
	private static final boolean DEFAULT_SHOWS_TIME = false;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static EntryFactory getInstance() {
		return instance;
	}
	
	/**
	 * Reserved for the singleton instance
	 */
	private EntryFactory() {
		super();
	}
	
	/**
	 * Returns the display name of the passed Entry
	 * @param entry the Entry
	 * @return the display name of the Entry
	 */
	public String getDisplayName(Entry entry) {
		return getDisplayName(entry.getClass().getSimpleName());
	}
	
	/**
	 * Checks if the passed Entry shows time
	 * @param entry the Entry to check
	 * @return if the Entry shows time
	 */
	public boolean showsTime(Entry entry) {
		return showsTime(entry.getClass().getSimpleName());
	}
	
	/**
	 * Returns the display name of the passed Entry type
	 * @param key the Entry type
	 * @return the display name
	 */
	private String getDisplayName(String key) {
		if (key == null) {
			return DEFAULT_DISPLAY_NAME;
		}
		EntryAssembler<? extends Entry> assembler = (EntryAssembler<? extends Entry>) assemblerMap.get(key);
		return assembler == null ? DEFAULT_DISPLAY_NAME : assembler.getDisplayName();
	}
	
	/**
	 * Checks if the passed Entry type shows time
	 * @param key the Entry type to check
	 * @return if the Entry type shows time
	 */
	public boolean showsTime(String key) {
		if (key == null) {
			return DEFAULT_SHOWS_TIME;
		}
		EntryAssembler<? extends Entry> assembler = (EntryAssembler<? extends Entry>) assemblerMap.get(key);
		return assembler == null ? DEFAULT_SHOWS_TIME : assembler.showsTime();
	}
	
	@Override
	public Entry requestItem(ParamMap paramMap) {
		return super.requestItem(paramMap);
	}
	
}
