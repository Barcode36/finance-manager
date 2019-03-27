package com.ccacic.financemanager.controller.account;

import java.util.List;

import com.ccacic.financemanager.controller.FXFrameFactory;
import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.account.Account;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/**
 * The FXFactory for Accounts in general
 * @author Cameron Cacic
 *
 * @param <T> the type of Account
 */
public abstract class FXAccountFactory<T extends Account> extends FXFrameFactory<Account> {

	/**
	 * Creates a new FXAccountFactory with the given name
	 * @param name the name of the facotry
	 */
	public FXAccountFactory(String name) {
		super(name);
	}
	
	/**
	 * Creates a list of frames needed to build an Account of type T
	 * @param toEdit the Account to edit, or null to create a new Account
	 * @return a list of activity frames
	 */
	public abstract List<FXPopupProgActivityFrame<?, ? extends Pane>> createAcctFrameList(T toEdit);
	
	/**
	 * This method allows Account children to inject views into the
	 * expanded account view section of the view, to display type
	 * specific data easily. All Parents in the returned list will be
	 * added to the view
	 * @param account the Account to build the views from
	 * @return a list of account extended views
	 */
	public abstract List<Parent> createAcctExpandedViewAddOnList(T account);

	@SuppressWarnings("unchecked")
	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createFrameList(Account toEdit) {
		return createAcctFrameList((T) toEdit);
	}

}
