package com.ccacic.financemanager.model.entrychunk;

/**
 * Enum for differing resolutions of dates, lower ordinality means higher resolution.
 * Contains a field maxResolution that indicates the cardinality of that resolution
 * from 0 to maxResolution. For example, DAILY indicates a resolution on a daily basis,
 * with a lower ordinality than WEEKLY. It has a maxResolution of 6, meaning that after
 * increasing past 6, DAILY should be reset to 0 and the next resolution level should
 * be incremented
 * @author Cameron Cacic
 *
 */
public enum DateResolution {
	
	DAILY(6),
	WEEKLY(3),
	MONTHLY(1),
	ANNUALY(Integer.MAX_VALUE);
	
	private final int maxResolution;
	
	/**
	 * Creates a new DateResolution
	 * @param max the maximum value for this resolution
	 */
	DateResolution(int max) {
		maxResolution = max;
	}
	
	/**
	 * Returns the maximum resolution value
	 * @return the maximum resolution value
	 */
	public int getMaxResolution() {
		return maxResolution;
	}
	
}
