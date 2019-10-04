package com.ccacic.financemanager.model;

import java.util.UUID;

/**
 * Uses the UUID class to generate a unique identifier for every
 * instance of the Unique class. The UUID is stored as a String and
 * is generated upon the first attempt to retrieve the instance's
 * ID, provided the instance has not already been provided an ID
 * through the setIdentifier method. That method will only set the
 * ID once, however, and subsequent calls will not affect the instance's
 * ID, rendering the ID permanent for the instance's lifespan. Generated
 * ID's are guaranteed to be unique for each instance through the UUID
 * class' contract. This is not true for setting the ID through the
 * setIdentifier method; no uniqueness checking is performed on the
 * passed ID. It is up to the programmer to maintain uniqueness in that
 * manner if they desire it, since there are situations where it is
 * desirable to have multiple Unique instances with the same ID (i.e. 
 * when there needs to be multiple copies of the same Unique). Overrides
 * the equals method so that two Uniques with the name ID are considered
 * equal
 * @author Cameron Cacic
 *
 */
public abstract class Unique {
	
	/**
	 * Generates a String representation of a UUID
	 * @return a String representation of a UUID
	 */
	public static String genUUID() {
		return UUID.randomUUID().toString();
	}
	
	private String identifier;
	
	/**
	 * Creates a new Unique
	 */
    protected Unique() {
		identifier = null;
	}
	
	/**
	 * Returns the ID. If this is the first time getIdentifier is being
	 * called or setIdentifier has not been called with a non-null value,
	 * a unique ID is randomly generated and returned. Subseqent calls will
	 * then always return that ID
	 * @return the unique ID of the Unique
	 */
	public String getIdentifier() {
		if (identifier == null) {
			identifier = UUID.randomUUID().toString();
		}
		return identifier;
	}
	
	/**
	 * Sets the ID to the passed value if the ID has not already been
	 * assigned through this method or an ID was generated through a
	 * call to getIdentifier. If the passed identifier is null, no
	 * changes to the state of this Unique will occur
	 * @param identifier the ID to set as this Unique's unique ID
	 */
	public void setIdentifier(String identifier) {
		if (this.identifier == null && identifier != null) {
			this.identifier = identifier; 
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		try {
			Unique uni = (Unique) obj;
			return uni.getIdentifier().equals(this.getIdentifier());
		} catch (ClassCastException e) {
			return false;
		}
		
	}

}
