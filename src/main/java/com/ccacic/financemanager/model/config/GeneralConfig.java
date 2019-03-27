package com.ccacic.financemanager.model.config;

import com.ccacic.financemanager.model.ParamMap;

/**
 * Singleton representation of the configuration of the system
 * @author Cameron Cacic
 *
 */
public class GeneralConfig {
	
	public static final String DEFAULT_CURR = "default_currency";
	public static final String DATA_FETCH_TIME = "data_fetch_time";
	public static final String ENCRYPTED = "encrypted";
	
	private static final GeneralConfig instance = new GeneralConfig();
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static GeneralConfig getInstance() {
		return instance;
	}
	
	private final ParamMap valueMap;
	
	/**
	 * Creates a GeneralConfig with default values
	 */
	private GeneralConfig() {
		valueMap = new ParamMap();
		valueMap.put(DEFAULT_CURR, "USD");
		valueMap.put(DATA_FETCH_TIME, "60000");
		valueMap.put(ENCRYPTED, "false");
	}
	
	/**
	 * Puts the values in the passed ParamMap into the config
	 * @param map the values to put
	 */
	public void putValues(ParamMap map) {
		valueMap.putAll(map);
	}
	
	/**
	 * Returns the value stored at the passed key
	 * @param key the key
	 * @return the value
	 */
	public String getValue(String key) {
		return valueMap.get(key);
	}
	
	/**
	 * Checks if the config specifies encryption
	 * @return if the config specifies encryption
	 */
	public boolean isEncrypted() {
		return valueMap.getAsBoolean(ENCRYPTED);
	}
	
	/**
	 * Sets the value of the encryption field
	 * @param enc the new value of the encryption field
	 */
	public void setEncrypted(boolean enc) {
		valueMap.put(ENCRYPTED, enc);
	}
	
	/**
	 * Returns the ParamMap backing the config
	 * @return the backing ParamMap
	 */
	public ParamMap getValueMap() {
		return valueMap;
	}
	
}
