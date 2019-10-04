package com.ccacic.financemanager.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.launcher.Launcher;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.Unique;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.config.GeneralConfig;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryFactory;
import com.ccacic.financemanager.model.entrychunk.DateResolution;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.util.StringProcessing;

/**
 * Utility class for creating and loading archives. Archives are
 * single file representations of the state of the model. When
 * created, they are named with the current date and time. Must
 * have its register() method invoked before it will work with
 * EventManager. Pertinent Event types are LOAD_ARCHIVE_REQUEST,
 * which loads the most recent archive, and SAVE_ARCHIVE_REQUEST,
 * which creates a new archive of the current model state
 * @author Cameron Cacic
 *
 */
public class Archiver {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH-mm-ss'.arc'");
	
	private static final String ENTRIES = "entries";

	private static boolean registered = false;

	/**
	 * Registers the Archiver to work with EventManager events
	 */
	public static void register() {
		if (!registered) {
			
			EventManager.addListener(null, e -> loadMostRecentArchive(), Event.LOAD_ARCHIVE_REQUEST);
			
			EventManager.addListener(null, e -> createArchive(), Event.SAVE_ARCHIVE_REQUEST);

			registered = true;
			
		}
	}
	
	/**
	 * Loads the most recent archive into the model, dumping anything
	 * currently stored there
	 * @return if the archive was successfully loaded
	 */
	private static boolean loadMostRecentArchive() {
		
		// the archive directory for the current User
		File archiveDir = new File(User.getCurrentUser().getUserDir(), "archives");
		
		if (!archiveDir.exists() || !archiveDir.isDirectory()) {
			throw new IllegalArgumentException(archiveDir + " does not exist or is not a directory, failed to find archive directory");
		}
		
		// finds the most recent archive
		File mostRecentArchive = null;
		LocalDateTime mostRecent = LocalDateTime.MIN;
		FileFilter filter = f -> f.getName().endsWith(".arc");
		for (File archive: Objects.requireNonNull(archiveDir.listFiles(filter))) {
			LocalDateTime time = LocalDateTime.parse(archive.getName(), FORMATTER);
			if (time.isAfter(mostRecent)) {
				mostRecent = time;
				mostRecentArchive = archive;
			}
		}
		
		if (mostRecentArchive == null) {
			Logger.getInstance().logWarning("Failed to find the most recent archive to load from");
			return false;
		}
		
		return loadArchive(mostRecentArchive);
		
	}
	
	/**
	 * Loads the passed File as an archive file into the model. Dumps
	 * the current model as a result, along with all files associated with it.
	 * throws an IllegalArgumentException if the passed file is not a .arc File
	 * @param archiveFile the passed archive File
	 * @return if the archive was successfully loaded
	 */
	private static boolean loadArchive(File archiveFile) {
		
		// verifies that the file is a .arc
		String extension = archiveFile.getName().substring(archiveFile.getName().lastIndexOf('.'));
		if (archiveFile.isDirectory() || !FileHandler.ARCH_EXTENSION.equals(extension)) {
			throw new IllegalArgumentException(archiveFile + " is not a " + FileHandler.ARCH_EXTENSION + " file");
		}
		
		// tells the EventManager to block user actions while the archive is being loaded
		Object eventIdLock = new Object();
		String archiverId = EventManager.getUniqueID(eventIdLock);
		String[] data = new String[] {archiverId, "Loading archive " + archiveFile.getName(), "Loading Archive"};
		Event lock = EventManager.fireEvent(new Event(Event.BLOCKING_PROGRESS_REQUEST, data));
		synchronized (Objects.requireNonNull(lock)) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				Logger.getInstance().logWarning(
						"Interrupted while loading archive waiting for blocking progress to finish");
			}
		}
		EventManager.fireEvent(new Event(Event.UPDATE, -1.0, archiverId));
		
		// begin trying to load the archive
		try {
			
			FileInputStream recordFileInputStream = new FileInputStream(archiveFile);
			
			String readFile;
			
			if (GeneralConfig.getInstance().isEncrypted()) {
				
				// the archive is encrypted, so it must be decrypted first before it's read in
				String password = User.getCurrentUser().getPassword();
				if (password == null) {
					recordFileInputStream.close();
					return false;
				}
				Encryption encryption = new Encryption(recordFileInputStream, password);
				byte[] decryptedData = encryption.getDataDecrypted();
				recordFileInputStream.close();
				
				if (decryptedData == null) {
					// failed decryption
					return false;
				} else {
					readFile = new String(decryptedData, StandardCharsets.UTF_8);
					Logger.getInstance().logDebug("Password accepted, decryption check passed");
				}
				
				
			} else {
				
				// the archive is unencrypted, so just read it in
				BufferedReader reader = new BufferedReader(new InputStreamReader(recordFileInputStream, StandardCharsets.UTF_8));
				StringBuilder readFileBuilder = new StringBuilder();
				while (reader.ready()) {
					readFileBuilder.append(reader.readLine());
				}
				readFile = readFileBuilder.toString();
				reader.close();
				
			}
			
			int index = readFile.indexOf("~ACCOUNT_HOLDER");
			List<AccountHolder> accountHolders = new ArrayList<>();
			while (index > -1) {
				String acctHoldStr = StringProcessing.pullBracketSection(readFile, index);
				accountHolders.add(readAcctHolder(acctHoldStr));
				index = readFile.indexOf("~ACCOUNT_HOLDER", index + Objects.requireNonNull(acctHoldStr).length() + 1);
			}

			// deletes the current model and all files associated with it
			FileHandler.getInstance().dumpAndDelete();
			
			for (AccountHolder accountHolder: accountHolders) {
				lock = EventManager.fireEvent(new Event(Event.NEW_ACCT_HOLDER, accountHolder));
				synchronized (Objects.requireNonNull(lock)) {
					lock.wait();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// finish the blocking progress
			EventManager.fireEvent(new Event(Event.BLOCKING_PROGRESS_END, archiverId));
		}
		
		return true;
	}
	
	/**
	 * Converts the passed String to an AccountHolder. Only works
	 * for the archived String representation of an AccountHolder
	 * @param str the AccountHolder as a String to read
	 * @return the read AccountHolder
	 */
	private static AccountHolder readAcctHolder(String str) {
		
		String id = null;
		String name = null;
		String category = null;
		Currency mainCurr = null;
		List<Account> accounts = new ArrayList<>();
		String accountsString = null;
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
				if (id != null) {
					
					AccountFactory accountFactory = AccountFactory.getInstance();
					List<ParamMap> paramMaps = ParamMap.decodeList(value);
					for (ParamMap paramMap: paramMaps) {
						
						paramMap.put(AccountAssembler.ENTRY_CHUNK_HASHES, "{}");
						paramMap.put(AccountAssembler.ENTRY_CHUNK_IDS, "{}");
						paramMap.put(AccountAssembler.ACCT_HOLD_ID, id);
						paramMap.put(AccountAssembler.DATE_RESOLUTION, DateResolution.ANNUALY.name());
						
						Account account = accountFactory.requestItem(paramMap);
						List<ParamMap> entryMaps = ParamMap.decodeList(StringProcessing.pullBracketSection(paramMap.get(ENTRIES), 0));
						EntryFactory entryFactory = EntryFactory.getInstance();
						List<Entry> entries = entryFactory.requestItems(entryMaps);
						account.getEntryChunkManager().addAllEntries(entries);
						accounts.add(account);
						
					}
					
				} else {
					accountsString = value;
				}
				break;
			}
		}
		
		if (accountsString != null) {
			
			if (id == null) {
				id = Unique.genUUID();
			}
			
			AccountFactory accountFactory = AccountFactory.getInstance();
			List<ParamMap> paramMaps = ParamMap.decodeList(accountsString);
			for (ParamMap paramMap: paramMaps) {
				
				paramMap.put(AccountAssembler.ENTRY_CHUNK_HASHES, "{}");
				paramMap.put(AccountAssembler.ENTRY_CHUNK_IDS, "{}");
				paramMap.put(AccountAssembler.ACCT_HOLD_ID, id);
				paramMap.put(AccountAssembler.DATE_RESOLUTION, DateResolution.ANNUALY.name());
				
				Account account = accountFactory.requestItem(paramMap);
				List<ParamMap> entryMaps = ParamMap.decodeList(StringProcessing.pullBracketSection(paramMap.get(ENTRIES), 0));
				EntryFactory entryFactory = EntryFactory.getInstance();
				List<Entry> entries = entryFactory.requestItems(entryMaps);
				account.getEntryChunkManager().addAllEntries(entries);
				accounts.add(account);
				
			}
		}
		
		if (AccountHolder.getCategory(category) != null) {
			return new AccountHolder(id, name, category, mainCurr, accounts);
		} else {
			return null;
		}
		
	}
	
	/**
	 * Converts the current state of the model into an archive named with the
	 * current date and time. Does not affect the current state of the model
	 * @return if the conversion succeeded
	 */
	private static boolean createArchive() {
		
		try {
			
			File archiveDir = new File(User.getCurrentUser().getUserDir(), "archives");
			if (!archiveDir.exists()) {
				if(!archiveDir.mkdir()) {
					throw new IOException("Creation of " + archiveDir + " failed");
				}
			}
			
			LocalDateTime currTime = LocalDateTime.now();
			String archiveName = String.format("%02d", currTime.getMonthValue()) + "-" + String.format("%02d", currTime.getDayOfMonth()) + "-"
					+ currTime.getYear() + " " + String.format("%02d", currTime.getHour()) + "-" + String.format("%02d", currTime.getMinute())
					+ "-" + String.format("%02d", currTime.getSecond()) + ".arc";
			File archiveFile = new File(archiveDir.getAbsolutePath(), archiveName);
			if (archiveFile.createNewFile()) {
				throw new IOException("Failed to create" + archiveFile);
			}
			
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteOutStream, StandardCharsets.UTF_8));
			writeAcctHolders(writer);
			writer.close();
			
			byte[] data = byteOutStream.toByteArray();
			FileIO fileIO = new FileIO();
			fileIO.writeToFile(archiveFile, data);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Writes all AccountHolders currently in the model into a String
	 * format and passing them into the passed Writer
	 * @param writer the Writer to pass the AccountHolder String to
	 * @return if the conversion to a String succeeded
	 */
	private static boolean writeAcctHolders(BufferedWriter writer) {
		try {
			for (AccountHolder aH: AccountHolder.getAccountHolders()) {
				writer.write("~ACCOUNT_HOLDER{");
				writer.write("id=" + aH.getIdentifier() + ";");
				writer.write("name=" + aH.getName() + ";");
				writer.write("category=" + aH.getCategory() + ";");
				writer.write("main_curr_code=" + aH.getMainCurr().getCode() + ";");
				writer.write("accounts={");
				if (aH.getAccounts().isEmpty()) {
					writer.write("");
				} else {
					for (Account a: aH.getAccounts()) {
						writeAccount(writer, a);
					}
				}
				writer.write("};");
				writer.write("}");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Writes the passed Account into a String format and passes it
	 * into the passed Writer
	 * @param writer the Writer to pass the Account String to
	 * @param a the Account to convert
	 * @return if the conversion succeeded
	 */
	private static boolean writeAccount(BufferedWriter writer, Account a) {
		try {
			AccountFactory accountFactory = AccountFactory.getInstance();
			ParamMap paramMap = accountFactory.requestDisassembly(a);
			paramMap.remove(AccountAssembler.ACCT_HOLD_ID);
			paramMap.remove(AccountAssembler.ENTRY_CHUNK_HASHES);
			paramMap.remove(AccountAssembler.ENTRY_CHUNK_IDS);
			paramMap.remove(AccountAssembler.DATE_RESOLUTION);
			List<String> entries = new ArrayList<>();
			EntryFactory entryFactory = EntryFactory.getInstance();
			for (EntryChunk chunk: a.getEntryChunks()) {
				for (Entry entry: chunk.getEntries()) {
					entries.add(entryFactory.requestDisassembly(entry).encode());
				}
			}
			paramMap.put(ENTRIES, entries);
			writer.write(paramMap.encode());
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

}
