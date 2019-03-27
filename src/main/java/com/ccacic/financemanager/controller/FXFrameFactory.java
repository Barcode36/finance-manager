package com.ccacic.financemanager.controller;

import java.util.List;

import javafx.scene.layout.Pane;

/**
 * Creates a List of frames
 * @author Cameron Cacic
 *
 * @param <T> the type the frames are intended for
 */
public abstract class FXFrameFactory<T> {

	private String factoryName;
	
	/**
	 * Creates a factory with the given name
	 * @param name the factory name
	 */
	public FXFrameFactory(String name) {
		factoryName = name;
	}
	
	/**
	 * Returns the factory name
	 * @return the factory name
	 */
	public String getFactoryName() {
		return factoryName;
	}
	
	/**
	 * Creates a new batch of FXPopupProgActivityFrames from scratch, so that the frames
	 * being put into progression popup activities do not contain data from previous frame
	 * usage. Inside this method, the new operator should be used for every element inside
	 * the List that is returned. While this is an instance method, it will be used inside
	 * a Singleton, so any instance data contained in an implementing class should be
	 * treated as effectively static
	 * @return a List of new frames
	 */
	public abstract List<FXPopupProgActivityFrame<?, ? extends Pane>> createFrameList(T toEdit);
	
}
