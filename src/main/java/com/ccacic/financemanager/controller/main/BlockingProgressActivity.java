package com.ccacic.financemanager.controller.main;

import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;

/**
 * An activity for blocking user input to the program while
 * a task happens in the background. Progress on this task
 * can be displayed through the ProgressBar. To do so, fire
 * an UPDATE event with the identifier passed to this activity
 * and the fraction of progress as a double as the event's data.
 * The only way to close the activity is to close the stage
 * directly or fire a BLOCKING_PROGRESS_END event on the passed
 * event identifier. The activity can either be manually
 * instantiated or fire a BLOCKING_PROGRESS_REQUEST event. If
 * the user closes the popup before a BLOCKING_PROGRESS_END event
 * is recieved, a BLOCKING_PROGRESS_CANCELED is fired on the
 * passed event identifier. Note that this will only be fired
 * if the activity is created by firing a BLOCKING_PROGRESS_REQUEST.
 * The static register method must be called at least once before
 * BLOCKING_PROGRESS_REQUEST will work to create an activity
 * @author Cameron Cacic
 *
 */
public class BlockingProgressActivity extends FXPopupActivity<VBox> {
	
private static boolean registered = false;
	
	/**
	 * Registers Blocking Progress Activity with the EventManager to listen
	 * for BLOCKING_PROGRESS_REQUEST events
	 */
	public static void register() {
		if (!registered) {
			
			EventManager.addListener(null, e -> {
				String[] data = (String[]) e.getData();
				Platform.runLater(() -> {
					
					BlockingProgressActivity activity = new BlockingProgressActivity(data[1], data[2], data[0]);
					
					String activityId = EventManager.getUniqueID(activity);
					EventManager.addListener(null, e2 -> {
						if (!activity.ended) {
							EventManager.fireEvent(new Event(Event.BLOCKING_PROGRESS_CANCELED, data[0]));
						}
						EventManager.removeThisListener();
					}, POPUP_STAGE_CLOSED, activityId);
					
					activity.open();
					
				});
			}, Event.BLOCKING_PROGRESS_REQUEST);
			
			registered = true;
		}
	}
	
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Text messageText;
	
	private String message;
	private String title;
	private String eventId;
	private boolean ended;
	
	/**
	 * Creates a new BlockingProgressActivity
	 * @param message the message to display
	 * @param title the title of the stage
	 * @param eventId the id to fire and listen for events on
	 */
	public BlockingProgressActivity(String message, String title, String eventId) {
		this.message = message;
		this.title = title;
		this.eventId = eventId;
		this.ended = false;
	}

	@Override
	protected void callPopupLoader() {
		load(FileHandler.getLayout("activity_blocking_progress.fxml"), new VBox());
		popupStage.setTitle(title);
		popupStage.initStyle(StageStyle.UNDECORATED);
	}

	@Override
	protected void initializeActivity() {
		
		messageText.setText(message);
		
		EventManager.addListener(progressBar, e -> {
			Double prog = (Double) e.getData();
			if (prog != null) {
				Platform.runLater(() -> progressBar.setProgress(prog));
			}
		}, Event.UPDATE, eventId);
		
		EventManager.addListener(null, e -> {
			ended = true;
			Platform.runLater(() -> popupStage.close());
			EventManager.removeThisListener();
		}, Event.BLOCKING_PROGRESS_END, eventId);
		
	}

}
