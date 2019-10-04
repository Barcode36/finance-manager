package com.ccacic.financemanager.controller.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.fileio.User;
import com.ccacic.financemanager.logger.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;

/**
 * An activity for selecting a user
 * @author Cameron Cacic
 *
 */
public class UserSelectionActivity extends FXPopupActivity<BorderPane> {
	
	@FXML
	private ListView<String> userListView;
	@FXML
	private Button openUserButton;
	@FXML
	private Button createNewUserButton;
	
	private final List<String> users;
	
	/**
	 * Creates a new UserSelectionActivity
	 */
	public UserSelectionActivity() {
		File userDir = FileHandler.getInstance().getUserDir();
		users = new ArrayList<>();
		for (File u: Objects.requireNonNull(userDir.listFiles())) {
			if (u.isDirectory()) {
				for (String u2: Objects.requireNonNull(u.list())) {
					if (u2.equals(u.getName() + FileHandler.DATA_EXTENSION)) {
						users.add(u.getName());
						break;
					}
				}
			}
		}
	}

	@Override
	protected void initializeActivity() {
		
		userListView.setCellFactory(TextFieldListCell.forListView());
		userListView.getItems().addAll(users);
		userListView.getSelectionModel().selectFirst();
		
		createNewUserButton.setOnMouseClicked(e -> {
			
			if (!userListView.getItems().contains("New User")) {
				userListView.getItems().add("New User");
			}
			userListView.setEditable(true);
			userListView.getSelectionModel().select("New User");
			userListView.requestFocus();
			
		});
		
		openUserButton.setOnMouseClicked(e -> {
			
			
			String user = userListView.getSelectionModel().getSelectedItem();
			File userDir = new File(FileHandler.getInstance().getUserDir(), user);
			File userFile = new File(userDir, user + FileHandler.DATA_EXTENSION);
			Thread thread = new Thread(() -> {
				User.setCurrentUser(userFile);
			
				if (User.getCurrentUser() != null) {
				
					Logger.getInstance().logDebug("User selected");
					String id = EventManager.getUniqueID(this);
					EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, true, id));
					Platform.runLater(popupStage::close);
					
				} else {
					Logger.getInstance().logDebug("Failed to select user, canceled by operator");
				}
			});
			thread.start();
			
		});
		openUserButton.disableProperty().bind(userListView.getSelectionModel().selectedItemProperty().isNull());
		
	}
	
	@Override
	protected void callPopupLoader() {
		load(FileHandler.getLayout("activity_user_selection.fxml"), new BorderPane());
		popupStage.setTitle("User Selection");
	}

}
