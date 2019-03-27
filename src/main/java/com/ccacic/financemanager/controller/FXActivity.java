package com.ccacic.financemanager.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Simplifies the process of building a JavaFX controller from FXML.
 * All that is needed is a proper URL to the FXML document describing
 * the controller and an instance of the document root. The FXML document
 * must have a dynamic root matching the instance passed in.
 * 
 * The result of the activity, if its goal is to create a result, can be
 * obtained by listening for the ACTIVITY_RESULT_OBTAINED event matched
 * to this FXActivity's unique event ID.
 * 
 * After instantiating an FXActivity, it will remain unloaded and unviewable
 * until the open method is called
 * @author Cameron Cacic
 *
 * @param <V> the type of the dynamic root in the FXML document
 */
public abstract class FXActivity<V extends Pane> {
	
	/**
	 * The event type for an activity result
	 */
	public static final String ACTIVITY_RESULT_OBTAINED = "activity_result_obtained";
	
	private V pane;
	private boolean isLoaded;
	
	private Runnable afterInitialization;
	
	/**
	 * No-argument constructor, allows for easy extension
	 */
	public FXActivity() {
		this.isLoaded = false;
	}
	
	/**
	 * Calls the loader for the provided FXML document. The type of
	 * V must match the dynamic root of the FXML document. This method
	 * must be called in order for the controller to load, and should
	 * be called only from within callLoader. After calling, getRoot
	 * will return the passed in dynmaic root instance
	 * @param fxmlPath the URL to the FXML document
	 * @param pane an instance of the dynamic root type
	 */
	protected void load(URL fxmlPath, V pane) {
		this.pane = pane;
		FXMLLoader loader = new FXMLLoader(fxmlPath);
		loader.setController(this);
		loader.setRoot(this.pane);
		try {
			loader.load();
			isLoaded = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns if this activity has been loaded
	 * @return the loaded state
	 */
	public boolean isLoaded() {
		return isLoaded;
	}
	
	/**
	 * Returns the dynamic root of this activity
	 * @return the dynamic root
	 */
	public V getRoot() {
		return pane;
	}
	
	/**
	 * Convenience method for setting the CSS background color
	 * of the passed Node
	 * @param n the node to color
	 * @param color the color
	 */
	protected void setColor(Node n, String color) {
		n.setStyle("-fx-background-color: " + color + ";");
	}
	
	/**
	 * Opens the activity
	 */
	public void open() {
		open(null);
	}
	
	/**
	 * Opens the activity
	 * @param afterInitialization Runnable called after the activity
	 * has been fully opened and initialized
	 */
	public void open(Runnable afterInitialization) {
		this.afterInitialization = afterInitialization;
		callLoader();
	}
	
	/**
	 * Should only be called by the FXML Loader
	 */
	public void initialize() {
		initializeActivity();
		if (afterInitialization != null) {
			afterInitialization.run();
		}
	}
	
	/**
	 * Should call the load method within itself and enact any
	 * modifications to the dynamic root. Should never be called
	 * directly
	 */
	protected abstract void callLoader();
	
	/**
	 * This method is called after the FXML loader has finished
	 * instantiating all fields. All listeners and modifications
	 * to components should be handled here. Should not be called
	 * directly
	 */
	protected abstract void initializeActivity();
}
