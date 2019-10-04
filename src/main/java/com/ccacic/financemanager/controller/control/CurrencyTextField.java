package com.ccacic.financemanager.controller.control;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.currency.Currency;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * A TextField for displaying a formatted Currency amount.
 * Will only allow numerical characters to be entered, along
 * with a singular '.' for decimal numbers. Will display a 0
 * if it is given empty text
 * @author Cameron Cacic
 *
 */
public class CurrencyTextField extends TextField {
	
	private final SimpleCurrencyProperty currencyProperty;
	
	/**
	 * Creates a new CurrencyTextField
	 */
	public CurrencyTextField() {
		super();
		this.currencyProperty = new SimpleCurrencyProperty();
		currencyProperty.addListener(e -> replaceText(0, 0, ""));
	}
	
	/**
	 * Returns the currencyProperty
	 * @return the currencyProperty
	 */
	public SimpleCurrencyProperty currencyProperty() {
		return currencyProperty;
	}
	
	/**
	 * Returns the current Currency
	 * @return the current Currency
	 */
	public Currency getCurrency() {
		return currencyProperty.getValue();
	}
	
	/**
	 * Sets the Currency to the provided Currency
	 * @param curr the new Currency
	 */
	public void setCurrency(Currency curr) {
		this.currencyProperty.set(curr);
	}
	
	/**
	 * Sets the value to the provided value
	 * @param value the new value
	 */
	public void setValue(double value) {
		setText(value + "");
	}
	
	/**
	 * Returns the current value
	 * @return the current value
	 */
	public double getValue() {
		return Double.parseDouble(Currency.deformat(getText()));
	}

	@Override
    public void replaceText(int start, int end, String insertedText) {
		// Get the text in the textfield, before the user enters something
        String currentText = this.getText() == null ? "" : this.getText();
        int caretPosition = getCaretPosition();

        // Compute the text that should normally be in the textfield now
        String finalText = currentText.substring(0, start) + insertedText + currentText.substring(end);
        
        try {
			String newText = currencyProperty.getValue().liveFormat(currentText, finalText);
			if (caretPosition == currentText.length()) {
				caretPosition = newText.length();
			}
			super.replaceText(0, currentText.length(), newText);
		} catch (NumberFormatException e) {
			Logger.getInstance().logDebug("Improper String for currency formatting");
		}
        final int finalCaretPos = caretPosition;
        Platform.runLater(() -> positionCaret(finalCaretPos));
	}
	
}
