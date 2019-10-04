package com.ccacic.financemanager.controller.main;

import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A popup activity for obtaining user confirmation before performing an
 * action. Can be intantiated directly and the result obtained from
 * the ACTIVITY_RESULT_OBTAINED event, or requested via firing
 * a CONFIRMATION_REQUEST event. The second option will only work
 * if the static method register() has been called first. It
 * only needs to be called once per JVM, but can be called
 * multiple times without issues
 * @author Cameron Cacic
 *
 */
public class ConfirmationActivity extends FXPopupActivity<VBox> {

	private static boolean registered = false;
	
	/**
	 * Registers Confirmation Activity with the EventManager to listen
	 * for CONFIRMATION_REQUEST events
	 */
	public static void register() {
		if (!registered) {
			
			EventManager.addListener(null, e -> {
				String[] data = (String[]) e.getData();
				Platform.runLater(() -> {
					
					ConfirmationActivity activity = new ConfirmationActivity(data[1], data[2]);
					
					String activityId = EventManager.getUniqueID(activity);
					EventManager.addListener(null, e2 -> {
						EventManager.fireEvent(new Event(Event.CONFIRMATION_RECEIVED, e2.getData(), data[0]));
						EventManager.removeThisListener();
					}, ACTIVITY_RESULT_OBTAINED, activityId);
					
					EventManager.addListener(null, e2 -> {
						if (!activity.resultObtained) {
							EventManager.fireEvent(new Event(Event.CONFIRMATION_RECEIVED, e2.getData(), data[0]));
						}
						EventManager.removeThisListener();
					}, POPUP_STAGE_CLOSED, activityId);
					
					activity.open();
					
				});
			}, Event.CONFIRMATION_REQUEST);
			
			registered = true;
		}
	}
	
	@FXML
	private Button noButton;
	@FXML
	private Button yesButton;
	@FXML
	private Text messageText;
	
	private final String message;
	private final String title;
	private boolean resultObtained;
	
	/**
	 * Creates a new ConfirmationActivity
	 * @param message the message to display
	 * @param title the title of the popup stage
	 */
	private ConfirmationActivity(String message, String title) {
		this.message = message;
		this.title = title;
		this.resultObtained = false;
	}
	
	@Override
	protected void callPopupLoader() {
		load(FileHandler.getLayout("activity_confirmation.fxml"), new VBox());
		getRoot().setSpacing(5.0);
		popupStage.setTitle(title);
	}

	@Override
	protected void initializeActivity() {
		
		messageText.setText(message);
		
		final String id = EventManager.getUniqueID(this);
		
		yesButton.setOnAction(e -> {
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, 1, id));
			resultObtained = true;
			popupStage.close();
		});
		
		noButton.setOnAction(e -> {
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, 0, id));
			resultObtained = true;
			popupStage.close();
		});
		
	}

}
