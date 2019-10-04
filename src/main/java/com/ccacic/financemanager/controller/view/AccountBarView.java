package com.ccacic.financemanager.controller.view;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.controller.account.AccountActivity;
import com.ccacic.financemanager.controller.account.FXAccountFrameContainer;
import com.ccacic.financemanager.controller.control.EntryChunkManagerControl;
import com.ccacic.financemanager.controller.entry.EntryActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;

/**
 * A view for displaying an Account within a container
 * @author Cameron Cacic
 *
 */
public class AccountBarView extends FXActivity<VBox> implements EventListener {
	
	private static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	private static final double accountBarHeight = 0.04 * screenBounds.getHeight();
	
	@FXML
	private HBox acctBar;
	@FXML
	private VBox acctExpandedBar;
	@FXML
	private HBox acctTypeBar;
	@FXML
	private Text acctTypeText;
	@FXML
	private HBox acctOptButtonBar;
	@FXML
	private Button addEntryButton;
	@FXML
	private Button editEntryButton;
	@FXML
	private Button removeEntryButton;
	@FXML
	private Button editAccountButton;
	@FXML
	private Button removeAccountButton;
	@FXML
	private Text acctText;
	@FXML
	private Text amountText;
	@FXML
	private Text amountDefCurrText;
	@FXML
	private Button toggleEntriesButton;
	@FXML
	private EntryChunkManagerControl entryChunkManagerControl;
	
	private final Account a;
	private final AccountHolder aH;
	
	private final String accountType;
	private final String acctId;
	
	private boolean firstRun;
	
	/**
	 * Creates a new AccountBarView
	 * @param a the Account to display
	 * @param aH the AccountHolder owning the passed Account, used purely for configuration
	 */
	AccountBarView(Account a, AccountHolder aH) {
		this.a = a;
		this.aH = aH;
		
		accountType = a.getClass().getSimpleName();
		acctId = EventManager.getUniqueID(a);
		
		firstRun = true;
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("view_account_bar.fxml"), new VBox());
	}
	
	@Override
	protected void initializeActivity() {
		
		AccountFactory accountFactory = AccountFactory.getInstance();
		entryChunkManagerControl.setManager(a.getEntryChunkManager(), a.getCurrency(), accountFactory.getEntryType(a));
		entryChunkManagerControl.managedProperty().bind(entryChunkManagerControl.visibleProperty());
		
		acctBar.setPrefHeight(accountBarHeight);
		setColor(acctBar, aH.getSecondaryColor());
		
		setColor(acctTypeBar, aH.getSecondaryColor());
		
		acctTypeText.setText(accountFactory.getDisplayName(accountType));
		
		acctExpandedBar.managedProperty().bind(acctExpandedBar.visibleProperty());
		
		FXAccountFrameContainer aContainer = FXAccountFrameContainer.getInstance();
		acctExpandedBar.getChildren().addAll(0, aContainer.getExpandedViewAddOns(accountType, a));
		setColor(acctExpandedBar, aH.getSecondaryColor());
		
		setColor(acctOptButtonBar, aH.getSecondaryColor());
		addEntryButton.setOnAction(e -> {

			EntryActivity entryActivity = new EntryActivity(accountFactory.getEntryType(accountType), 
					a.getCurrency(), null, acctId);
			entryActivity.open();
			
		});
		
		editEntryButton.setOnAction(e -> {
			
			if (entryChunkManagerControl.getCurrentSelectionModel() != null) {
				Entry toEdit = entryChunkManagerControl.getCurrentSelectionModel().getSelectedItem();
				EntryActivity entryActivity = new EntryActivity(accountFactory.getEntryType(accountType), 
						a.getCurrency(), toEdit, acctId);
				entryActivity.open();
			}
			
		});
		
		removeEntryButton.setOnAction(e -> {

			if (entryChunkManagerControl.getCurrentSelectionModel() != null) {
				Entry removed = entryChunkManagerControl.getCurrentSelectionModel().getSelectedItem();
				EventManager.fireEvent(new Event(Event.DELETE_ENTRY, removed, acctId));
			}

		});
		
		editAccountButton.setOnAction(e -> {
			
			String acctHoldIdentifier = EventManager.getUniqueID(aH);
			AccountActivity acctAct = new AccountActivity(aH, a);
			String acctActId = EventManager.getUniqueID(acctAct);
			EventManager.addListener(acctAct, e2 -> Platform.runLater(() -> {
				acctText.setText(a.getName());
				Currency defaultCurr = Currency.getDefaultCurrency();
				amountDefCurrText.setText(a.formattedTotal(defaultCurr));
				amountText.setText(a.formattedRawTotal());
				FXAccountFrameContainer aCont = FXAccountFrameContainer.getInstance();
				int index = 0;
				for (Parent parent: aCont.getExpandedViewAddOns(accountType, a)) {
					acctExpandedBar.getChildren().set(index++, parent);
				}

			}), ACTIVITY_RESULT_OBTAINED, acctActId);
			acctAct.open();
		});
		
		String aHId = EventManager.getUniqueID(aH);
		removeAccountButton.setOnAction(e -> EventManager.fireEvent(new Event(Event.DELETE_ACCOUNT, a, aHId)));
			
		acctText.setText(a.getName());
		
		final Currency defaultCurr = Currency.getDefaultCurrency();
		amountDefCurrText.setText(a.formattedTotal(defaultCurr));
		amountText.setText(a.formattedRawTotal());
		
		amountDefCurrText.managedProperty().bind(amountDefCurrText.visibleProperty());
		amountDefCurrText.visibleProperty().bind(Bindings.createBooleanBinding(() -> !amountDefCurrText.getText().equals(amountText.getText()), amountDefCurrText.textProperty(), amountText.textProperty()));
		
		toggleEntriesButton.setOnAction(e -> toggleEntryChunks());
		
		if (firstRun) {
			EventManager.addListener(getRoot(), this, Event.UPDATE, acctId);
		}
		firstRun = false;

	}
	
	/**
	 * Toggles the visibility of all the EntryChunks being displayed as
	 * a part of this view
	 */
	public void toggleEntryChunks() {
		
		if (toggleEntriesButton.getText().equals("+")) {
			toggleEntriesButton.setText("-");
			acctExpandedBar.setVisible(true);
			entryChunkManagerControl.setVisible(true);
		} else {
			toggleEntriesButton.setText("+");
			acctExpandedBar.setVisible(false);
			entryChunkManagerControl.setVisible(false);
		}
	}
	
	@Override
	public void onEvent(Event event) {
		final Currency defaultCurr = Currency.getDefaultCurrency();
		amountText.setText(a.formattedRawTotal());
		amountDefCurrText.setText(a.formattedTotal(defaultCurr));
	}
}
