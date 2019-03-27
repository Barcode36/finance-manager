package com.ccacic.financemanager.exception;

import java.io.IOException;

/**
 * An IOException for when the expected hash of a File does not
 * match its actual hash
 * @author Cameron Cacic
 *
 */
public class MismatchedHashException extends IOException {

	private String expectedHash;
	private String actualHash;
	
	/**
	 * Creates a new MismatchedHashException
	 * @param message the message for the Exception
	 * @param expectedHash the expected hash of the File
	 * @param actualHash the actual hash of the File
	 */
	public MismatchedHashException(String message, String expectedHash, String actualHash) {
		super(message);
		this.expectedHash = expectedHash;
		this.actualHash = actualHash;
	}
	
	/**
	 * Returns the expected hash of the File
	 * @return the expected hash of the File
	 */
	public String getExpectedHash() {
		return expectedHash;
	}
	
	/**
	 * Returns the actual hash of the File
	 * @return the actual hash of the File
	 */
	public String getActualHash() {
		return actualHash;
	}

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 5967101975943229070L;

}
