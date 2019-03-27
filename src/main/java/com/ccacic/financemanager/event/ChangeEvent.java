package com.ccacic.financemanager.event;

import com.ccacic.financemanager.model.Delta;

/**
 * An Update type event that only hold Delta objects as
 * its data. Usage is semantic for when an Update reflects
 * a quantifiable change in some value that either cannot
 * or is inefficient to recompute
 * @author Cameron Cacic
 *
 */
public class ChangeEvent extends Event {

	/**
	 * Creates a new ChangeEvent with the given Delta and id
	 * @param delta the delta to store
	 * @param id the Event id
	 */
	public ChangeEvent(Delta delta, String id) {
		super(UPDATE, delta, id);
	}
	
	/**
	 * Creates a new ChangeEvent with the given Delta
	 * @param delta the Delta to store
	 */
	public ChangeEvent(Delta delta) {
		super(UPDATE, delta);
	}
	
	/**
	 * Returns the Delta
	 * @return the Delta
	 */
	public Delta getDelta() {
		return (Delta) getData();
	}

}
