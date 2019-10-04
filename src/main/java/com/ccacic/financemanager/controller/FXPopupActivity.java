package com.ccacic.financemanager.controller;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * An FXActivity designed to work in an independant stage
 * from the main stage. Uses StageStack to create the chain
 * of stage ownership, with the stage at the top of the stack
 * becoming the parent of the stage for this popup. This popup's
 * stage is then added to the top of the stack. Upon the closing
 * of the popup stage, an event is fired of type
 * POPUP_STAGE_CLOSED. This is intended for handling when the
 * user has forcibly closed the stage when a return value was
 * exptected from the activity
 * @author Cameron Cacic
 *
 * @param <V> the type of the dynamic root of the activity
 */
public abstract class FXPopupActivity<V extends Pane> extends FXActivity<V> {
	
	/**
	 * The event type for the popup stage closing
	 */
	public static final String POPUP_STAGE_CLOSED = "popup_stage_closed";
	
	protected final Stage popupStage;
	
	/**
	 * Creates a new popup activity and a new stage, incorporating
	 * it into the stage stack
	 */
	protected FXPopupActivity() {
		String actId = EventManager.getUniqueID(this);
		popupStage = new Stage() {
			
			@Override
			public void close() {
				super.close();
				StageStack.popStage();
				EventManager.fireEvent(new Event(POPUP_STAGE_CLOSED, actId));
			}
			
		};
		popupStage.initModality(Modality.APPLICATION_MODAL);
		StageStack.addStage(popupStage);
	}
	
	/**
	 * Acts like callLoader. Should call the load method within itself
	 * and enact anymodifications to the dynamic root. Should never be
	 * called directly
	 */
	protected abstract void callPopupLoader();
	
	@Override
	protected void callLoader() {
		callPopupLoader();
		Scene scene = new Scene(getRoot());
		scene.getStylesheets().add(FileHandler.getStyle("stylesheet.css").toExternalForm());
		popupStage.setScene(scene);
		popupStage.setResizable(true);
		popupStage.sizeToScene();
		popupStage.showAndWait();
	}
}
