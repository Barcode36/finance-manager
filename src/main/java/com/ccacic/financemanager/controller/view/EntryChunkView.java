package com.ccacic.financemanager.controller.view;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.controller.control.entrytable.EntryTable;
import com.ccacic.financemanager.controller.entry.EntryActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entrychunk.DateResolution;
import com.ccacic.financemanager.model.entrychunk.DateResolutionManager;
import com.ccacic.financemanager.model.entrychunk.EntryChunk;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A view for displaying an EntryChunk. All Entries are
 * contained in an EntryTable
 * @author Cameron Cacic
 *
 */
public class EntryChunkView extends FXActivity<VBox> {
	
	@FXML
	private Text dateRange;
	@FXML
	private Button toggleEntriesButton;
	@FXML
	private EntryTable entryTable;
	
	private EntryChunk entryChunk;
	private Currency currency;
	private String entryType;
	private DateResolutionManager manager;
	
	/**
	 * Creates a new EntryChunkView
	 * @param entryChunk the EntryChunk to display
	 * @param currency the Currency to format with
	 * @param entryType the type of Entry to build the table for
	 * @param resolution the DateResolution of the EntryChunk's holder
	 */
	public EntryChunkView(EntryChunk entryChunk, Currency currency, String entryType, DateResolution resolution) {
		this.entryChunk = entryChunk;
		this.currency = currency;
		this.entryType = entryType;
		this.manager = new DateResolutionManager(resolution);
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("view_entry_chunk.fxml"), new VBox());
	}

	@Override
	protected void initializeActivity() {
		
		final String id = EventManager.getUniqueID(entryChunk);
		EventManager.addListener(entryChunk, e -> {
			Entry entry = (Entry) e.getData();
			entryTable.getItems().add(entry);
			entryTable.getSelectionModel().select(entry);
			Platform.runLater(() ->
				dateRange.setText(manager.getFormattedRange(entryChunk.getEarliest(), entryChunk.getLatest())));
			if (!entryTable.isVisible()) {
				toggleEntries();
			}
		}, Event.NEW_ENTRY, id);
		
		EventManager.addListener(entryChunk, e -> {
			
			Entry entry = (Entry) e.getData();
			entryTable.getItems().remove(entry);
			if (!entryChunk.isEmpty()) {
				Platform.runLater(() ->
					dateRange.setText(manager.getFormattedRange(entryChunk.getEarliest(), entryChunk.getLatest())));
			}
			
		}, Event.DELETE_ENTRY, id);
		
		EventManager.addListener(entryChunk, e -> {
			
			Delta delta = (Delta) e.getData();
			Entry entry = (Entry) delta.getObject();
			int index = entryTable.getItems().indexOf(entry);
			if (index > -1) {
				entryTable.getItems().set(index, entry);
				Platform.runLater(() ->
					dateRange.setText(manager.getFormattedRange(entryChunk.getEarliest(), entryChunk.getLatest())));
			}
			
		}, Event.UPDATE, id);
		
		ObservableList<Entry> entriesList = FXCollections.observableArrayList();
		ReadOnlyList.addAll(entriesList, entryChunk.getEntries());
		
		entryTable.managedProperty().bind(entryTable.visibleProperty());
		entryTable.setItems(entriesList);
		entryTable.buildTable(currency, entryType);
		
		entryTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
			
			@Override
			public void handle(MouseEvent e) {
				if (e.getClickCount() > 1) {
					Entry toEdit = entryTable.getSelectionModel().getSelectedItem();
					if (toEdit != null) {
						EntryActivity entryActivity = new EntryActivity(entryType, 
								currency, toEdit, id);
						entryActivity.open();
					}
				}
			}
			
		});
		
		toggleEntriesButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				
				if (toggleEntriesButton.getText().equals("+")) {
					toggleEntriesButton.setText("-");
					entryTable.setVisible(true);
				} else {
					toggleEntriesButton.setText("+");
					entryTable.setVisible(false);
				}
			}
			
		});
		
		dateRange.setText(manager.getFormattedRange(entryChunk.getEarliest(), entryChunk.getLatest()));
		
	}
	
	/**
	 * Returns the EntryTable for this view. Used so a parent
	 * view can target the table specifically for actions
	 * @return the EntryTable
	 */
	public EntryTable getEntryTable() {
		return entryTable;
	}
	
	/**
	 * Toggles the visibility of the EntryTable and focuses it
	 */
	public void toggleEntries() {
		if (toggleEntriesButton != null && entryTable != null) {
			Platform.runLater(() -> {
				toggleEntriesButton.fire();
				entryTable.requestFocus();
			});
		}
	}

}
