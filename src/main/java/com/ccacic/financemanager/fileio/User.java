package com.ccacic.financemanager.fileio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.ParamMap;

/**
 * A class representing Users of the system. Statically contains
 * the current User of the system
 * @author Cameron Cacic
 *
 */
public class User {
	
	public static final String NAME = "name";
	public static final String ACCT_HOLD_IDS = "acct_hold_ids";
	public static final String HASHES = "hashes";

	private static User currentUser;
	
	/**
	 * Returns the current User of the system
	 * @return the current User of the system
	 */
	public static User getCurrentUser() {
		return currentUser;
	}
	
	/**
	 * Sets the current User of the system to the User specified
	 * in the passed File
	 * @param userFile the File representing the new current User
	 */
	public static void setCurrentUser(File userFile) {
		try {
			
			if (userFile.exists()) {
				try {
					currentUser = new User(userFile);
				} catch (IllegalArgumentException e) {
					currentUser = null;
				}
			} else {
				PasswordFetcher passwordFetcher = new PasswordFetcher();
				String password = passwordFetcher.fetchNewPassword();
				if (password == null) {
					return;
				}
				userFile.getParentFile().mkdirs();
				userFile.createNewFile();
				currentUser = new User(userFile.getName().substring(0, userFile.getName().lastIndexOf('.')), password);
				FileIO fileIO = new FileIO();
				fileIO.writeToFile(userFile, currentUser.disassemble().encode(), password);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private String name;
	private String password;
	private List<String> acctHoldIds;
	private Map<String, String> hashes;
	private File userDir;
	
	/**
	 * Creates a new User using the passed user file. Should
	 * only be called from the static User methods
	 * @param userFile the user file to build the User from
	 * @throws IOException
	 */
	private User(File userFile) throws IOException {
		
		PasswordFetcher passwordFetcher = new PasswordFetcher();
		password = passwordFetcher.fetchPassword(userFile);
		
		if (password == null) {
			throw new IllegalArgumentException("Failed to get password");
		}
		
		FileIO fileIO = new FileIO();
		ParamMap map = ParamMap.decode(fileIO.loadFile(userFile, null, password));
		
		name = map.get(NAME);
		acctHoldIds = map.getAsList(ACCT_HOLD_IDS);
		
		hashes = new HashMap<>();
		List<String> listHashes = map.getAsList(HASHES);
		for (int i = 0; i < acctHoldIds.size(); i++) {
			hashes.put(acctHoldIds.get(i), listHashes.get(i));
		}
		
		userDir = userFile.getParentFile();
		
		tieToAcctHolderEvents();
		
	}
	
	/**
	 * Creates a new User from the passed name and password.
	 * Should only be called from static User methods
	 * @param name the name of the new User
	 * @param password the password of the new User
	 */
	private User(String name, String password) {
		this.name = name;
		this.password = password;
		this.acctHoldIds = new ArrayList<>();
		this.hashes = new HashMap<>();
		this.userDir = new File(FileHandler.getInstance().getUserDir(), name);
		
		tieToAcctHolderEvents();
	}
	
	/**
	 * Ties this User to relevant events regarding AccountHolders
	 */
	private void tieToAcctHolderEvents() {
		
		EventManager.addListener(this, e -> {
			
			AccountHolder accountHolder = (AccountHolder) e.getData();
			acctHoldIds.add(accountHolder.getIdentifier());
			hashes.put(accountHolder.getIdentifier(), null);
			
		}, Event.NEW_ACCT_HOLDER);
		
		EventManager.addListener(this, e -> {
			
			AccountHolder accountHolder = (AccountHolder) e.getData();
			acctHoldIds.remove(accountHolder.getIdentifier());
			hashes.remove(accountHolder.getIdentifier());
			File toBeDeleted = new File(userDir, accountHolder.getIdentifier());
			toBeDeleted.delete();
			
		}, Event.DELETE_ACCT_HOLDER);
		
	}
	
	/**
	 * Returns the User's name
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the User's password
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns the AccountHolder ID's associated with this User.
	 * Lines up index-wise with the hashes associated with this
	 * User
	 * @return a List of AccountHolder ID's
	 */
	public List<String> getAcctHoldIds() {
		return acctHoldIds;
	}
	
	/**
	 * Returns the hashes of the AccountHolder files associated with
	 * this User. Lines up index-wise with the AccountHolder ID's
	 * associated with this User
	 * @return a List of hashes
	 */
	public Map<String, String> getHashes() {
		return hashes;
	}
	
	/**
	 * Returns the directory of this User
	 * @return the directory of this User
	 */
	public File getUserDir() {
		return userDir;
	}
	
	/**
	 * Returns all the archive files associated with this User
	 * @return the archive files associated with this User
	 */
	public List<File> getArchives() {
		List<File> archives = new ArrayList<>();
		File archiveDir = new File(userDir, "archives");
		for (File file: archiveDir.listFiles()) {
			if (file.getName().endsWith(FileHandler.ARCH_EXTENSION)) {
				archives.add(file);
			}
		}
		return archives;
	}
	
	/**
	 * Updates the hash associated with the passed AccountHolder ID's
	 * @param acctHoldId the AccountHolder ID to update
	 * @param newHash the new hash
	 */
	public void updateHash(String acctHoldId, String newHash) {
		if (hashes.keySet().contains(acctHoldId)) {
			hashes.put(acctHoldId, newHash);
		} else {
			Logger.getInstance().logError("Unknown Account Holder written to user " + name + ": " + acctHoldId);
		}
	}
	
	/**
	 * Disassembles this User into a ParamMap representation
	 * @return a ParamMap representation of this User
	 */
	public ParamMap disassemble() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(NAME, name);
		paramMap.put(ACCT_HOLD_IDS, acctHoldIds);
		List<String> listHashes = new ArrayList<>();
		for (String acctHoldID: acctHoldIds) {
			listHashes.add(hashes.get(acctHoldID));
		}
		paramMap.put(HASHES, listHashes);
		return paramMap;
	}
	
}
