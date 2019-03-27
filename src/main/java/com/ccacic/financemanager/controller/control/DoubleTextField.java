package com.ccacic.financemanager.controller.control;

import com.ccacic.financemanager.logger.Logger;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextField;

/**
 * A TextField for displaying only double values
 * @author Cameron Cacic
 *
 */
public class DoubleTextField extends TextField {
	
	private final DoubleProperty doubleValue;
	
	/**
	 * Creates a new DoubleTextField
	 */
	public DoubleTextField() {
		doubleValue = new SimpleDoubleProperty(Double.NaN);
		focusedProperty().addListener((observable, oldVal, newVal) -> {
			
			if (newVal) {
				Platform.runLater(() -> selectAll());
			}
			
		});
	}
	
	/**
	 * Returns the doubleValueProperty
	 * @return the doubleValueProperty
	 */
	public DoubleProperty doubleValueProperty() {
		return doubleValue;
	}
	
	/**
	 * Returns the current double value
	 * @return the current double value
	 */
	public final double getDoubleValue() {
		return doubleValue.doubleValue();
	}
	
	/**
	 * Sets the double value to the passed value
	 * @param value the new value
	 */
	public final void setDoubleValue(double value) {
		doubleValue.set(value);
		if (value == Double.NaN) {
			super.setText("");
		} else {
			super.setText(value + "");
		}
	}
	
	@Override
    public void replaceText(int start, int end, String insertedText) {
		String currentText = this.getText() == null ? "" : this.getText();
		String finalText = currentText.substring(0, start) + insertedText + currentText.substring(end);
		try {
			double newValue;
			if (".".equals(finalText)) {
				insertedText = "0.";
				newValue = 0.0;
			} else {
				newValue = Double.parseDouble(finalText);
			}
			doubleValue.set(newValue);
			super.replaceText(start, end, insertedText);
		} catch (NumberFormatException e) {
			Logger.getInstance().logInfo("Non-number inserted into a DoubleTextField, rejecting the change");
		}
	}

}
