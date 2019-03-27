package com.ccacic.financemanager.model.account.children;

import com.ccacic.financemanager.model.account.Account;

/**
 * An implementation of Account for checking accounts. Adds an APY field
 * @author Cameron Cacic
 *
 */
public class CheckingAccount extends Account {
	
	private double APY;
	
	/**
	 * Assembler method for setting the APY
	 * @param APY the APY
	 * @return this CheckingAccount, for chaining
	 */
	public CheckingAccount APY(double APY) {
		this.APY = APY;
		return this;
	}
	
	/**
	 * Returns the APY
	 * @return the APY
	 */
	public double getAPY() {
		return APY;
	}
	
}
