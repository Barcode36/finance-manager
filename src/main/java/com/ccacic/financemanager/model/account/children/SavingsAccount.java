package com.ccacic.financemanager.model.account.children;

import com.ccacic.financemanager.model.account.Account;

/**
 * An implementation of Account for savings accounts. Adds an APY field
 * @author Cameron Cacic
 *
 */
public class SavingsAccount extends Account {
	private double APY;
	
	/**
	 * Assembler method for setting the APY
	 * @param apy the APY
	 * @return this SavingsAccount, for chaining
	 */
	SavingsAccount APY(double apy) {
		this.APY = apy;
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
