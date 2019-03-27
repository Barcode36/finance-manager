package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.account.children.CashAccount;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * The FXFactory for CashAccount
 * @author Cameron Cacic
 *
 */
public class CashAccountFXFactory extends FXAccountFactory<CashAccount> {

	/**
	 * Creates a new CashAccountFXFactory
	 */
	public CashAccountFXFactory() {
		super(CashAccount.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createAcctFrameList(CashAccount toEdit) {
		return new ArrayList<FXPopupProgActivityFrame<?,? extends Pane>>();
	}

	@Override
	public List<Parent> createAcctExpandedViewAddOnList(CashAccount account) {
		return new ArrayList<>();
	}
	
}
