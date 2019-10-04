package com.ccacic.financemanager.controller.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;   

/**
 * A TextField that limits the length of Strings it can hold
 * @author Cameron Cacic
 *
 */
public class LimitedTextField extends TextField {

    private final IntegerProperty maxLength;

    /**
     * Creates a new LimitedTextField
     */
    public LimitedTextField() {
        super();
        this.maxLength = new SimpleIntegerProperty(-1);
    }

    /**
     * Returns the maxLengthProperty
     * @return the maxLengthProperty
     */
    public IntegerProperty maxLengthProperty() {
        return this.maxLength;
    }

    /**
     * Returns the max length
     * @return the max length
     */
    private int getMaxLength() {
        return this.maxLength.getValue();
    }

    /**
     * Sets the max length
     * @param maxLength the max length
     */
    public final void setMaxLength(int maxLength) {
        this.maxLength.setValue(maxLength);
    }

    @Override
    public void replaceText(int start, int end, String insertedText) {
        if (this.getMaxLength() <= 0) {
            // Default behavior, in case of no max length
            super.replaceText(start, end, insertedText);
        }
        else {
            // Get the text in the textfield, before the user enters something
            String currentText = this.getText() == null ? "" : this.getText();

            // Compute the text that should normally be in the textfield now
            String finalText = currentText.substring(0, start) + insertedText + currentText.substring(end);

            // If the max length is not excedeed
            int numberOfexceedingCharacters = finalText.length() - this.getMaxLength();
            if (numberOfexceedingCharacters <= 0) {
                // Normal behavior
                super.replaceText(start, end, insertedText);
            }
            else {
                // Otherwise, cut the the text that was going to be inserted
                String cutInsertedText = insertedText.substring(
                        0, 
                        insertedText.length() - numberOfexceedingCharacters
                );

                // And replace this text
                super.replaceText(start, end, cutInsertedText);
            }
        }
    }
}
