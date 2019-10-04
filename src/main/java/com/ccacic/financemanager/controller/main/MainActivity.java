package com.ccacic.financemanager.controller.main;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.controller.StageStack;
import com.ccacic.financemanager.controller.view.AccountHolderListView;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.fileio.User;
import com.ccacic.financemanager.launcher.Launcher;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.Category;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;

/**
 * The main activity of the view. Holds all views and activities
 * that aren't popup activities. Coordinates population of all
 * BorderPane sections as well as their initial population. Doesn't
 * load using an FXML document to allow for more flexibility
 * @author Cameron Cacic
 *
 */
public class MainActivity extends FXActivity<BorderPane> {

	private static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

	//private static final double accountButtonHeight = 0.12 * screenBounds.getHeight();
	//private static final double accountButtonWidth = 0.12 * screenBounds.getWidth();
	private static final double mainStageMinWidth = 0.67 * screenBounds.getWidth();
	private static final double mainStageMinHeight = 0.67 * screenBounds.getHeight();
	private static final double categoryListMinWidth = 0.30 * screenBounds.getWidth();
	private static final double categoryListMaxWidth = 0.35 * screenBounds.getWidth();
	private static final double categoryListHeight = 0.66 * screenBounds.getHeight();
	//private static final double categoryBarHeight = 0.063 * screenBounds.getHeight();
	private static final double entryListHeight = categoryListHeight;
	private static final double entryListWidth = screenBounds.getWidth() - categoryListMinWidth;
	
	private static final double scrollbarWidth;
	static {
		double sW;
		try {
			sW = ScrollBar.class.getDeclaredConstructor().newInstance().getWidth();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			sW = 0;
		}
		scrollbarWidth = sW;

	}

	private VBox acctHoldBox;
	private ScrollPane accountScrollPane;
	
	private Pane acctPane;
	private Stage mainStage;
	
	public MainActivity(Stage mainStage) {
		this.mainStage = mainStage;
		StageStack.addStage(mainStage);
	}
	
	@Override
	protected void initializeActivity() {
		this.acctHoldBox = new VBox();
		this.accountScrollPane = new ScrollPane();
		BorderPane mainPane = new BorderPane();
		
		ScrollPane acctHoldScrollPane = new ScrollPane();
		VBox acctHoldAndSumBox = new VBox();
		
		accountScrollPane.setPrefHeight(entryListHeight);
		accountScrollPane.setPrefWidth(entryListWidth);
		accountScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		accountScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		accountScrollPane.setFitToWidth(true);
		
		acctHoldScrollPane.setPrefHeight(categoryListHeight);
		acctHoldScrollPane.setMinWidth(categoryListMinWidth);
		acctHoldScrollPane.setMaxWidth(categoryListMaxWidth);
		acctHoldScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		acctHoldScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		
		acctHoldBox.setSpacing(5);
		acctHoldBox.setMinWidth(categoryListMinWidth - scrollbarWidth);
		acctHoldBox.setMaxWidth(categoryListMaxWidth - scrollbarWidth);
		acctHoldScrollPane.setContent(acctHoldBox);
		
		acctHoldAndSumBox.getChildren().add(acctHoldScrollPane);
		
		SummaryActivity sumAct = new SummaryActivity();
		sumAct.open();
		acctHoldAndSumBox.getChildren().add(sumAct.getRoot());
		
		ButtonToolbarActivity buttonToolbarActivity = new ButtonToolbarActivity();
		String buttonId = EventManager.getUniqueID(buttonToolbarActivity);
		EventListener centerPaneListener = EventManager.addListener(this, e -> {
			
			EventListener centerContentListener = e2 -> {
				Pane result = (Pane) e2.getData();
				if (result != null) {
					result.setPrefWidth(entryListWidth - scrollbarWidth);
				}
				acctPane = result;
				Platform.runLater(() -> accountScrollPane.setContent(acctPane));
			};
			
			acctHoldBox.getChildren().clear();
			for (Category cat: AccountHolder.getAllCategories()) {
				AccountHolderListView accountHolderListView = new AccountHolderListView(cat);
				String acctHoldListViewId = EventManager.getUniqueID(accountHolderListView);
				EventManager.addListener(accountHolderListView, centerContentListener, ACTIVITY_RESULT_OBTAINED, acctHoldListViewId);
				accountHolderListView.open();
				acctHoldBox.getChildren().add(accountHolderListView.getRoot());
			}
			accountScrollPane.setContent(acctPane);
			
		}, ACTIVITY_RESULT_OBTAINED, buttonId);
		buttonToolbarActivity.open();
		centerPaneListener.onEvent(new Event(ACTIVITY_RESULT_OBTAINED));
		
		mainPane.setTop(buttonToolbarActivity.getRoot());
		mainPane.setLeft(acctHoldAndSumBox);
		mainPane.setCenter(accountScrollPane);
		
		Scene scene = new Scene(mainPane);
		
		scene.getStylesheets().add(FileHandler.getStyle("stylesheet.css").toExternalForm());
		mainStage.setScene(scene);
		mainStage.setMinWidth(mainStageMinWidth);
		mainStage.setMinHeight(mainStageMinHeight);
		mainStage.setMaximized(true);
		mainStage.setTitle("Finance Manager v0.2.0");
		
		UserSelectionActivity userSelectionActivity = new UserSelectionActivity();
		String id = EventManager.getUniqueID(userSelectionActivity);
		
		EventManager.addListener(null, e -> {
			
			boolean userSelected = (boolean) e.getData();
			if (userSelected) {
				FileHandler fileHandler = FileHandler.getInstance();
				fileHandler.loadRecords();
			} else {
				Launcher.exitImmediately();
			}
			EventManager.removeThisListener();
			
		}, FXActivity.ACTIVITY_RESULT_OBTAINED, id);
		EventManager.addListener(null, e -> {
			
			if (User.getCurrentUser() == null) {
				Launcher.exitImmediately();
			}
			
		}, FXPopupActivity.POPUP_STAGE_CLOSED, id);
		
		userSelectionActivity.open();
		
		if (User.getCurrentUser() == null) {
			mainStage.close();
		} else {
			mainStage.show();
		}
		
	}

	@Override
	protected void callLoader() {
		initializeActivity();
	}
}
