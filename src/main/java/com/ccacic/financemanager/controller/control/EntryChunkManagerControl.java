package com.ccacic.financemanager.controller.control;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.view.EntryChunkView;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;
import com.ccacic.financemanager.model.entrychunk.EntryChunkManager;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.VBox;

/**
 * A control for displaying a generic EntryChunkManager, which in turn displays
 * all the EntryChunks within the manager. Thr SelectionModels of all the
 * EntryChunkViews are bound together such that only one entry may be selected
 * at a time across all EntryChunkViews. The view will dynamically populate
 * and depopulate entries and EntryChunkViews through the EventManager
 * @author Cameron Cacic
 *
 */
public class EntryChunkManagerControl extends VBox {
	
	private SelectionModel<Entry> currentModel;
	private Currency currency;
	private String entryType;
	private EntryChunkManager manager;
	private List<SelectionModel<Entry>> selectionModels;
	
	/**
	 * Creates a new EntryChunk
	 */
	public EntryChunkManagerControl() {
		managedProperty().bind(visibleProperty());
		setVisible(false);
		setSpacing(5.0);
		setPadding(new Insets(10.0, 0.0, 0.0, 0.0));
		selectionModels = new ArrayList<>();
	}
	
	/**
	 * Sets the EntryChunkManager to the provided value
	 * @param manager the manager to display
	 * @param currency the currency to display the manager's total in
	 * @param entryType a key to the entry type of the manager
	 */
	public void setManager(EntryChunkManager manager, Currency currency, String entryType) {
		
		getChildren().clear();
		selectionModels.clear();
		currentModel = null;
		this.manager = manager;
		this.currency = currency;
		this.entryType = entryType;
		
		List<Node> entryChunkViews = new ArrayList<>();
		EntryChunkView first = null;
		for (EntryChunk chunk: manager.getEntryChunks()) {
			EntryChunkView entryChunkView = getEntryChunkView(chunk, false);
			if (first == null) {
				first = entryChunkView;
			}
			entryChunkViews.add(entryChunkView.getRoot());						
		}
		
		if (first != null) {
			first.toggleEntries();
		}
		Platform.runLater(() -> getChildren().addAll(entryChunkViews));
		
		String managerId = EventManager.getUniqueID(manager);
		EventManager.addListener(this, e -> {
			
			EntryChunk entryChunk = (EntryChunk) e.getData();
			EntryChunkView entryChunkView = getEntryChunkView(entryChunk, true);
			int insertionIndex = manager.getEntryChunks().indexOf(entryChunk);
			Platform.runLater(() -> {
				
				getChildren().add(insertionIndex, entryChunkView.getRoot());
				entryChunkView.getEntryTable().requestFocus();
				
			});
			
		}, Event.NEW_ENTRY_CHUNK, managerId);
		
	}
	
	/**
	 * Creates a new EntryChunkView tied to this manager
	 * @param entryChunk the EntryChunk to create the view for
	 * @param newChunk if this is a newly created EntryChunk, toggles open the view
	 * @return the new EntryChunkView
	 */
	private EntryChunkView getEntryChunkView(EntryChunk entryChunk, boolean newChunk) {
		
		EntryChunkView entryChunkView = new EntryChunkView(entryChunk, currency, entryType, manager.getDateResolution());
		entryChunkView.open(() -> {
		
			SelectionModel<Entry> selectionModel = entryChunkView.getEntryTable().getSelectionModel();
			selectionModels.add(selectionModel);
			selectionModel.selectedItemProperty().addListener((obv, oldVal, newVal) -> {
				
				if (oldVal == null) {
					for (SelectionModel<Entry> model: selectionModels) {
						if (!model.equals(selectionModel)) {
							model.clearSelection();
						}
					}
				}
				if (newVal == null) {
					currentModel = null;
				} else {
					currentModel = selectionModel;
				}
				
			});
			
			entryChunkView.getEntryTable().getItems().addListener((ListChangeListener.Change<? extends Entry> c) -> {
				
				while (c.next());
				
				if (c.wasAdded()) {
					selectionModel.select(c.getAddedSubList().get(c.getAddedSubList().size() - 1));
				} else if (c.wasUpdated()) {
					selectionModel.select(c.getTo());
				}
				
				Platform.runLater(() -> entryChunkView.getEntryTable().requestFocus());
				
			});
			
			if (newChunk) {
				entryChunkView.toggleEntries();
				selectionModel.selectFirst();
			}
			
			String chunkId = EventManager.getUniqueID(entryChunk);
			EventManager.addListener(null, e -> {
				Platform.runLater(() -> getChildren().remove(entryChunkView.getRoot()));
				EventManager.removeThisListener();
			}, Event.DELETE_ENTRY_CHUNK, chunkId);
			
		});
		
		return entryChunkView;
		
	}
	
	public SelectionModel<Entry> getCurrentSelectionModel() {
		return currentModel;
	}
	
}
