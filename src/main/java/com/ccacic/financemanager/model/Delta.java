package com.ccacic.financemanager.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of changes. Changes are stored as
 * a mapping from an old value to a new value, with each
 * change additionally being associated with an ID for easy
 * lookup and reference. Also stores a reference to the object
 * the changes it stores reflect upon
 * @author Cameron Cacic
 *
 */
public class Delta {
	
	private final Map<String, Object[]> deltaMap;
	private final Object obj;
	
	/**
	 * Creates a new Delta for the passed object
	 * @param obj the object
	 */
	public Delta(Object obj) {
		deltaMap = new HashMap<>();
		this.obj = obj;
	}
	
	/**
	 * Adds a delta mapping from the passed old value to the passed new value,
	 * and associates it with the passed ID
	 * @param id the ID to associate the delta to
	 * @param oldVal the old value
	 * @param newVal the new value
	 */
	public void addDelta(String id, Object oldVal, Object newVal) {
		Object[] deltaPair = new Object[2];
		deltaPair[0] = oldVal;
		deltaPair[1] = newVal;
		deltaMap.put(id, deltaPair);
	}
	
	/**
	 * Creates a new delta entry with the old value initiated to the first
	 * value passed to this method. Calling the method a second time will
	 * initiate the new value to the passed value. Subsequent calls will
	 * continue updating the new value but leave the old value untouched.
	 * Calls on an ID already in the delta mapping will also replace the
	 * new value with the passed value
	 * @param id the ID to map the delta entry to
	 * @param value the value to put into the delta entry
	 */
	public void addPartialDelta(String id, Object value) {
		if (deltaMap.containsKey(id)) {
			deltaMap.get(id)[1] = value;
		} else {
			Object[] deltaPair = new Object[2];
			deltaPair[0] = value;
			deltaMap.put(id, deltaPair);
		}
	}
	
	/**
	 * Returns the object this Delta was created in reference to
	 * @return the object
	 */
	public Object getObject() {
		return obj;
	}
	
	/**
	 * Gets the old value associated with the passed ID
	 * @param id the ID to reference
	 * @return the old value associated with the ID
	 */
	private Object getOldValue(String id) {
		return deltaMap.get(id)[0];
	}
	
	/**
	 * Gets the new value associated with the passed ID
	 * @param id the ID to reference
	 * @return the new value associated with the ID
	 */
	private Object getNewValue(String id) {
		return deltaMap.get(id)[1];
	}
	
	/**
	 * Gets the old value associated with the passed ID as an int
	 * @param id the ID to reference
	 * @return the old value associated with the ID
	 */
	public int getOldValueAsInt(String id) {
		return (Integer) getOldValue(id);
	}
	
	/**
	 * Gets the new value associated with the passed ID as an int
	 * @param id the ID to reference
	 * @return the new value associated with the ID
	 */
	public int getNewValueAsInt(String id) {
		return (Integer) getNewValue(id);
	}
	
	/**
	 * Gets the old value associated with the passed ID as a double
	 * @param id the ID to reference
	 * @return the old value associated with the ID
	 */
	public double getOldValueAsDouble(String id) {
		return (Double) getOldValue(id);
	}
	
	/**
	 * Gets the new value associated with the passed ID as a double
	 * @param id the ID to reference
	 * @return the new value associated with the ID
	 */
	public double getNewValueAsDouble(String id) {
		return (Double) getNewValue(id);
	}
	
	/**
	 * Returns true if there is a delta associated with the passed ID
	 * @param id the ID to check
	 * @return if an entry existed for the passed ID
	 */
	public boolean hasDeltaEntry(String id) {
		return deltaMap.containsKey(id);
	}
	
	/**
	 * Returns true if the delta associated with the passed ID does not
	 * reflect a meaningful change. This is the case whenever the equals
	 * method of the old value returns true when passing it the new value
	 * @param id the ID to check
	 * @return if the delta reflects a meaningful change
	 */
	public boolean deltaEntryHasChange(String id) {
		Object[] deltaPair = deltaMap.get(id);
		if (deltaPair[0] == null) {
			return deltaPair[1] != null;
		} else {
			if (deltaPair[1] == null) {
				return true;
			} else {
				return !deltaPair[0].equals(deltaPair[1]);
			}
		}
	}

}
