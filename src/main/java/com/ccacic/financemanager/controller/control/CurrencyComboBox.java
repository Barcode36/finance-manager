package com.ccacic.financemanager.controller.control;

import com.ccacic.financemanager.model.currency.Currency;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * A ComboBox for displaying Currency
 * @author Cameron Cacic
 *
 */
public class CurrencyComboBox extends ComboBox<Currency> {
	
	private static final String NULL_CURR_ERROR_MSG = "NULL_CURR";

	/**
	 * Creates a new CurrencyComboBox
	 */
	public CurrencyComboBox() {
		super();
		
		setConverter(new StringConverter<Currency>() {
			
			@Override
			public String toString(Currency curr) {
				return formatCurr(curr);
			}
			
			@Override
			public Currency fromString(String string) {
				return null;
			}
			
		});
	}
	
	/**
	 * Formats the given Currency into a displayable String
	 * @param curr the Currency to format
	 * @return a formatted String representation of the Currency
	 */
	private String formatCurr(Currency curr) {
		if (curr != null) {
			String symbol = curr.getSymbol();
			if (symbol != null) {
				if (symbol.contains(curr.getCode())) {
					return symbol;
				}
				return curr.getCode() + " " + symbol;
			} else {
				return curr.getCode();
			}
		} else {
			return NULL_CURR_ERROR_MSG;
		}
	}
	
}
