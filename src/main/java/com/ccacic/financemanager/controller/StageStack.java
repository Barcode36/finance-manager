package com.ccacic.financemanager.controller;

import java.util.LinkedList;

import javafx.stage.Stage;

/**
 * A global reference class for creating popup stages based
 * on the current stage at the top of the stack. Allows for a
 * popup to determine who its owner should be without needing
 * to have full knowledge of the stage heirarchy. Thread safe
 * @author Cameron Cacic
 *
 */
public final class StageStack {
	
	private static final LinkedList<Stage> stageStack = new LinkedList<>();
	
	/**
	 * Peeks the stage at the top of the stack
	 * @return the stage at the top of the stack
	 */
	public static synchronized Stage peekStage() {
		if (stageStack.isEmpty()) {
			return null;
		}
		return stageStack.peekLast();
	}
	
	/**
	 * Adds the given stage to the top of the stack
	 * @param stage the stage to add
	 */
	public static synchronized void addStage(Stage stage) {
		if (!stageStack.isEmpty()) {
			stage.initOwner(stageStack.getLast());
		}
		stageStack.addLast(stage);
	}
	
	/**
	 * Pops the stage from the top of the stack
	 * @return the stage on the top of the stack
	 */
	public static synchronized Stage popStage() {
		if (stageStack.isEmpty()) {
			return null;
		}
		return stageStack.pop();
	}
	
	/**
	 * Prevents intances from being created
	 */
	private StageStack() {
		// do nothing
	}
	
}
