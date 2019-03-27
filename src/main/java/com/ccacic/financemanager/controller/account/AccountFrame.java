package com.ccacic.financemanager.controller.account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.control.CurrencyComboBox;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.config.GeneralConfig;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entrychunk.DateResolution;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * The base frame for collecting data for an Account
 * @author Cameron Cacic
 *
 */
public class AccountFrame extends FXPopupProgActivityFrame<String, VBox> {
	
	@FXML
	private ComboBox<String> acctTypeCombo;
	@FXML
	private ComboBox<String> exchangeCombo;
	@FXML
	private TextField nameTextField;
	@FXML
	private CurrencyComboBox currencyCombo;
	
	private Account toEdit;
	private AccountHolder owner;
	
	private List<String> holdableAcctList;
	private List<String> holdableAcctListNames;
	
	/**
	 * Creates a new AccountFrame
	 * @param owner the AccountHolder that owns or will own the Account
	 * @param toEdit the Account to edit, or null to create a new Account
	 */
	public AccountFrame(AccountHolder owner, Account toEdit) {
		this.owner = owner;
		this.toEdit = toEdit;
		
		holdableAcctList = new ArrayList<>(AccountHolder.getCategory(owner.getCategory()).getHoldableAccounts());
		holdableAcctList.sort(null);
		holdableAcctListNames = new ArrayList<>();
		AccountFactory accountFactory = AccountFactory.getInstance();
		for (String acct: holdableAcctList) {
			holdableAcctListNames.add(accountFactory.getDisplayName(acct));
		}
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("activity_account.fxml"), new VBox());
		getRoot().setSpacing(5);
	}

	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		
		paramMap.putType(holdableAcctList.get(acctTypeCombo.getSelectionModel().getSelectedIndex()));
		
		if (toEdit == null) {
			paramMap.put(AccountAssembler.TIME_CREATED, LocalDateTime.now().toString());
		} else {
			paramMap.put(AccountAssembler.TIME_CREATED, toEdit.getDateTimeCreated().toString());
		}
		paramMap.put(AccountAssembler.NAME, nameTextField.getText());
		paramMap.put(AccountAssembler.CURRENCY, currencyCombo.getSelectionModel().getSelectedItem().getCode());
		paramMap.put(AccountAssembler.EXCHANGE_ID, exchangeCombo.getSelectionModel().getSelectedItem());
		if (toEdit == null) {
			paramMap.put(AccountAssembler.ACCT_HOLD_ID, owner.getIdentifier());
			paramMap.put(AccountAssembler.ENTRY_CHUNK_IDS, new ArrayList<>());
			paramMap.put(AccountAssembler.ENTRY_CHUNK_HASHES, new ArrayList<>());
			paramMap.put(AccountAssembler.DATE_RESOLUTION, DateResolution.ANNUALY.name());
		}
		
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		AccountFactory accountFactory = AccountFactory.getInstance();
		GeneralConfig genCon = GeneralConfig.getInstance();
		
		final ObservableList<Entry> entries = FXCollections.observableArrayList();
		
		acctTypeCombo.getItems().addAll(holdableAcctListNames);
		if (toEdit != null) {
			acctTypeCombo.setDisable(true);
			acctTypeCombo.getSelectionModel().select(accountFactory.getDisplayName(toEdit));
		} else {
			acctTypeCombo.getSelectionModel().selectFirst();
		}
		
		acctTypeCombo.setOnAction((event) -> {
			
			String newAcctType = holdableAcctList.get(acctTypeCombo.getSelectionModel().getSelectedIndex());
			String resultEventIdentifier = EventManager.getUniqueID(this);
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, newAcctType, resultEventIdentifier));
			Set<Currency> currSet = accountFactory.getCurrencies(holdableAcctList.get(acctTypeCombo.getSelectionModel().getSelectedIndex()));
			currencyCombo.setItems(FXCollections.observableArrayList(currSet));
			if (currencyCombo.getItems().contains(owner.getMainCurr())) {
				currencyCombo.getSelectionModel().select(owner.getMainCurr());
			} else {
				currencyCombo.getSelectionModel().selectFirst();
			}
			
		});
		
		ObservableList<String> exchangeOptions
			= FXCollections.observableArrayList(CurrencyExchangeFactory.getInstance().getExchangeIDs());
		exchangeOptions.add("None");
		exchangeCombo.setItems(exchangeOptions);
		if (toEdit != null) {
			exchangeCombo.getSelectionModel().select(toEdit.getExchangeID());
		} else {
			exchangeCombo.getSelectionModel().selectFirst();
			if (currencyCombo.getItems().contains(owner.getMainCurr())) {
				currencyCombo.getSelectionModel().select(owner.getMainCurr());
			} else {
				currencyCombo.getSelectionModel().selectFirst();
			}
		}
		Set<Currency> currSet = accountFactory.getCurrencies(holdableAcctList.get(acctTypeCombo.getSelectionModel().getSelectedIndex()));
		currencyCombo.setItems(FXCollections.observableArrayList(currSet));
		if (toEdit != null) {
			currencyCombo.getSelectionModel().select(toEdit.getCurrency());
		} else {
			if (currencyCombo.getItems().contains(owner.getMainCurr())) {
				currencyCombo.getSelectionModel().select(owner.getMainCurr());
			} else {
				currencyCombo.getSelectionModel().selectFirst();
			}
		}
		currencyCombo.setOnAction((event) -> {
			
			if (Currency.getDefaultCurrency().equals(
					currencyCombo.getSelectionModel().getSelectedItem())) {
				exchangeCombo.setVisible(false);
			} else {
				exchangeCombo.setVisible(true);
			}
			
		});
		if (Currency.getDefaultCurrency().equals(
				currencyCombo.getSelectionModel().getSelectedItem())) { 
			exchangeCombo.setVisible(false);
		} else {
			exchangeCombo.setVisible(true);
		}
		
		nameTextField.setText(toEdit != null ? toEdit.getName() : "");
		
		if (toEdit != null) {
			ReadOnlyList.addAll(entries, toEdit.getEntryChunkManager().getEntries());
		}
		
	}

}
