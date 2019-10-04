package com.ccacic.financemanager.controller.view;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.controller.accountholder.AccountHolderActivity;
import com.ccacic.financemanager.controller.control.CurrencyText;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.Category;
import com.ccacic.financemanager.model.currency.Currency;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;

/**
 * A view for displaying all the AccountHolders within a Category
 * in a seperate container
 * @author Cameron Cacic
 *
 */
public class AccountHolderListView extends FXActivity<VBox> implements EventListener {
	
	private static final int BUTTONS_IN_ROW_COUNT = 2;
	
	private static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	private static final double accountButtonWidth = 0.16 * screenBounds.getWidth();
	private static final double accountButtonHeight = 0.12 * screenBounds.getHeight();
	
	@FXML
	private HBox categoryBar;
	@FXML
	private Text categoryNameText;
	@FXML
	private CurrencyText categoryTotalText;
	@FXML
	private Button categoryAddButton;
	@FXML
	private GridPane buttonGridPane;
	@FXML
	private Pane seperatorPane;
	
	private final Category category;
	
	/**
	 * Creates a new AccountHolderListView
	 * @param category the category to display AccountHolders for
	 */
	public AccountHolderListView(Category category) {
		this.category = category;
	}

	@Override
	protected void initializeActivity() {
		setColor(categoryBar, category.getPrimaryColor());
		
		categoryNameText.setText(category.getDisplayName());
		categoryTotalText.setCurrency(Currency.getDefaultCurrency());
		
		categoryAddButton.setOnAction(e -> {
			
	    	AccountHolderActivity acctHoldAct = new AccountHolderActivity(category.getName(), null);
	    	String acctHoldid = EventManager.getUniqueID(acctHoldAct);
	    	acctHoldAct.open();
		    
		});
		
		EventManager.addListener(getRoot(), e -> {
			AccountHolder accountHolder = (AccountHolder) e.getData();
			if (accountHolder.getCategory().equals(category.getName()))
				addAcctHoldButton(accountHolder);
	    		String catId = EventManager.getUniqueID(category);
	    		EventManager.fireEvent(new Event(Event.UPDATE, catId));
		}, Event.NEW_ACCT_HOLDER);
		
		seperatorPane.managedProperty().bind(Bindings.size(buttonGridPane.getChildren()).greaterThan(0));
		
		for (AccountHolder aH: AccountHolder.getAccountHolders()) {
			if (aH.getCategory().equals(category.getName())) {
				addAcctHoldButton(aH);
			}
		}
		
		shuffleButtons(0);
		
	}
	
	private void addAcctHoldButton(AccountHolder aH) {
		
		final String id = EventManager.getUniqueID(this);
		
		Button button = new Button();
		button.setPrefWidth(accountButtonWidth);
		button.setPrefHeight(accountButtonHeight);
		button.setOnAction(e -> {
    			
			AccountHolderExpandedView accountHolderExpandedView = new AccountHolderExpandedView(aH);
			accountHolderExpandedView.open();
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, accountHolderExpandedView.getRoot(), id));
    		
    	});
		
		AccountHolderSummaryView accountHolderSummaryView = new AccountHolderSummaryView(aH);
		accountHolderSummaryView.open();
		button.setGraphic(accountHolderSummaryView.getRoot());
		
		String acctHoldId = EventManager.getUniqueID(aH);
		EventManager.addListener(aH, e2 -> {
			
			Platform.runLater(() -> {
				synchronized (buttonGridPane.getChildren()) {
					int index = buttonGridPane.getChildren().indexOf(button);
					buttonGridPane.getChildren().remove(index);
					shuffleButtons(index);
				}
				categoryTotalText.subtractAmount(aH.getTotal());
			});
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, null, id));
			
		}, Event.DELETE_ACCT_HOLDER, acctHoldId);
		
		EventManager.addListener(aH, this, Event.UPDATE, acctHoldId);
		
		GridPane.setConstraints(button, buttonGridPane.getChildren().size() % BUTTONS_IN_ROW_COUNT, 
				buttonGridPane.getChildren().size() / BUTTONS_IN_ROW_COUNT);
		
		Runnable addChildren = () -> {
			buttonGridPane.getChildren().add(button);
			categoryTotalText.addAmount(aH.getTotal());
		};
		
		if (Platform.isFxApplicationThread()) {
			addChildren.run();
		} else {
			Platform.runLater(addChildren);
		}
		
	}
	
	/**
	 * Maintains ordering of the buttons containing AccountHolderSummaryViews
	 * within the GridPane. Sets the contraints of the children ofthe GridPane
	 * from the starting point to match their propoer positioning. Should be called
	 * after every child is added or removed from the GridPane at the index
	 * it used to be it.
	 * @param start the index to start processing at
	 */
	private void shuffleButtons(int start) {
		
		synchronized (buttonGridPane.getChildren()) {
			for (int i = start; i < buttonGridPane.getChildren().size(); i++) {
				GridPane.setConstraints(buttonGridPane.getChildren().get(i), i % BUTTONS_IN_ROW_COUNT, i / BUTTONS_IN_ROW_COUNT);
			}
		}
		
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("view_account_holder_list.fxml"), new VBox());
	}

	@Override
	public void onEvent(Event event) {
		double total = 0;
		for (AccountHolder aH: AccountHolder.getAccountHolders()) {
			if (aH.getCategory().equals(category.getName())) {
				total += aH.getTotal();
			}
		}
		categoryTotalText.setAmount(total);
	}
	
}
