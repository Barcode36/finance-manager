package com.ccacic.financemanager.fileio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.logger.Logger;

/**
 * A pseudo-interface for fetching a password from the user.
 * Obscures the details of interacting with EventManager to
 * fetch a password to provide a simple, single method call
 * to do so
 * @author Cameron Cacic
 *
 */
class PasswordFetcher {
	
	/**
	 * The default number of tries the user has to input the correct password
	 */
	private static final int NUM_TRIES = 3;
	
	/**
	 * Fetches the password from the user that decrypts the passed File
	 * @param encryptedText the File to check decryption on
	 * @return the proper password, or null if none was obtained
	 * @throws FileNotFoundException if the passed File isn't found
	 */
	public String fetchPassword(File encryptedText) throws FileNotFoundException {
		return fetchPassword(encryptedText, NUM_TRIES);
	}

	/**
	 * Fetches the password from the user that decrypts the passed File
	 * @param encryptedText the File to check decryption on
	 * @param tries the number of tries the user has to input the proper password
	 * @return the proper password, or null if none was obtained
	 * @throws FileNotFoundException if the passed File isn't found
	 */
    private String fetchPassword(File encryptedText, int tries) throws FileNotFoundException {
		
		String password = showPasswordField("Enter password", "Password");
		
		if (password == null) {
			return null;
		}
		
		int attempted = 1;
		do {
			
			InputStream fileStream = new FileInputStream(encryptedText);
			Encryption encryption = new Encryption(fileStream, password);
			if (encryption.getDataDecrypted() != null) {
				return password;
			} else {
				password = showPasswordField("Incorrect password, " + (tries - attempted)
						+ " tries remaining", "Password");
				if (password == null) {
					return null;
				}
			}
			
			attempted++;
			
		} while (attempted < tries);
		
		return null;
		
	}
	
	/**
	 * Obtains a new password from the user
	 * @return the new password, or null if the user cancels
	 */
	public String fetchNewPassword() {
		
		boolean unequal = true;
		String password = null;
		while (unequal) {
			password = showPasswordField("Enter a password", "New Password");
			if (password == null) {
				return null;
			}
			String confirm = showPasswordField("Confirm the password", "Confirm Password");
			if (confirm == null) {
				return null;
			}
			unequal = !confirm.equals(password);
		}
		return password;
		
	}
	
	/**
	 * Shows the password input interface to the user along with the
	 * provided title and message metadata, if the interface supports it
	 * @param message the message to display along with the password input interface
	 * @param title the title to display alongside the password input interface
	 * @return the obtained password, or null if the user cancels
	 */
    private String showPasswordField(String message, String title) {
		
		String id = EventManager.getUniqueID(this);
		String[] result = new String[] {null};
		Logger.getInstance().logDebug("Adding password listener");
		EventManager.addListener(this, e -> {
			result[0] = (String) e.getData();
			synchronized (result) {
				result.notifyAll();
			}
		}, Event.PASSWORD_RECEIVED, id);
		
		EventManager.fireEvent(new Event(Event.PASSWORD_REQUEST, new String[] {id, message, title}, null));
		
		synchronized (result) {
			try {
				result.wait();
			} catch (InterruptedException e1) {
				Logger.getInstance().logException(e1);
			}
		}
		
		return result[0];
		
	}
	
}
