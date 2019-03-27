package com.ccacic.financemanager.fileio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryFactory;
import com.ccacic.financemanager.util.StringProcessing;

/**
 * Provides Entry specific FileIO methods
 * @author Cameron Cacic
 *
 */
public class EntryFileIO extends FileIO {

	/**
	 * Loads the passed File as a List of Entries
	 * @param sourceFile the File to read
	 * @param expectedHash the expected hash of the File
	 * @return a List of Entries from the File
	 * @throws IOException
	 */
	public List<Entry> loadEntries(File sourceFile, String expectedHash) throws IOException {
		
		String entryStrings = loadFile(sourceFile, expectedHash);
		List<Entry> entries = new ArrayList<>();
		if (entryStrings == null) {
			return entries;
		}
		List<String> entryStringList = StringProcessing.pullBracketSections(entryStrings);
		EntryFactory factory = EntryFactory.getInstance();
		for (String entryStr: entryStringList) {
			ParamMap entryMap = ParamMap.decode(entryStr, false);
			entries.add(factory.requestItem(entryMap));
		}
		return entries;
		
	}

	/**
	 * Writes the passed Entries to the passed File
	 * @param file the File to write to
	 * @param entries the Entries to write
	 * @return the hash of the File post writing Entries
	 * @throws IOException
	 */
	public String writeEntries(File file, List<Entry> entries) throws IOException {
		
		StringBuilder stringBuilder = new StringBuilder();
		EntryFactory factory = EntryFactory.getInstance();
		for (Entry entry: entries) {
			String encoded = factory.requestDisassembly(entry).encode();
			stringBuilder.append(encoded);
		}

		return writeToFile(file, stringBuilder.toString());
		
	}
	
}
