package com.ccacic.financemanager.model.account.children;

import com.ccacic.financemanager.model.account.Account;

/**
 * An implementation of Account for representing lines of credit. Adds
 * a credit line, annual fee, APR, and minimum payment fields. Total
 * should be interpreted as outstanding debt
 * @author Cameron Cacic
 *
 */
public class CreditAccount extends Account {
	
	private double creditLine;
	private double annualFee;
	private double apr;
	private double minimumPayment;
	
	/**
	 * Assembler method for setting the creditLine
	 * @param creditLine the credit line
	 * @return this CreditAccount, for chaining
	 */
	public CreditAccount creditLine(double creditLine) {
		this.creditLine = creditLine;
		return this;
	}
	
	/**
	 * Assembler method for setting the annualFee
	 * @param annualFee the annual fee
	 * @return this CreditAccount, for chaining
	 */
	public CreditAccount annualFee(double annualFee) {
		this.annualFee = annualFee;
		return this;
	}
	
	/**
	 * Assembler method for setting the APR
	 * @param apr the APR
	 * @return this CreditAccount, for chaining
	 */
	public CreditAccount apr(double apr) {
		this.apr = apr;
		return this;
	}
	
	/**
	 * Assembler method for setting the minimumPayment
	 * @param minimumPayment the minimum payment
	 * @return this CreditAccount, for chaining
	 */
	public CreditAccount minimumPayment(double minimumPayment) {
		this.minimumPayment = minimumPayment;
		return this;
	}
	
	/**
	 * Returns the credit line
	 * @return the credit line
	 */
	public double getCreditLine() {
		return creditLine;
	}
	
	/**
	 * Returns the annual fee
	 * @return the annual fee
	 */
	public double getAnnualFee() {
		return annualFee;
	}
	
	/**
	 * Returns the APR
	 * @return the APR
	 */
	public double getAPR() {
		return apr;
	}
	
	/**
	 * Returns the minimum payment
	 * @return the minimum payment
	 */
	public double getMinimumPayment() {
		return minimumPayment;
	}

}
