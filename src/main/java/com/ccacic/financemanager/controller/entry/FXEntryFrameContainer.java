package com.ccacic.financemanager.controller.entry;

import java.util.List;

import com.ccacic.financemanager.controller.FXFrameContainer;
import com.ccacic.financemanager.model.entry.Entry;

import javafx.scene.control.TableColumn;

/**
 * The frame container for Entry frames
 * @author Cameron Cacic
 *
 */
public class FXEntryFrameContainer extends FXFrameContainer<Entry> {
	
	private static FXEntryFrameContainer instance = new FXEntryFrameContainer();
	
	/**
	 * Returns the instance of the FXEntryFrameContainer
	 * @return the instance
	 */
	public static FXEntryFrameContainer getInstance() {
		return instance;
	}
	
	/**
	 * Prevents external instantiation
	 */
	private FXEntryFrameContainer() {
		super();
	}
	
	/**
	 * Passes the columns of the EntryTable to the appropriate factory
	 * for editing
	 * @param columns the EntryTable columns
	 * @param key the key to locate the factory with
	 */
	public void insertEntryColumns(List<TableColumn<Entry, ?>> columns, String key) {
		FXEntryFactory<Entry> factory = (FXEntryFactory<Entry>) frameListMap.get(key);
		if (factory == null) {
			return;
		}
		factory.insertEntryColumns(columns);;
	}

}
