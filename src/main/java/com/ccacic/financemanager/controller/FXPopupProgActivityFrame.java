package com.ccacic.financemanager.controller;

import com.ccacic.financemanager.model.ParamMap;

import javafx.scene.layout.Pane;

/**
 * A mini FXActivity. Exists as a frame within an FXPopupProgActivity.
 * Its purpose is to display an interface to the user for obtaining
 * specific data, obtainable by the single method within it
 * @author Cameron Cacic
 *
 * @param <T> the type the frame collects data for
 * @param <V> the dynamic root type
 */
public abstract class FXPopupProgActivityFrame<T, V extends Pane> extends FXActivity<V> {

	/**
	 * Returns the data collected by this frame in the form of
	 * a ParamMap
	 * @return the ParamMap
	 */
	public abstract ParamMap getParamMap();
	
}
