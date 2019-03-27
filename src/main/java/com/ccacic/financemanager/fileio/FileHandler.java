package com.ccacic.financemanager.fileio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.exception.MismatchedHashException;
import com.ccacic.financemanager.launcher.Launcher;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.Category;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.config.GeneralConfig;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.util.StringProcessing;

/**
 * General purpose singleton for handling file operations. Gradually
 * being subdivided into smaller responsibility classes
 * @author Cameron Cacic
 *
 */
public class FileHandler {
	
	public static final String ACCOUNT_PATH = "com.ccacic.financemanager.model.account.";
	public static final String ENTRY_PATH = "com.ccacic.financemanager.model.entry.";
	
	public static final String CONFIG_EXTENSION = ".cfg";
	public static final String DATA_EXTENSION = ".dat";
	public static final String ARCH_EXTENSION = ".arc";
	public static final String TMP_EXTENSION = ".tmp";
	
	private static final FileHandler instance = new FileHandler();
	
	/**
	 * Returns the singelton instance
	 * @return the singleton instance
	 */
	public static FileHandler getInstance() {
		return instance;
	}
	
	private File dataDir;
	private File userDir;
	
	/**
	 * Creates a new FileHandler and finds or creates the
	 * required directories
	 */
	private FileHandler() {		
		
		dataDir = new File(System.getProperty("user.dir") + "\\data");
		if (!dataDir.exists()) {
			dataDir.mkdir();
		}
		
		userDir = new File(dataDir, "\\users");
		if (!userDir.exists()) {
			userDir.mkdir();
		}
		
		EventManager.addListener(this, e -> {
			Account account = (Account) e.getData();
			findAndDelete(User.getCurrentUser().getUserDir(), account.getIdentifier());
		}, Event.DELETE_ACCOUNT);
		
		EventManager.addListener(this, e -> {
			AccountHolder acctHold = (AccountHolder) e.getData();
			findAndDelete(User.getCurrentUser().getUserDir(), acctHold.getIdentifier());
		}, Event.DELETE_ACCT_HOLDER);
		
	}
	
	/**
	 * Returns the general user directory
	 * @return the general user directory
	 */
	public File getUserDir() {
		return userDir;
	}
	
	/**
	 * Returns the data directory
	 * @return the data directory
	 */
	public File getDataDir() {
		return dataDir;
	}
	
	/**
	 * Returns a URL containing the path to the given file in styles
	 * @param style the style file name
	 * @return the URL pointing to the stylesheet
	 */
	public static URL getStyle(String style) {
		return ClassLoader.getSystemClassLoader().getResource("style/" + style); 
	}
	
	/**
	 * Returns a URL containing the path to the given file in layouts
	 * @param layout the layout file name
	 * @return the URL pointing to the fxml file
	 */
	public static URL getLayout(String layout) {
		return ClassLoader.getSystemClassLoader().getResource("layout/" + layout); 
	}
	
	/**
	 * Loads the configuration files into the model
	 * @return if the loading succeeded
	 */
	public boolean loadConfig() {
		
		try {
			File configFile = new File(dataDir, "\\config" + CONFIG_EXTENSION);
			if (!configFile.exists()) {
				configFile.createNewFile();
				Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("config/def_config.cfg"), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			
			Scanner configScan = new Scanner(configFile);
			
			List<String> currParseList = new ArrayList<String>();
			List<String> catParseList = new ArrayList<String>();
			String generalSection = "";
			
			while (configScan.hasNextLine()) {
				String line = configScan.nextLine().trim();
				switch (line) {
				case "CURRENCY":
					loadConfigSection(configScan, currParseList, "CURRENCY");
					break;
				case "CATEGORY":
					loadConfigSection(configScan, catParseList, "CATEGORY");
					break;
				case "GENERAL":
					generalSection += loadConfigSection(configScan, null, "GENERAL");
				default:
					continue;
				}
			}
			configScan.close();
			
			for (String currStr: currParseList) {
				ParamMap paramMap = StringProcessing.pullParamMap(currStr);
				Currency curr = new Currency(paramMap);
				Logger.getInstance().logInfo("Loaded currency " + curr.getCode());
			}
			
			for (String section: catParseList) {
				ParamMap paramMap = StringProcessing.pullParamMap(section);
				Category category = Category.buildCategory(paramMap);
				AccountHolder.addCategory(category);
			}
			
			ParamMap paramMap = StringProcessing.pullParamMap(generalSection);
			GeneralConfig.getInstance().putValues(paramMap);
			
		} catch (Exception e) {
			Logger.getInstance().logException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * Parses each section into the passed List of Strings
	 * @param scan the Scanner to read from
	 * @param parsedSectionList the List to parse into
	 * @param sectionName the name of the section
	 * @return the full output of the Scanner
	 */
	private String loadConfigSection(Scanner scan, List<String> parsedSectionList, String sectionName) {
		String parsedLines = "";
		boolean exit = false;
		while (scan.hasNextLine() && !exit) {
			String line = scan.nextLine().trim();
			if (line.equals("END " + sectionName)) {
				exit = true;
			} else if (line.length() > 0 && line.charAt(0) != '#') {
				parsedLines += line;
			}
		}
		if (parsedSectionList != null) {
			parsedSectionList.add(parsedLines);
		}
		return parsedLines;
	}
	
	/**
	 * Loads all the records in the file system into the model
	 * for the current User. Requires there to be a current User
	 * @return if the loading succeeded
	 */
	public boolean loadRecords() {
		
		User currUser = User.getCurrentUser();
		if (currUser == null) {
			throw new UnsupportedOperationException("Cannot load records without a specified user");
		}
		
		String fileHandlerId = EventManager.getUniqueID(this);
		String[] data = new String[] {fileHandlerId, "Loading records...", "Loading"};
		EventManager.fireEvent(new Event(Event.BLOCKING_PROGRESS_REQUEST, data));
		
		List<AccountHolder> accountHolders = new ArrayList<>();
		double count = currUser.getAcctHoldIds().size() * 2;
		int completed = 0;
		Iterator<String> acctHoldIdIterator = currUser.getAcctHoldIds().iterator();
		Set<String> loadedIds = new HashSet<>();
		while (acctHoldIdIterator.hasNext()) {
			
			String acctHoldId = acctHoldIdIterator.next();
			if (!loadedIds.contains(acctHoldId)) {
				loadedIds.add(acctHoldId);
				File acctHoldFile = new File(currUser.getUserDir(), acctHoldId);
				acctHoldFile = new File(acctHoldFile, acctHoldId + DATA_EXTENSION);
				String expectedHash = currUser.getHashes().get(acctHoldId);
				if (expectedHash == null) {
					acctHoldIdIterator.remove();
				} else {
					try {
						AccountHolder readAcctHolder = readAcctHolder(acctHoldFile, expectedHash);
						if (readAcctHolder == null) {
							acctHoldIdIterator.remove();
							currUser.getHashes().remove(acctHoldId);
						} else {
							accountHolders.add(readAcctHolder);
						}
					} catch (IOException e) {
						Logger.getInstance().logException(e);
					}
					
				}
			} else {
				count -= 2;
				completed--;
			}
			
			completed++;
			EventManager.fireEvent(new Event(Event.UPDATE, completed / count, fileHandlerId));
			
		}
		
		for (AccountHolder accountHolder: accountHolders) {
			Event lock = EventManager.fireEvent(new Event(Event.NEW_ACCT_HOLDER, accountHolder));
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					Logger.getInstance().logWarning("Interrupted while waiting on new account holder to finish");
				}
			}
			completed++;
			EventManager.fireEvent(new Event(Event.UPDATE, completed / count, fileHandlerId));
		}
		
		EventManager.fireEvent(new Event(Event.BLOCKING_PROGRESS_END, fileHandlerId));
		
		return true;
	}
	
	/**
	 * Converts the passed File into an AccountHolder, provided
	 * the hash of the passed File matches the passed expectedHash.
	 * Prompts for confirmation through EventManager if the hash fails
	 * @param acctHoldFile the File to convert
	 * @param expectedHash the expected hash of the File
	 * @return the created AccountHolder
	 * @throws IOException
	 */
	private AccountHolder readAcctHolder(File acctHoldFile, String expectedHash) throws IOException {
		
		if (!acctHoldFile.exists()) {
			return null;
		}
		
		String id = null;
		String name = null;
		String category = null;
		Currency mainCurr = null;
		List<String> acctIds = new ArrayList<>();
		List<String> hashes = new ArrayList<>();
		
		FileIO fileIO = new FileIO();
		String str;
		try {
			str = fileIO.loadFile(acctHoldFile, expectedHash);
		} catch (MismatchedHashException e) {

			Logger.getInstance().logWarning("Expected hash " + e.getExpectedHash() + " for " + acctHoldFile
					+ " did not match its actual hash of " + e.getActualHash());
			
			String[] data = new String[] {EventManager.getUniqueID(this),
					"Account Holder file " + acctHoldFile.getName() + " is potentially corrupted. Load anyway?",
					"Load Corrupted Account"};
			
			EventManager.addListener(null, e2 -> {
				Integer result = (Integer) e2.getData();
				if (result != null && result == 1) {
					try {
						EventManager.fireEvent(new Event(Event.NEW_ACCT_HOLDER, readAcctHolder(acctHoldFile, null)));
					} catch (IOException e1) {
						Logger.getInstance().logException(e1);
					}
				}
			}, Event.CONFIRMATION_RECEIVED, EventManager.getUniqueID(this));
			
			EventManager.fireEvent(new Event(Event.CONFIRMATION_REQUEST, data));
			
			return null;
			
		}
		
		String[][] args = StringProcessing.pullArgs(str);
		int smallLength = Math.min(args[0].length, args[1].length);
		for (int i = 0; i < smallLength; i++) {
			
			String field = args[0][i];
			String value = args[1][i];
			
			switch (field) {
			case "id":
				id = value;
				break;
			case "name":
				name = value;
				break;
			case "category":
				category = value;
				break;
			case "main_curr_code":
				try {
					mainCurr = Currency.getCurrency(value);
				} catch (InvalidCurrencyCodeException e) {
					Logger.getInstance().logError(e.getMessage());
					Launcher.exitImmediately();
				}
				break;
			case "accounts":
				acctIds = StringProcessing.decodeList(value);
				break;
			case "hashes":
				hashes = StringProcessing.decodeList(value);
				break;
			}
		}
		
		AccountHolder aH;
		List<Account> accounts = new ArrayList<>();
		if (AccountHolder.getCategory(category) != null) {
			aH = new AccountHolder(id, name, category, mainCurr, accounts);
		} else {
			return null;
		}
		
		String acctHoldId = EventManager.getUniqueID(aH);
		File acctHoldDir = acctHoldFile.getParentFile();
		for (int i = 0; i < hashes.size(); i++) {
			File acctFile = new File(acctHoldDir, acctIds.get(i));
			acctFile = new File(acctFile, acctIds.get(i) + DATA_EXTENSION);
			Account readAccount = readAccount(acctFile, hashes.get(i), acctHoldId);
			if (readAccount == null) {
				acctIds.remove(i);
				hashes.remove(i);
				i--;
			} else {
				EventManager.fireEvent(new Event(Event.NEW_ACCOUNT, readAccount, acctHoldId));
			}
		}
		
		return aH;
		
	}
	
	/**
	 * Converts the passed File into an Account, provided
	 * the hash of the passed File matches the passed expectedHash.
	 * Prompts for confirmation through EventManager if the hash fails.
	 * Requires the AcountHolder ID of the owning AccountHolder
	 * @param acctFile the file to convert
	 * @param expectedHash the expected hash of the File
	 * @param acctHoldId the ID of the owning AccountHolder
	 * @return the created Account
	 * @throws IOException
	 */
	private Account readAccount(File acctFile, String expectedHash, String acctHoldId) throws IOException {
		
		if (!acctFile.exists()) {
			return null;
		}
		
		FileIO fileIO = new FileIO();
		String acctStr;
		try {
			acctStr = fileIO.loadFile(acctFile, expectedHash);
		} catch (MismatchedHashException e) {
			
			Logger.getInstance().logWarning("Expected hash " + e.getExpectedHash() + " for " + acctFile
					+ " did not match its actual hash of " + e.getActualHash());
			
			String[] data = new String[] {EventManager.getUniqueID(this),
					"Account file " + acctFile.getName() + " is potentially corrupted. Load anyway?",
					"Load Corrupted Account"};
			
			EventManager.addListener(null, e2 -> {
				Integer result = (Integer) e2.getData();
				if (result != null && result == 1) {
					try {
						EventManager.fireEvent(new Event(Event.NEW_ACCOUNT, readAccount(acctFile, null, acctHoldId), acctHoldId));
					} catch (IOException e1) {
						Logger.getInstance().logException(e1);
					}
				}
			}, Event.CONFIRMATION_RECEIVED, EventManager.getUniqueID(this));
			
			EventManager.fireEvent(new Event(Event.CONFIRMATION_REQUEST, data));
			
			return null;
			
		}
		
		ParamMap acctMap = ParamMap.decode(acctStr);
		acctMap.put(AccountAssembler.ACCT_HOLD_ID, acctHoldId);
		AccountFactory factory = AccountFactory.getInstance();
		Account account = factory.requestItem(acctMap);
		return account;
		
	}
	
	/**
	 * Writes the current state of the model to the file system
	 * @return if the write succeeded
	 */
	public boolean writeFiles() {

		if (User.getCurrentUser() == null) {
			return false;
		}
		
		for (AccountHolder aH: AccountHolder.getAccountHolders()) {
			User.getCurrentUser().updateHash(aH.getIdentifier(), writeAcctHolder(aH));
		}
		FileIO fileIO = new FileIO();
		
		try {
			
			User currUser = User.getCurrentUser();
			File userFile = new File(userDir, currUser.getName());
			userFile = new File(userFile, currUser.getName() + DATA_EXTENSION);
			String encodedUser = currUser.disassemble().encode();
			fileIO.writeToFile(userFile, encodedUser);
			
			File configFile = new File(dataDir, "config" + CONFIG_EXTENSION);
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
			
			writer.write("GENERAL");
			writer.newLine();
			for (String line: encodeParamMapForConfig(GeneralConfig.getInstance().getValueMap())) {
				writer.write(line);
				writer.newLine();
			}
			writer.write("END GENERAL");
			writer.newLine();
			writer.newLine();
			
			for (Category category: AccountHolder.getAllCategories()) {
				writer.write("CATEGORY");
				writer.newLine();
				for (String line: encodeParamMapForConfig(category.encode())) {
					writer.write(line);
					writer.newLine();
				}
				writer.write("END CATEGORY");
				writer.newLine();
				writer.newLine();
			}
			
			for (Currency currency: Currency.getAllCurrencies()) {
				writer.write("CURRENCY");
				writer.newLine();
				for (String line: encodeParamMapForConfig(currency.encode())) {
					writer.write(line);
					writer.newLine();
				}
				writer.write("END CURRENCY");
				writer.newLine();
				writer.newLine();
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Writes the passed AccountHolder to the file system
	 * @param aH the AccountHolder to write
	 * @return the hash of the written file
	 */
	private String writeAcctHolder(AccountHolder aH) {
		
		try {
				
			File acctHoldDir = new File(User.getCurrentUser().getUserDir(), aH.getIdentifier());
			if (!acctHoldDir.exists()) {
				acctHoldDir.mkdir();
			}
			
			String encoded = "id=" + aH.getIdentifier() + ";"
				+ "name=" + aH.getName() + ";"
				+ "category=" + aH.getCategory() + ";"
				+ "main_curr_code=" + aH.getMainCurr().getCode() + ";"
				+ "accounts={";
			
			List<String> hashes = new ArrayList<>();
			for (int i = 0; i < aH.getAccounts().size(); i++) {
				
				Account a = aH.getAccounts().get(i);
				String hash = writeAccount(acctHoldDir, a);
				if (hash != null) {
					hashes.add(hash);
					encoded += a.getIdentifier();
					if (i < aH.getAccounts().size() - 1) {
						encoded += ",";
					}
				}
				
			}
			encoded += "};hashes={";
			for (int i = 0; i < hashes.size(); i++) {
				encoded += hashes.get(i);
				if (i < hashes.size() - 1) {
					encoded += ",";
				}
			}
			encoded += "};";
			
			File acctHoldFile = new File(acctHoldDir, aH.getIdentifier() + DATA_EXTENSION);
			FileIO fileIO = new FileIO();
			return fileIO.writeToFile(acctHoldFile, encoded);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Writes the passed Account to the passed directory,
	 * which should be the directory of the AccountHolder
	 * that owns the Account
	 * @param acctHoldDir the directory to write to
	 * @param a the Account to write
	 * @return the hash of the written file
	 */
	private String writeAccount(File acctHoldDir, Account a) {
		
		try {
			
			File acctDir = new File(acctHoldDir, a.getIdentifier());
			if (!acctDir.exists()) {
				acctDir.mkdir();
			}
			
			AccountFactory accountFactory = AccountFactory.getInstance();
			ParamMap acctMap = accountFactory.requestDisassembly(a);

			File acctFile = new File(acctDir, a.getIdentifier() + DATA_EXTENSION);
			FileIO fileIO = new FileIO();
			return fileIO.writeToFile(acctFile, acctMap.encode());
			
		} catch (Exception ex) {
			Logger.getInstance().logException(ex);
		}
		
		return null;
	}
	
	/**
	 * Converts a ParamMap encoding to a config style format
	 * @param paramMap the ParamMap to encode
	 * @return a List of Strings representing each line of a config file
	 */
	private List<String> encodeParamMapForConfig(ParamMap paramMap) {
		List<String> list = new ArrayList<>();
		for (String key: paramMap.keySet()) {
			list.add(key + " = " + paramMap.get(key) + ";");
		}
		return list;
	}
	
	/**
	 * Finds the File with the name matching the passed String
	 * and deletes it. Only searches within the passed directory
	 * @param searchDir the directory to search within
	 * @param target the name of the File to delete
	 */
	private void findAndDelete(File searchDir, String target) {
		File[] candidates = searchDir.listFiles();
		if (candidates != null) {
			for (File candidate: candidates) {
				if (candidate.getName().equals(target)) {
					deleteFile(candidate);
				} else {
					if (candidate.isDirectory()) {
						findAndDelete(candidate, target);
					}
				}
			}
		}
			
	}
	
	/**
	 * Deletes the passed File and any Files within it
	 * if the File is a directory
	 * @param file the File to delete
	 */
	private void deleteFile(File file) {
		
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f: files) {
					deleteFile(f);
				}
			}
		}
		
		file.delete();
		
	}
	
	/**
	 * Dumps the current model in memory and deletes it from the file system
	 */
	public void dumpAndDelete() {
		
		ReadOnlyList<AccountHolder> deletionLedger = new ReadOnlyList<>(AccountHolder.getAccountHolders());
		for (AccountHolder acctHold: deletionLedger) {
			String id = EventManager.getUniqueID(acctHold);
			Object lock = EventManager.fireEvent(new Event(Event.DELETE_ACCT_HOLDER, acctHold, id));
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					Logger.getInstance().logWarning("Interrupted while waiting on event lock while dumping and deleting");
				}
			}
		}
		
		assert User.getCurrentUser().getAcctHoldIds().isEmpty();
		
		AccountHolder.clear();
	}

}
