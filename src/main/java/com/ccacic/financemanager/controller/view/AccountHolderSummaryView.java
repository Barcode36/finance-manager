package com.ccacic.financemanager.controller.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A view for displaying a concise view of an AccountHolder. Shows
 * a count of account types, amount within each account type, and
 * a total amount for the AccountHolder
 * @author Cameron Cacic
 *
 */
public class AccountHolderSummaryView extends FXActivity<BorderPane> implements EventListener {
	
	@FXML
	private Text nameText;
	@FXML
	private VBox acctTypesBox;
	@FXML
	private Text totalText;

	private final AccountHolder aH;
	
	/**
	 * Creates a new AccountHolderSummaryView
	 * @param aH the AccountHolder to display
	 */
	public AccountHolderSummaryView(AccountHolder aH) {
		this.aH = aH;
	}
	
	@Override
	protected void initializeActivity() {
		
		nameText.setText(aH.getName());
		String eventAcctHoldID = EventManager.getUniqueID(aH);
		EventManager.addListener(aH, this, Event.UPDATE, eventAcctHoldID);
		onEvent(null);
		
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("view_account_holder_summary.fxml"), new BorderPane());
		setColor(getRoot(), aH.getSecondaryColor());
	}

	@Override
	public void onEvent(Event event) {
		AccountFactory accountFactory = AccountFactory.getInstance();
		
		final double total = aH.getTotal();
		
		Map<String, Integer> typeCountMap = new HashMap<>();
		for (Account a: aH.getAccounts()) {
			typeCountMap.merge(a.getClass().getSimpleName(), 1, Integer::sum);
		}
		List<Node> newAcctTexts = new ArrayList<>();
		for (String key: typeCountMap.keySet()) {
			int count = typeCountMap.get(key);
			Text acctText = new Text(count + " " + accountFactory.getDisplayName(key) + (count > 1 ? "s" : ""));
			acctText.setFont(Font.font("Calibri", FontWeight.NORMAL, 24));
			acctText.setFill(Color.WHITE);
			newAcctTexts.add(acctText);
		}
		
		Platform.runLater(() -> {
			acctTypesBox.getChildren().clear();
			totalText.setText(aH.getMainCurr().format(total));
			acctTypesBox.getChildren().addAll(newAcctTexts);
		});
	}

}
