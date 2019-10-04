package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivity;
import com.ccacic.financemanager.event.ChangeEvent;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.model.AccountHolder;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountFactory;
import javafx.application.Platform;

/**
 * A progression activity for building a new Account or editing an 
 * existing Account
 * @author Cameron Cacic
 *
 */
public class AccountActivity extends FXPopupProgActivity<Account> {
	
	private Account toEdit;
	private final String acctHoldIdentifier;
	
	/**
	 * Creates a new AccountActivity
	 * @param owner the AccountHolder that owns or will own the Account
	 * @param toEdit the Account to edit, or null to create a new Account
	 */
	public AccountActivity(AccountHolder owner, Account toEdit) {
		this.toEdit = toEdit;
		this.acctHoldIdentifier = EventManager.getUniqueID(owner);
		
		List<String> holdableAcctList = new ArrayList<>(
				AccountHolder.getCategory(owner.getCategory()).getHoldableAccounts());
		holdableAcctList.sort(null);
		String key = toEdit != null ? toEdit.getClass().getSimpleName() : holdableAcctList.get(0);
		
		AccountFrame accountFrame = new AccountFrame(owner, toEdit);
		String acctTypeChangeID = EventManager.getUniqueID(accountFrame);
		EventManager.addListener(this, e -> {
			
			getRoot().setCenter(accountFrame.getRoot());
			index = 0;
			frames.clear();
			frames.add(accountFrame);
			FXAccountFrameContainer fxAcctFactory = FXAccountFrameContainer.getInstance();
			frames.addAll(fxAcctFactory.getFrameList((String) e.getData(), toEdit));
			Platform.runLater(this::initializeActivity);
			
		}, ACTIVITY_RESULT_OBTAINED, acctTypeChangeID);
		
		frames.add(accountFrame);
		FXAccountFrameContainer fxAcctFrameContainer = FXAccountFrameContainer.getInstance();
		frames.addAll(fxAcctFrameContainer.getFrameList(key, toEdit));
	}
	
	@Override
	protected void callPopupLoader() {
		popupStage.setTitle(toEdit != null ? "Save Account Changes" : "Create New Account");
	}
	
	@Override
	protected void initializeActivity() {
		finishButton.setText(toEdit != null ? "Save Account Changes" : "Create Account");
		super.initializeActivity();
	}

	@Override
	protected Account createResult(ParamMap frameValues) {
		AccountFactory factory = AccountFactory.getInstance();
		if (toEdit != null) {
			Delta delta = factory.modifyItem(toEdit, frameValues);
			String acctId = EventManager.getUniqueID(toEdit);
			EventManager.fireEvent(new ChangeEvent(delta, acctId));
		} else {
			toEdit = factory.requestItem(frameValues);
			if (acctHoldIdentifier != null) {
				EventManager.fireEvent(new Event(Event.NEW_ACCOUNT, toEdit, acctHoldIdentifier));
			}
		}
		return toEdit;
	}
}
