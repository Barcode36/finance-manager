package com.ccacic.financemanager.model.entrychunk;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.fileio.User;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.entry.Entry;

/**
 * In charge of creating, managing, and deleting EntryChunks. Operates at a given
 * DateResolution, dividing Entries among EntryChunks by ranges specified by the
 * extremes of the DateResolution. If the amount of Entries in a single chunk
 * exceeds a threshold value, then the chunk should be broken up into new chunks
 * each with a narrower range, or if no narrower range can be created at the given
 * DateResolution, then the DateResolution itself will be narrowed
 * @author Cameron Cacic
 *
 */
public class EntryChunkManager {
	
	public static final int MIN_ENTRIES = 5;
	public static final int SPLIT_THRESHOLD = 100;
	
	private final RangeMap<LocalDateTime, EntryChunk> chunkMap;
	private final DateResolutionManager resolutionManager;
	private File entryChunkDirectory;
	private final EntryChunkProducer producer;
	
	/**
	 * Creates a new EntryChunkManager
	 * @param entryChunkIdToHashMap maps the IDs of the EntryChunks it manages to the expected
	 * hashes of their source files
	 * @param acctHoldId the ID of the AccountHolder that owns the manager
	 * @param acctId the ID of the Account that owns the manager
	 * @param resolution the DateResolution to begin operating at
	 * @param producer the EntryChunkProducer to instantiate EntryChunks from
	 */
	public EntryChunkManager(Map<String, String> entryChunkIdToHashMap, String acctHoldId, String acctId,
			DateResolution resolution, EntryChunkProducer producer) {
		
		this.producer = producer;
		
		chunkMap = new RangeMap<>();
		resolutionManager = new DateResolutionManager(resolution);
		
		entryChunkDirectory = new File(User.getCurrentUser().getUserDir(), acctHoldId);
		entryChunkDirectory = new File(entryChunkDirectory, acctId);
		if (!entryChunkDirectory.exists() && !entryChunkIdToHashMap.isEmpty()) {
			throw new IllegalArgumentException("Entry chunk directory " + entryChunkDirectory.getAbsolutePath() + " does not exist");
		}
		
		for (String chunkId: entryChunkIdToHashMap.keySet()) {
			
			File entryChunkFile = new File(entryChunkDirectory, chunkId + FileHandler.DATA_EXTENSION);
			EntryChunk chunk = producer.createEntryChunk(entryChunkFile, entryChunkIdToHashMap.get(chunkId));
			
			if (chunk.getEntries() == null || chunk.getEntries().isEmpty()) {
				Logger.getInstance().logWarning("Empty entry chunk " + entryChunkFile.getName() + " encountered and removed");
				if (!entryChunkFile.delete()) {
					Logger.getInstance().logWarning("Failed to delete entry chunk file " + entryChunkFile);
				}
				continue;
				//throw new IllegalArgumentException("Entry chunks are never allowed to be empty");
			}
			
			LocalDateTime min = LocalDateTime.MAX;
			LocalDateTime max = LocalDateTime.MIN;
			for (Entry entry: chunk.getEntries()) {
				if (entry.getDateTime().isBefore(min)) {
					min = entry.getDateTime();
				}
				if (entry.getDateTime().isAfter(max)) {
					max = entry.getDateTime();
				}
			}
			
			LocalDateTime[] resolvedRange = resolutionManager.getResolvedRange(min, max);
			final LocalDateTime finalMin = resolvedRange[0];
			final LocalDateTime finalMax = resolvedRange[1];
			chunkMap.put(finalMin, finalMax, chunk);
			
			String id = EventManager.getUniqueID(this);
			String chunkEventId = EventManager.getUniqueID(chunk);
			EventManager.addListener(chunk, e -> {
				for (Entry entry: chunk.getEntries()) {
					if (entry.getDateTime().isBefore(finalMin) || entry.getDateTime().isAfter(finalMax)) {
						chunk.removeEntry(entry);
						if (chunk.isEmpty()) {
							chunkMap.removeEntry(chunk);
							EventManager.fireEvent(new Event(Event.DELETE_ENTRY_CHUNK, chunkEventId));
						}
						addEntry(entry);
						break;
					}
				}
				EventManager.fireEvent(new Event(Event.UPDATE, id));
			}, Event.UPDATE, chunkEventId);
			
		}
	}
	
	/**
	 * Adds the passed Entry to the proper EntryChunk, creating a new EntryChunk
	 * if one is required or breaking up existing EntryChunks if they are too large
	 * @param entry the Entry to add
	 */
	public void addEntry(Entry entry) {
		
		LocalDateTime entryDate = entry.getDateTime();
		EntryChunk chunk = chunkMap.getNearestEntry(entryDate, (o1, o2) -> {
			
			long diff = o2.until(o1, ChronoUnit.MINUTES);
			int iDiff;
			if (diff > Integer.MAX_VALUE) {
				iDiff = Integer.MAX_VALUE;
			} else {
				iDiff = (int) diff;
			}
			return iDiff;
			
		});
		
		if (chunk == null || chunk.getEarliest().getYear() != entry.getDateTime().getYear()) {
			
			chunk = producer.createEntryChunk(entryChunkDirectory, entry);
			chunkMap.put(entryDate, entryDate, chunk);
			
			String id = EventManager.getUniqueID(this);
			String chunkId = EventManager.getUniqueID(chunk);
			final EntryChunk chunkRef = chunk;
			EventManager.addListener(chunk, e -> {
				for (Entry entry2: chunkRef.getEntries()) {
					if (entry2.getDateTime().isBefore(chunkRef.getEarliest())
							|| entry2.getDateTime().isAfter(chunkRef.getLatest())) {
						chunkRef.removeEntry(entry2);
						if (chunkRef.isEmpty()) {
							chunkMap.removeEntry(chunkRef);
							EventManager.fireEvent(new Event(Event.DELETE_ENTRY_CHUNK, chunkId));
						}
						addEntry(entry2);
						break;
					}
				}
				EventManager.fireEvent(new Event(Event.UPDATE, id));
			}, Event.UPDATE, chunkId);
			
			EventManager.fireEvent(new Event(Event.NEW_ENTRY_CHUNK, chunk, id));
			
		} else {
			
			Entry testEntry = chunk.getEntries().get(0);
			
			chunk.addEntry(entry);
			
			//if (chunk.getEntryCount() > SPLIT_THRESHOLD) {
				// TODO split the chunk into smaller pieces
			//} else {
				chunkMap.expandRange(entry.getDateTime(), testEntry.getDateTime());
			//}
			
		}
	}
	
	/**
	 * Adds all the passed Entries to the proper EntryChunks, creating new EntryChunks
	 * if required or breaking up existing EntryChunks if they are too large
	 * @param entries the Entries to add
	 */
	public void addAllEntries(Collection<Entry> entries) {
		for (Entry entry: entries) {
			addEntry(entry);
		}
	}
	
	/**
	 * Removes the passed Entry from the EntryChunk that contains it. Does
	 * nothing if the passed Entry is not in any EntryChunks managed by the
	 * manager. If the removed Entry is the last Entry in the EntryChunk it
	 * is removed from, then the EntryChunk is deleted
	 * @param entry the Entry to remove
	 */
	public void removeEntry(Entry entry) {
		
		LocalDateTime entryDate = entry.getDateTime();
		EntryChunk chunk = chunkMap.getNearestEntry(entryDate, (o1, o2) -> {
			
			long diff = o2.until(o1, ChronoUnit.MINUTES);
			int iDiff;
			if (diff > Integer.MAX_VALUE) {
				iDiff = Integer.MAX_VALUE;
			} else {
				iDiff = (int) diff;
			}
			return iDiff;
			
		});
		
		chunk.removeEntry(entry);
		if (chunk.isEmpty()) {
			chunkMap.removeEntry(chunk);
			String id = EventManager.getUniqueID(chunk);
			EventManager.fireEvent(new Event(Event.DELETE_ENTRY_CHUNK, id));
		}
		
	}
	
	/**
	 * Rebalances the EntryChunks so that all the Entries are as equally distributed
	 * amongst as few as possible EntryChunks without exceeding the passed load
	 * fraction of the threshold, altering the DateResolution as necessary to be
	 * optimal. Warning: this method is highly costly in memory and time for large
	 * amounts of Entries
	 */
	public void rebalance(double load) {
		// TODO
	}
	
	/**
	 * Returns all the EntryChunks in this EntryChunkManager in order, nonbacking
	 * @return an ordered List of EntryChunks
	 */
	public List<EntryChunk> getEntryChunks() {
		return chunkMap.orderedValues(false);
	}
	
	/**
	 * Returns an unmodifiable List of all the Entries from all the EntryChunks
	 * managed by the EntryChunkManager
	 * @return an unmodifiable List of Entries
	 */
	public ReadOnlyList<Entry> getEntries() {
		List<Entry> entries = new ArrayList<>();
		for (EntryChunk chunk: getEntryChunks()) {
			ReadOnlyList.addAll(entries, chunk.getEntries());
		}
		return new ReadOnlyList<>(entries);
	}
	
	/**
	 * Returns the DateResolution currently in use
	 * @return the DateResolution
	 */
	public DateResolution getDateResolution() {
		return resolutionManager.getResolution();
	}
	
}
