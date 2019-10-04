package com.ccacic.financemanager.controller.accountholder;

import java.util.ArrayList;

import com.ccacic.financemanager.controller.FXPopupActivity;
import com.ccacic.financemanager.controller.control.CurrencyComboBox;
import com.ccacic.financemanager.controller.control.LimitedTextField;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.currency.Currency;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * A popup activity for creating and editing AccountHolders
 * @author Cameron Cacic
 *
 */
public class AccountHolderActivity extends FXPopupActivity<VBox> {
	
	private static final int MAX_NAME_LENGTH = 40;
	
	@FXML 
	private LimitedTextField nameTextField;
	@FXML 
	private CurrencyComboBox currCombo;
	@FXML
	private Button createAcctHoldButton;
	@FXML
	private Button cancelButton;
	
	private final String cat;
	private AccountHolder toEdit;
	
	/**
	 * Creates a new AccountHolderActivity
	 * @param cat the category of the AccountHolder
	 * @param toEdit the AccountHolder to edit, or null to create a new AccountHolder
	 */
	public AccountHolderActivity(String cat, AccountHolder toEdit) {
		this.toEdit = toEdit;
		this.cat = cat;
	}
	
	@Override
	protected void callPopupLoader() {
		load(FileHandler.getLayout("activity_account_holder.fxml"), new VBox());
		getRoot().setSpacing(5);
		popupStage.setTitle(toEdit != null ? "Edit Account Holder" : "Create New Account Holder");
	}
	
	@Override
	protected void initializeActivity() {
		
		nameTextField.setText(toEdit != null ? toEdit.getName() : "");
		nameTextField.setMaxLength(MAX_NAME_LENGTH);
		currCombo.setItems(FXCollections.observableArrayList(Currency.getAllCurrencies()));
		if (toEdit != null) {
			currCombo.getSelectionModel().select(toEdit.getMainCurr());
		} else {
			currCombo.getSelectionModel().select(Currency.getDefaultCurrency());
		}
		
		final ObservableList<Account> accounts = FXCollections.observableArrayList();
		if (toEdit != null) {
			ReadOnlyList.addAll(accounts, toEdit.getAccounts());
		}
		
		createAcctHoldButton.setText(toEdit != null ? "Save Account Holder Changes" : "Create Account Holder");
		createAcctHoldButton.setOnAction(e -> {
			
			AccountHolder accountHolder = new AccountHolder(null, nameTextField.getText(), cat,
					currCombo.getSelectionModel().getSelectedItem(),
					new ArrayList<>(accounts));
			String acctHoldId = EventManager.getUniqueID(accountHolder);
			EventManager.fireEvent(new Event(Event.NEW_ACCT_HOLDER, accountHolder));
			String identifier = EventManager.getUniqueID(this);
			EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, accountHolder, identifier));
			popupStage.close();
			
		});
		
		cancelButton.setOnAction(e -> popupStage.close());
	}

}
