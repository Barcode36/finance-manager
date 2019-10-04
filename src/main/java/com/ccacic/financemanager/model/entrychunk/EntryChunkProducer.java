package com.ccacic.financemanager.model.entrychunk;

import java.io.File;

import com.ccacic.financemanager.model.entry.Entry;

/**
 * Interface for producing instances of EntryChunk
 * @author Cameron Cacic
 *
 */
public interface EntryChunkProducer {
	
	/**
	 * Creates a new EntryChunk using the source file and expected hash constructor
	 * @param entryChunkFile the source File to get Entries from
	 * @param expectedHash the expected hash of the source File
	 * @return the new EntryChunk instance
	 */
	EntryChunk createEntryChunk(File entryChunkFile, String expectedHash);
	
	/**
	 * Creates a new EntryChunk using the directory and first Entry constructor
	 * @param entryChunkDirectory the directory to create the EntryChunk within
	 * @param firstEntry the first Entry to put in the EntryChunk
	 * @return the new EntryChunk instance
	 */
	EntryChunk createEntryChunk(File entryChunkDirectory, Entry firstEntry);
	
}
