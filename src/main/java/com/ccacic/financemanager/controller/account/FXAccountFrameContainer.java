package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXFrameContainer;
import com.ccacic.financemanager.model.account.Account;

import javafx.scene.Parent;

/**
 * The frame container for Accounts in general
 * @author Cameron Cacic
 *
 */
public class FXAccountFrameContainer extends FXFrameContainer<Account> {
	
	private static FXAccountFrameContainer instance = new FXAccountFrameContainer();
	
	/**
	 * Returns the instance
	 * @return the instance
	 */
	public static FXAccountFrameContainer getInstance() {
		return instance;
	}
	
	/**
	 * Prevents external instantiation
	 */
	private FXAccountFrameContainer() {
		super();
	}
	
	/**
	 * Returns the expanded account views for the factory corresponding
	 * to the passed key
	 * @param key the key to use to find the factory
	 * @param account the account to get the views from
	 * @return
	 */
	public List<Parent> getExpandedViewAddOns(String key, Account account) {
		FXAccountFactory<Account> factory = (FXAccountFactory<Account>) frameListMap.get(key);
		if (factory == null) {
			return new ArrayList<>();
		}
		return factory.createAcctExpandedViewAddOnList(account);
	}

}
