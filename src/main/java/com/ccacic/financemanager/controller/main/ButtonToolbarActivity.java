package com.ccacic.financemanager.controller.main;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * An activity for easy action selection. Simply contains Buttons
 * mapped to various functions for testing. Should eventually be
 * replaced by an actual toolbar once toolbar functions are finalized
 * @author Cameron Cacic
 *
 */
public class ButtonToolbarActivity extends FXActivity<HBox> {
	
	@FXML
	private Button selectUserButton;
	@FXML
	private Button saveButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button saveArchiveButton;
	@FXML 
	private Button loadArchiveButton;
	
	@Override
	protected void initializeActivity() {
		
		selectUserButton.setOnAction(e -> {
			UserSelectionActivity userSelectionActivity = new UserSelectionActivity();
			String id = EventManager.getUniqueID(userSelectionActivity);
			EventManager.addListener(this, e2 -> {
				
				boolean userSelected = (boolean) e2.getData();
				if (userSelected) {
					FileHandler fileHandler = FileHandler.getInstance();
					fileHandler.loadRecords();
				}
				
			}, FXActivity.ACTIVITY_RESULT_OBTAINED, id);
			userSelectionActivity.open();
		});
		selectUserButton.setDisable(true);
		
		saveButton.setOnAction(e -> {
			FileHandler fileHandler = FileHandler.getInstance();
			fileHandler.writeFiles();
		});
		
		refreshButton.setOnAction(e -> {
			refreshButton.setDisable(true);
			Event event = EventManager.fireEvent(new Event(Event.REFRESH_RATES));
			EventManager.onEventFinish(event, () -> Platform.runLater(() -> refreshButton.setDisable(false)));
		});
		
		saveArchiveButton.setOnAction(e -> {
			saveArchiveButton.setDisable(true);
			Event event = EventManager.fireEvent(new Event(Event.SAVE_ARCHIVE_REQUEST));
			EventManager.onEventFinish(event, () -> Platform.runLater(() -> saveArchiveButton.setDisable(false)));
		});
		
		loadArchiveButton.setOnAction(e -> {
			loadArchiveButton.setDisable(true);
			Event event = EventManager.fireEvent(new Event(Event.LOAD_ARCHIVE_REQUEST));
			EventManager.onEventFinish(event, () -> Platform.runLater(() -> loadArchiveButton.setDisable(false)));
		});
		
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("activity_button_toolbar.fxml"), new HBox());
		getRoot().setSpacing(5);
	}
	
}
