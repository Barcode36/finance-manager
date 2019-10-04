package com.ccacic.financemanager.controller.view;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.controller.account.AccountActivity;
import com.ccacic.financemanager.controller.accountholder.AccountHolderActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.ReadOnlyList;
import com.ccacic.financemanager.model.account.Account;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A view for displaying an AccountHolder in a detailed
 * and editable manner within a seperate container
 * @author Cameron
 *
 */
public class AccountHolderExpandedView extends FXActivity<VBox> implements EventListener {
	
	@FXML
	private HBox acctHoldNameBox;
	@FXML
	private HBox acctHoldOptButtonBox;
	@FXML
	private Text nameText;
	@FXML
	private Text amountText;
	@FXML
	private Button addAccountButton;
	@FXML
	private Button editAccountButton;
	@FXML
	private Button deleteAccountButton;
	@FXML
	private Button editAcctHoldButton;
	@FXML
	private Button deleteAcctHoldButton;
	
	private AccountHolder aH;
	
	/**
	 * Creates a new AccountHolderExpandedView
	 * @param aH the AccountHolder to display
	 */
	public AccountHolderExpandedView(AccountHolder aH) {
		this.aH = aH;
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("view_account_holder_expanded.fxml"), new VBox());
	}
	
	@Override
	protected void initializeActivity() {
		
		String aHId = EventManager.getUniqueID(aH);
		EventManager.addListener(getRoot(), this, Event.UPDATE, aHId);
		onEvent(null);
		
		setColor(acctHoldNameBox, aH.getPrimaryColor());
		setColor(acctHoldOptButtonBox, aH.getPrimaryColor());
		
		nameText.setText(aH.getName());
		
		addAccountButton.setOnAction(e -> {

			AccountActivity acctAct = new AccountActivity(aH, null);
			String acctActId = EventManager.getUniqueID(acctAct);
			EventManager.addListener(this, e2 -> {
				
				Account account = (Account) e2.getData();
				AccountBarView acctBarAct = new AccountBarView(account, aH);
				acctBarAct.open(acctBarAct::toggleEntryChunks);
				
				EventManager.addListener(account, e3 -> Platform.runLater(() -> {
					Account a = (Account) e3.getData();
					if (account.equals(a)) {
						getRoot().getChildren().remove(acctBarAct.getRoot());
					}
				}), Event.DELETE_ACCOUNT, aHId);
				
				Platform.runLater(() -> getRoot().getChildren().add(acctBarAct.getRoot()));
				
				String id = EventManager.getUniqueID(this);
				EventManager.fireEvent(new Event(Event.UPDATE, id));
				EventManager.removeThisListener();
				
			}, ACTIVITY_RESULT_OBTAINED, acctActId);
			acctAct.open();
			
		});
		
		editAcctHoldButton.setOnAction(e -> {

			AccountHolderActivity acctHoldAct = new AccountHolderActivity(aH.getCategory(), aH);
			String aHAcctId = EventManager.getUniqueID(acctHoldAct);
			EventManager.addListener(this, e2 -> {
				aH = (AccountHolder) e2.getData();
				String id = EventManager.getUniqueID(this);
				EventManager.fireEvent(new Event(Event.UPDATE, id));
				EventManager.removeThisListener();
			}, ACTIVITY_RESULT_OBTAINED, aHAcctId);
			acctHoldAct.open();
			
		});
		
		deleteAcctHoldButton.setOnAction(e -> {

			String id = EventManager.getUniqueID(this);
			EventManager.fireEvent(new Event(Event.UPDATE, id));
			String acctHoldId = EventManager.getUniqueID(aH);
			EventManager.fireEvent(new Event(Event.DELETE_ACCT_HOLDER, aH, acctHoldId));

		});
		
		final ReadOnlyList<Account> accts = aH.getAccounts();
		for (Account a: accts) {
			
			AccountBarView acctBarAct = new AccountBarView(a, aH);
			acctBarAct.open();
			
			EventManager.addListener(a, e -> Platform.runLater(() -> {
				Account account = (Account) e.getData();
				if (a.equals(account)) {
					getRoot().getChildren().remove(acctBarAct.getRoot());
				}
			}), Event.DELETE_ACCOUNT, aHId);
			
			getRoot().getChildren().add(acctBarAct.getRoot());
			
		}
		
		String id = EventManager.getUniqueID(this);
		EventManager.fireEvent(new Event(ACTIVITY_RESULT_OBTAINED, id));
	}

	@Override
	public void onEvent(Event event) {
		double total = 0;
		for (Account a: aH.getAccounts()) {
			total += a.getTotal(aH.getMainCurr());
		}
		final double finalTotal = total;
		Platform.runLater(() -> amountText.setText(aH.getMainCurr().format(finalTotal)));
	}
	
}
