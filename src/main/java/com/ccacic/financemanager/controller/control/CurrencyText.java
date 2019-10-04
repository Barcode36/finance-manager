package com.ccacic.financemanager.controller.control;

import com.ccacic.financemanager.model.currency.Currency;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;

/**
 * A Text for displaying amounts of Currency in a formatted manner
 * @author Cameron Cacic
 *
 */
public class CurrencyText extends Text {

	private final DoubleProperty amountProperty;
	private final SimpleCurrencyProperty currencyProperty;
	
	/**
	 * Creates a new CurrencyText with a zero amount
	 */
	public CurrencyText() {
		amountProperty = new SimpleDoubleProperty(0.0);
		currencyProperty = new SimpleCurrencyProperty();
		textProperty().bind(Bindings.createStringBinding(() -> {
			
			if (currencyProperty.get() == null) {
				return amountProperty.get() + "";
			} else {
				return currencyProperty.get().format(amountProperty.get());
			}
			
		}, amountProperty, currencyProperty));
	}
	
	/**
	 * Returns the amountProperty
	 * @return the amountProperty
	 */
	public DoubleProperty amountProperty() {
		return amountProperty;
	}
	
	/**
	 * Returns the currencyProperty
	 * @return the currencyProperty
	 */
	public SimpleCurrencyProperty currencyProperty() {
		return currencyProperty;
	}
	
	/**
	 * Sets the amount to the given amount
	 * @param amount the amount
	 */
	public void setAmount(double amount) {
		amountProperty.set(amount);
	}
	
	/**
	 * Adds the passed value to the current amount
	 * @param amount the amount to add
	 */
	public void addAmount(double amount) {
		double old = amountProperty.get();
		amountProperty.set(old + amount);
	}
	
	/**
	 * Subtracts the passed value to the current amount
	 * @param amount the amount to subtract
	 */
	public void subtractAmount(double amount) {
		double old = amountProperty.get();
		amountProperty.set(old - amount);
	}
	
	/**
	 * Sets the Currency to the passed Currency
	 * @param currency the new Currency
	 */
	public void setCurrency(Currency currency) {
		currencyProperty.set(currency);
	}
	
	/**
	 * Returns the amount
	 * @return the amount
	 */
	public double getAmount() {
		return amountProperty.get();
	}
	
	/**
	 * Returns the currency
	 * @return the currency
	 */
	public Currency getCurrency() {
		return currencyProperty.get();
	}
	
}
