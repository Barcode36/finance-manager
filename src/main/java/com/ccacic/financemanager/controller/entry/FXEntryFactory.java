package com.ccacic.financemanager.controller.entry;

import java.util.List;

import com.ccacic.financemanager.controller.FXFrameFactory;
import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.entry.Entry;

import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;

/**
 * The FXFactory for Entries in general
 * @author Cameron Cacic
 *
 * @param <T> the type of Entry
 */
public abstract class FXEntryFactory<T extends Entry> extends FXFrameFactory<Entry> {

	/**
	 * Creates a new FXEntryFactory with the given name
	 * @param name the name of the factory
	 */
	public FXEntryFactory(String name) {
		super(name);
	}
	
	/**
	 * Creates a list of frames needed to build an Entry of type T
	 * @param toEdit the Entry to edit, or null to create a new Entry
	 * @return a list of activity frames
	 */
	public abstract List<FXPopupProgActivityFrame<?, ? extends Pane>> createEntryFrameList(T toEdit);
	
	/**
	 * The passed list contains all the default columns for EntryTable.
	 * New columns inserted into this list will appear in the EntryTable
	 * in the order they appear in the list. This allows Entry subclasses
	 * to add columns for new data types, remove unneccesary columns, or
	 * reorder the columns
	 * @param columns the columns of the EntryTable
	 */
	public abstract void insertEntryColumns(List<TableColumn<T, ?>> columns); 
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createFrameList(Entry toEdit) {
		return createEntryFrameList((T) toEdit);
	}
	
}
