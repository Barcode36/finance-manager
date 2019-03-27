package com.ccacic.financemanager.controller.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccacic.financemanager.controller.FXActivity;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.tag.Tag;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Displays a summary of the user's finances, including
 * a total amount and amounts by Tag, providing a more
 * condensed summary than by category
 * @author Cameron Cacic
 *
 */
public class SummaryActivity extends FXActivity<VBox> implements EventListener {
	
	@FXML
	private Text totalText;
	@FXML
	private HBox totalBox;
	
	private Map<Tag, Text> tagAmountTextMap;
	private Map<Tag, HBox> tagAmountBoxMap;
	
	/**
	 * Creates a new SummaryActivity
	 */
	public SummaryActivity() {
		super();
		tagAmountTextMap = new HashMap<>();
		tagAmountBoxMap = new HashMap<>();
		for (AccountHolder accountHolder: AccountHolder.getAccountHolders()) {
			String id = EventManager.getUniqueID(accountHolder);
			EventManager.addListener(accountHolder, this, Event.UPDATE, id);
		}
		EventManager.addListener(null, e -> {
			AccountHolder accountHolder = (AccountHolder) e.getData();
			String id = EventManager.getUniqueID(accountHolder);
			EventManager.addListener(accountHolder, this, Event.UPDATE, id);
			onEvent(null);
		}, Event.NEW_ACCT_HOLDER);
		EventManager.addListener(null, e -> {
			onEvent(null);
		}, Event.DELETE_ACCT_HOLDER);
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("activity_summary.fxml"), new VBox());
		getRoot().setSpacing(5);
		getRoot().setAlignment(Pos.TOP_CENTER);
	}
	
	@Override
	protected void initializeActivity() {
		
		onEvent(null);
		
		totalBox.setStyle("-fx-background-color: rgb(0, 100, 180);");
		
	}

	@Override
	public void onEvent(Event event) {
		
		Currency defCurr = Currency.getDefaultCurrency();
		AccountFactory accountFactory = AccountFactory.getInstance();
		
		double total = 0;
		Map<Tag, Double> amountMap = new HashMap<>();
		for (AccountHolder aH: AccountHolder.getAccountHolders()) {
			for (Account a: aH.getAccounts()) {
				
				double value = a.getTotal(defCurr);
				total += value;
				
				for (Tag tag: accountFactory.getTags(a)) {
					
					Double prevAmount = amountMap.get(tag);
					if (prevAmount == null) {
						prevAmount = new Double(0.0);
					}
					prevAmount += value;
					if (tag.equals(Tag.DEBT)) {
						total -= value * 2;
					}
					amountMap.put(tag, prevAmount);
					
				}
			}
		}
		
		Set<Tag> duplicatedKeys = new HashSet<>(tagAmountTextMap.keySet());
		
		final double finalTotal = total;
		Platform.runLater(() -> totalText.setText(defCurr.format(finalTotal)));
		
		Font tagFont = new Font("Calibri", 28);
		List<Node> newTags = new ArrayList<>();
		for (Tag tag: amountMap.keySet()) {
			
			double amount = amountMap.get(tag);
			duplicatedKeys.remove(tag);
			
			if (tagAmountTextMap.containsKey(tag)) {
				Platform.runLater(() -> tagAmountTextMap.get(tag).setText(defCurr.format(amount)));
			} else {
			
				Text tagText = new Text(tag.getName());
				tagText.setFont(tagFont);
				tagText.setFill(Color.WHITE);
				
				Text tagAmount = new Text(defCurr.format(amount));
				tagAmount.setFont(tagFont);
				tagAmount.setFill(Color.WHITE);
				
				Pane spreader = new Pane();
				HBox.setHgrow(spreader, Priority.ALWAYS);
				
				HBox tagBox = new HBox();
				tagBox.setAlignment(Pos.CENTER);
				HBox.setHgrow(tagBox, Priority.ALWAYS);
				tagBox.setPadding(new Insets(0, 5, 0, 5));
				tagBox.setStyle("-fx-background-color: rgb(0, 146, 176);");
				tagBox.getChildren().addAll(tagText, spreader, tagAmount);
				
				newTags.add(tagBox);
				
				tagAmountTextMap.put(tag, tagAmount);
				tagAmountBoxMap.put(tag, tagBox);
				
			}
			
		}
		
		tagAmountTextMap.keySet().removeAll(duplicatedKeys);
		
		Platform.runLater(() -> {
			getRoot().getChildren().addAll(newTags);
			for (Tag removedTag: duplicatedKeys) {
				getRoot().getChildren().remove(tagAmountBoxMap.get(removedTag));
				tagAmountBoxMap.remove(removedTag);
			}
		});
	}
	
}
