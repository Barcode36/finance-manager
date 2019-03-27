package com.ccacic.financemanager.model.entrychunk.children;

import java.io.File;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.children.StockEntry;
import com.ccacic.financemanager.model.entry.children.StockEntryAssembler;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;

/**
 * EnrtyChunk child that adds a shareTotal field that must be maintained
 * in the same way amount and entryCount are in EntryChunk
 * @author Cameron Cacic
 *
 */
public class StockEntryChunk extends EntryChunk {
	
	/* The share total is set to its proper value during the EntryChunk
	 * constructor, and then the share total is instantiated to zero.
	 * Unless some way is discovered to overcome this, all children of
	 * EntryChunk with fields updated during the finalization methods
	 * must re-update the fields in their constructors*/
	private double shareTotal = 0;
	
	/**
	 * Creates a new StockEntryChunk with the passed source file and expected source file hash
	 * @param entryChunkFile the source file
	 * @param expectedHash the expected hash of the source file
	 */
	public StockEntryChunk(File entryChunkFile, String expectedHash) {
		super(entryChunkFile, expectedHash);
		for (Entry entry: getEntries())	{
			StockEntry stockEntry = (StockEntry) entry;
			shareTotal += stockEntry.getShares();
		}
	}
	
	/**
	 * Creates a new StockEntryChunk with the passed directory and first Entry
	 * @param entryChunkDirectory the directory
	 * @param firstEntry the first Entry
	 */
	public StockEntryChunk(File entryChunkDirectory, Entry firstEntry) {
		super(entryChunkDirectory, firstEntry);
		StockEntry stockEntry = (StockEntry) firstEntry;
		shareTotal = stockEntry.getShares();
	}
	
	@Override
	protected void addEntryFinalize(Entry entry) {
		StockEntry stockEntry = (StockEntry) entry;
		shareTotal += stockEntry.getShares();
	}
	
	@Override
	protected void removeEntryFinalize(Entry entry) {
		StockEntry stockEntry = (StockEntry) entry;
		shareTotal -= stockEntry.getShares();
	}
	
	/**
	 * Returns the share total
	 * @return the share total
	 */
	public double getShareTotal() {
		return shareTotal;
	}
	
	@Override
	public void onEvent(Event event) {
		Delta delta = (Delta) event.getData();
		shareTotal += delta.getNewValueAsDouble(StockEntryAssembler.SHARES) - delta.getOldValueAsDouble(StockEntryAssembler.SHARES);
		super.onEvent(event);
	}

}
