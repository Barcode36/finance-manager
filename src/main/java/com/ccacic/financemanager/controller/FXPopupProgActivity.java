package com.ccacic.financemanager.controller;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * A popup activity for building an item by progressing through
 * data collecting frames. Provides a basic framework of next,
 * previous, cancel, and finish buttons for progressing through
 * the frames, which are supplied by accessing the frames List
 * directly
 * @author Cameron Cacic
 *
 * @param <T> the type of item the activity builds
 */
public abstract class FXPopupProgActivity<T> extends FXPopupActivity<BorderPane> {
	
	@FXML
	protected Button prevButton;
	@FXML
	protected Button nextButton;
	@FXML
	protected Button cancelButton;
	@FXML
	protected Button finishButton;

	protected final List<FXPopupProgActivityFrame<?, ? extends Pane>> frames;
	protected int index;
	
	/**
	 * Creates a new popup progression activity with an empty frame list
	 */
	public FXPopupProgActivity() {
		frames = new ArrayList<>();
		index = 0;
	}
	
	/**
	 * Creates the result of the activity from the gathered data
	 * of the frames
	 * @param frameValues
	 * @return the new item
	 */
	protected abstract T createResult(ParamMap frameValues);
	
	@Override
	protected void initializeActivity() {
		for (FXPopupProgActivityFrame<?, ? extends Pane> frame: frames) {
			if (!frame.isLoaded()) {
				frame.callLoader();
			}
		}
		getRoot().setCenter(frames.get(index).getRoot());
		
		prevButton.setDisable(index <= 0);
		prevButton.setOnAction(e -> {
			
			if (index > 0) {
				index--;
				getRoot().setCenter(frames.get(index).getRoot());
				nextButton.setDisable(false);
			}
			prevButton.setDisable(index <= 0);
			
			if (getRoot().getScene().getHeight() > popupStage.getHeight()) {
				popupStage.setHeight(getRoot().getScene().getHeight());
			}
			
			if (getRoot().getScene().getWidth() > popupStage.getWidth()) {
				popupStage.setHeight(getRoot().getScene().getWidth());
			}
			
		});
		
		nextButton.setDisable(index >= frames.size() - 1);
		nextButton.setOnAction(e -> {
			
			if (index < frames.size() - 1) {
				index++;
				getRoot().setCenter(frames.get(index).getRoot());
				prevButton.setDisable(false);
			}
			nextButton.setDisable(index >= frames.size() - 1);
			
			if (getRoot().getScene().getHeight() > popupStage.getHeight()) {
				popupStage.setHeight(getRoot().getScene().getHeight());
			}
			
			if (getRoot().getScene().getWidth() > popupStage.getWidth()) {
				popupStage.setHeight(getRoot().getScene().getWidth());
			}
			
		});
		
		cancelButton.setOnAction(e -> {
			
			popupStage.close();
			
		});
		
		finishButton.setOnAction(e -> {
			
			ParamMap paramMap = new ParamMap();
			for (FXPopupProgActivityFrame<?, ? extends Pane> frame: frames) {
				paramMap.putAll(frame.getParamMap());
			}
			T result = createResult(paramMap);
			String id = EventManager.getUniqueID(this);
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, result, id));
			popupStage.close();
			
		});
		finishButton.disableProperty().bind(nextButton.disabledProperty().not());
		
		popupStage.sizeToScene();
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("activity_popup_prog.fxml"), new BorderPane());
		super.callLoader();
	}

}
