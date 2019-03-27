package com.ccacic.assetexchangewrapper.core.exceptions;

import java.io.IOException;

/**
 * Exception for when a market has been requested but it doesn't exist
 * @author CAmeron Cacic
 *
 */
public class MissingMarketException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6321695371605265671L;
	
	/**
	 * \Creates a new MissingMarketException with the passed message
	 * @param message the message
	 */
	public MissingMarketException(String message) {
		super(message);
	}
	
}
