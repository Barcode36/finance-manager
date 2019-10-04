package com.ccacic.financemanager.model.entrychunk;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.event.ChangeEvent;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.EntryFileIO;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.Unique;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryAssembler;

/**
 * The third layer of the model. This layer is typically invisible to
 * the user, as users do not directly create EntryChunks. That job is
 * relegated to the EntryChunkManager. EntryChunks keep track of the
 * Entries put into it, but with the twist that the Entries are not
 * always kept in memory. EntryChunk tracks a source file and a
 * temporary file that contain the original Entries and the modified
 * Entries, respectively. Entries are only placed in memory when they
 * are requested, at which point the unmodifiable List returned to
 * the requester is tracked through a SoftReference. When the
 * SoftReference is garbage collected, the chunk's reference to the
 * modifiable Entries List in memory is nullified. Since the contract
 * for SoftReference is to be garbage collected only as needed, this
 * allows the Entries to remain in memory for as long as there is
 * room for them. For very large records, Entries will be the vast
 * majority of the memory footprint, so storing Entries in this way
 * will prevent very large records from requiring a huge amount of
 * memory. Before the chunk's Entries List is nullified, it is
 * written to the temporary file if any changes have occured to the
 * Entries. This temporary file is written to the source file upon
 * saving the model, and is deleted at the end of the program's life.
 * 
 * EntryChunks have the additional use of breaking up Accounts into
 * smaller chunks of Entries. How the Entries managed by an Account
 * are to be broken up into chunks is the perview of the
 * EntryChunkManager. The benefit of this is that Accounts with
 * extremely high amounts of Entries will not need to have all their
 * Entries loaded from the file system into memory every time the
 * Account is opened: a potentially extremely time consuming
 * venture.
 * 
 * For subclasses, note that the fields entryCount and total,
 * accesible through their respective getter methods, are calculated
 * by updating their values whenever an Entry is added or removed
 * from the chunk. Thus, changes made to the Entries the chunk tracks
 * should ALWAYS be done through the addEntry and removeEntry
 * methods for structural changes and have a ChangeEvent with a
 * proper Delta fired for value changes. Otherwise, it is up to the
 * subclass to maintain these values
 * @author Cameron Cacic
 *
 */
public class EntryChunk extends Unique implements EventListener {
	
	private SoftReference<ReadOnlyList<Entry>> entriesReadOnlyRef;
	private final ReferenceQueue<ReadOnlyList<Entry>> referenceQueue;
	private List<Entry> entries;
	private final Object entriesLock = new Object();
	private boolean useTmp;
	private boolean changed;
	
	private final File sourceFile;
	private final File tmpFile;
	private String expectedSrcHash;
	private String expectedTmpHash;
	private final EntryFileIO entryFileIO;
	
	private Thread fileDumpThread;
	
	private double total;
	private int entryCount;
	
	private LocalDateTime earliest;
	private LocalDateTime latest;
	private boolean empty;
	
	/**
	 * Creates a new EntryChunk with the passed File as the source file to source
	 * Entries from and check against the passed expected hash. Should be called
	 * to recreate an EntryChunk from the file system
	 * @param entryChunkFile the File to source Entries from
	 * @param expectedHash the expected hash of the source file
	 */
	public EntryChunk(File entryChunkFile, String expectedHash) {
		setIdentifier(entryChunkFile.getName().substring(0, entryChunkFile.getName().lastIndexOf('.')));
		
		entryFileIO = new EntryFileIO();
		
		this.sourceFile = entryChunkFile;
		String tmpName = "tmp$" + getIdentifier() + FileHandler.TMP_EXTENSION;
		this.tmpFile = new File(sourceFile.getParentFile(), tmpName);
		
		this.expectedSrcHash = expectedHash;
		this.referenceQueue = new ReferenceQueue<>();
		this.useTmp = tmpFile.exists();
		this.changed = false;
		
		total = 0.0;
		try {
			entries = new ArrayList<>();
			entriesReadOnlyRef = new SoftReference<>(new ReadOnlyList<>(entries), referenceQueue);
			List<Entry> loadedEntries = entryFileIO.loadEntries(sourceFile, expectedHash);
			for (Entry entry: loadedEntries) {
				addEntry(entry, false);
			}
			entryCount = entries.size();
		} catch (IOException e) {
			Logger.getInstance().logException(e);
			entriesReadOnlyRef = new SoftReference<>(null);
		}
		empty = false;
		
		fileDumpThread = new Thread(getFileDump());
		fileDumpThread.start();
		
	}
	
	/**
	 * Creates a new EntryChunk inside the passed directory and with the passed
	 * Entry as the first Entry it is charged with keeping track of. Should be
	 * used to create brand new EntryChunks that are not yet in the file system
	 * @param entryChunkDirectory the directory to make the EntryChunk in
	 * @param firstEntry the firstr Entry to keep track of
	 */
	public EntryChunk(File entryChunkDirectory, Entry firstEntry) {
		
		entryFileIO = new EntryFileIO();
		
		sourceFile = new File(entryChunkDirectory, getIdentifier() + FileHandler.DATA_EXTENSION);
		expectedSrcHash = null;
		String tmpName = "tmp$" + getIdentifier() + FileHandler.TMP_EXTENSION;
		tmpFile = new File(entryChunkDirectory, tmpName);
		
		referenceQueue = new ReferenceQueue<>();
		useTmp = true;
		changed = false;
		
		entries = new ArrayList<>();
		entries.add(firstEntry);
		empty = false;
		try {
			expectedTmpHash = entryFileIO.writeEntries(tmpFile, entries);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		entries.clear();
		entriesReadOnlyRef = new SoftReference<>(new ReadOnlyList<>(entries), referenceQueue);
		addEntry(firstEntry, false);
		
		fileDumpThread = new Thread(getFileDump());
		fileDumpThread.start();
	}
	
	/**
	 * Gets an instance of the Runnable that handles dumping the entries list
	 * to the file system
	 * @return the file dump Runnable
	 */
	private Runnable getFileDump() {
		return () -> {
			WeakReference<EntryChunk> parent = new WeakReference<>(this);
			boolean interrupted = false;
			while (parent.get() != null && !interrupted) {
				
				try {
					referenceQueue.remove();
					// wait until work being done on entries is finished
					synchronized (entriesLock) {
						if (changed) {
							expectedTmpHash = entryFileIO.writeEntries(tmpFile, entries);
						}
						changed = false;
						entries = null;
						useTmp = true;
						interrupted = Thread.interrupted();
					}
				} catch (InterruptedException | IOException e) {
					interrupted = true;
				}
				
			}
		};
	}
	
	/**
	 * Adds the passed Entry to the EntryChunk, with the passed
	 * isNewEntry boolean dictating if a NEW_ENTRY event should
	 * be fired at the conclusion of adding the Entry
	 * @param entry the Entry to add
	 * @param isNewEntry if a NEW_ENTRY event should be fired
	 */
	private void addEntry(Entry entry, boolean isNewEntry) {
		
		// prevents the fileDumpThread from nullifying entries
		synchronized (entriesLock) {
			
			// makes sure entries is nonnull
			ReadOnlyList<Entry> reference = getEntries();
			
			changed |= entries.add(entry);
			total += entry.getAmount();
			entryCount++;
			
			if (earliest == null || latest == null) {
				earliest = entry.getDateTime();
				latest = entry.getDateTime();
			} else {
				if (earliest.isAfter(entry.getDateTime())) {
					earliest = entry.getDateTime();
				} else if (latest.isBefore(entry.getDateTime())) {
					latest = entry.getDateTime();
				}
			}
			
			addEntryFinalize(entry);
			
			String id = EventManager.getUniqueID(this);
			if (isNewEntry) {
				EventManager.fireEvent(new Event(Event.NEW_ENTRY, entry, id));
			}
			String entryId = EventManager.getUniqueID(entry);
			EventManager.addListener(entry, this, Event.UPDATE, entryId);
		}
		
	}
	
	/**
	 * Adds the passed Entry to the EntryChunk
	 * @param entry the Entry to add
	 */
	final void addEntry(Entry entry) {
		addEntry(entry, true);
	}
	
	/**
	 * Called at the end of adding a new Entry to the EntryChunk
	 * but before any Events are fired. Intended to be overridden
	 * by subclasses to add new functionality to addEntry as
	 * opposed to directly overriding addEntry to avoid
	 * potential protected/private method overloading weirdness
	 * @param entry the Entry to finalize adding to
	 */
	protected void addEntryFinalize(Entry entry) {
		// placeholder for being overridden
	}
	
	/**
	 * Removes the Entry from the EntryChunk
	 * @param entry the Entry to remove
	 */
	final void removeEntry(Entry entry) {
		
		// prevents fileDumpThread from nullifying entries
		synchronized (entriesLock) {
			
			// makes sure entries is nonnull
			ReadOnlyList<Entry> reference = getEntries();
			
			changed = entries.remove(entry);
			if (changed) {
				total -= entry.getAmount();
				entryCount--;

				if (!reference.isEmpty()) {
					if (earliest.isEqual(entry.getDateTime())) {
						earliest = LocalDateTime.MAX;
						for (Entry e : reference) {
							if (earliest.isAfter(e.getDateTime())) {
								earliest = e.getDateTime();
							}
						}
					}
					if (latest.isEqual(entry.getDateTime())) {
						latest = LocalDateTime.MIN;
						for (Entry e : reference) {
							if (latest.isBefore(e.getDateTime())) {
								latest = e.getDateTime();
							}
						}
					}
				} else {
					earliest = null;
					latest = null;
					empty = true;
				}

				removeEntryFinalize(entry);
				
				String id = EventManager.getUniqueID(this);
				EventManager.fireEvent(new Event(Event.DELETE_ENTRY, entry, id));

				if (entries.isEmpty()) {
					Logger.getInstance().logDebug("Entry chunk " + getIdentifier() + " is empty, deleting files");
					fileDumpThread.interrupt();
					try {
						fileDumpThread.join();
					} catch (InterruptedException e) {
						Logger.getInstance().logException(e);
					}
					if (!tmpFile.delete()) {
						Logger.getInstance().logWarning("Failed to delete temporary file " + tmpFile);
					}
					if (!sourceFile.delete()) {
						Logger.getInstance().logWarning("Failed to delete source file " + sourceFile);
					}
				}
			}
		}
		
	}
	
	/**
	 * Called at the end of removing an Entry from the EntryChunk
	 * but before any Events are fired and the EntryChunk is
	 * checked for deletion. Intended to be overridden to add new
	 * functionality to removeEntry as opposed to overriding
	 * removeEntry
	 * @param entry the Entry being removed
	 */
	protected void removeEntryFinalize(Entry entry) {
		// placeholder for being overridden
	}
	
	/**
	 * Returns an unmodifiable List of Entries managed by this EntryChunk
	 * @return an unmodifiable List of Entries
	 */
	public ReadOnlyList<Entry> getEntries() {
		
		ReadOnlyList<Entry> readOnlyEntries;
		
		synchronized (entriesLock) {
			
			readOnlyEntries = entriesReadOnlyRef.get();
			if (readOnlyEntries == null) {

				try {

					fileDumpThread.interrupt();
					fileDumpThread.join();

					if (useTmp) {
						entries = entryFileIO.loadEntries(tmpFile, expectedTmpHash);
					} else {
						entries = entryFileIO.loadEntries(sourceFile, expectedSrcHash);
					}
					readOnlyEntries = new ReadOnlyList<>(entries);
					entriesReadOnlyRef = new SoftReference<>(readOnlyEntries, referenceQueue);
					fileDumpThread = new Thread(getFileDump());
					fileDumpThread.start();

				} catch (IOException | InterruptedException e) {
					Logger.getInstance().logException(e);
					return null;
				}

			}
		}
		
		return readOnlyEntries;
		
	}
	
	/**
	 * Commits the changes stored in the temporary file to the source file,
	 * along with any changes still in memory. The temporary file is also
	 * deleted as it is no longer needed
	 * @return the new hash of the source file
	 */
	public String commitChanges() {
		
		try {
			
			synchronized (entriesLock) {
				ReadOnlyList<Entry> reference = getEntries();
				fileDumpThread.interrupt();
				fileDumpThread.join();
				expectedSrcHash = entryFileIO.writeEntries(sourceFile, entries);
				useTmp = false;
				changed = false;
				if (!tmpFile.delete()) {
					Logger.getInstance().logWarning("Failed to delete temp file " + tmpFile);
				}
			}
			
		} catch (InterruptedException | IOException e) {
			Logger.getInstance().logException(e);
			return null;
		}
		
		return expectedSrcHash;
		
	}
	
	/**
	 * Returns the source file
	 * @return the source file
	 */
	public File getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * Returns the temporary file
	 * @return the temporary file
	 */
	public File getTmpFile() {
		return tmpFile;
	}
	
	/**
	 * Returns the total of all the Entries in the EntryChunk 
	 * @return the total
	 */
	public double getTotal() {
		return total;
	}
	
	/**
	 * Returns how many Entries are managed by the EntryChunk
	 * @return the Entry count
	 */
	public int getEntryCount() {
		return entryCount;
	}
	
	/**
	 * Returns the earliest LocalDateTime of the Entry with the
	 * earliest date and time managed by the EntryChunk
	 * @return the earliest LocalDateTime
	 */
	public LocalDateTime getEarliest() {
		return earliest;
	}
	
	/**
	 * Returns the latest LocalDateTime of the Entry with the
	 * latest date and time managed by the EntryChunk
	 * @return the latest LocalDateTime
	 */
	public LocalDateTime getLatest() {
		return latest;
	}
	
	/**
	 * Checks if the EntryChunk is empty. Empty EntryChunks
	 * should be discarded as an empty EntryChunk will have
	 * has its backing Files removed from the file system
	 * @return if the EntryChunk is empty
	 */
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public void onEvent(Event event) {
		Delta delta = (Delta) event.getData();
		total += delta.getNewValueAsDouble(EntryAssembler.AMOUNT) - delta.getOldValueAsDouble(EntryAssembler.AMOUNT);
		String id = EventManager.getUniqueID(this);
		EventManager.fireEvent(new ChangeEvent(delta, id));
	}

}
