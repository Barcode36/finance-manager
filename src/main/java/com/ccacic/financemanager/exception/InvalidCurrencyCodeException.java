package com.ccacic.financemanager.exception;

/**
 * An Exception for invalid currency codes
 * @author Cameron Cacic
 *
 */
public class InvalidCurrencyCodeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5703618671269429459L;
	
	/**
	 * Creates a new InvalidCurrencyCodeException
	 */
	public InvalidCurrencyCodeException() {
		super();
	}
	
	/**
	 * Creates a new InvalidCurrencyCodeException with the passed message
	 * @param message the message for hte Exception
	 */
	public InvalidCurrencyCodeException(String message) {
		super(message);
	}

}
