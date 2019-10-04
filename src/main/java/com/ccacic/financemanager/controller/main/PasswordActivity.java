package com.ccacic.financemanager.controller.main;

import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A popup activity for obtaining a password from the user.
 * Can be intantiated directly and the result obtained from
 * the ACTIVITY_RESULT_OBTAINED event, or requested via firing
 * a PASSWORD_REQUEST event. The second option will only work
 * if the static method register() has been called first. It
 * only needs to be called once per JVM, but can be called
 * multiple times without issues
 * @author Cameron Cacic
 *
 */
public class PasswordActivity extends FXPopupActivity<VBox> {
	
	private static boolean registered = false;
	
	/**
	 * Registers PasswordActivity with the EventManager to listen
	 * for PASSWORD_REQUEST events
	 */
	public static void register() {
		if (!registered) {
			
			EventManager.addListener(null, e -> {
				String[] data = (String[]) e.getData();
				Platform.runLater(() -> {
					
					PasswordActivity activity = new PasswordActivity(data[1], data[2]);
					
					String activityId = EventManager.getUniqueID(activity);
					EventManager.addListener(null, e2 -> {
						EventManager.fireEvent(new Event(Event.PASSWORD_RECEIVED, e2.getData(), data[0]));
						EventManager.removeThisListener();
					}, ACTIVITY_RESULT_OBTAINED, activityId);
					
					EventManager.addListener(null, e2 -> {
						if (!activity.resultObtained) {
							EventManager.fireEvent(new Event(Event.PASSWORD_RECEIVED, null, data[0]));
						}
						EventManager.removeThisListener();
					}, POPUP_STAGE_CLOSED, activityId);
					
					activity.open();
					
				});
			}, Event.PASSWORD_REQUEST);
			
			registered = true;
		}
	}
	
	@FXML
	private Text passwordText;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Button cancelButton;
	@FXML
	private Button enterButton;
	
	private final String text;
	private final String title;
	private boolean resultObtained;
	
	/**
	 * Creates a new PasswordActivity
	 * @param text the text to display alongside the passord field
	 * @param title the title of the password stage
	 */
	private PasswordActivity(String text, String title) {
		this.text = text;
		this.title = title;
		this.resultObtained = false;
	}

	@Override
	protected void initializeActivity() {
		
		passwordText.setText(text);
		
		final String id = EventManager.getUniqueID(this);
		
		passwordField.setOnAction(e -> {
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, passwordField.getText(), id));
			resultObtained = true;
			popupStage.close();
		});
		
		cancelButton.setOnAction(e -> {
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, null, id));
			resultObtained = true;
			popupStage.close();
		});
		
		enterButton.setOnAction(e -> {
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, passwordField.getText(), id));
			resultObtained = true;
			popupStage.close();
		});
		
		popupStage.setTitle(title);
		
	}

	@Override
	protected void callPopupLoader() {
		load(FileHandler.getLayout("activity_password.fxml"), new VBox());
		getRoot().setSpacing(5.0);
		getRoot().setAlignment(Pos.CENTER_LEFT);
	}

}
