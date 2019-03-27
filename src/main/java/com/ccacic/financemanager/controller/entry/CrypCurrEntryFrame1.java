package com.ccacic.financemanager.controller.entry;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.control.CurrencyTextField;
import com.ccacic.financemanager.controller.control.LimitedTextField;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntry;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntryAssembler;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * The first frame for CrypCurrEntry
 * @author Cameron Cacic
 *
 */
public class CrypCurrEntryFrame1 extends FXPopupProgActivityFrame<CrypCurrEntry, VBox> {

	@FXML
	private LimitedTextField transField;
	@FXML
	private CurrencyTextField histPriceField;
	@FXML
	private Text valueText;

	private CrypCurrEntry toEdit;
	//private StringProperty amountProperty;
	
	/**
	 * Creates a new CrypCurrEntryFrame1
	 * @param toEdit the CrypCurrEntry to edit, or null to create a new one
	 */
	public CrypCurrEntryFrame1(CrypCurrEntry toEdit) {
		this.toEdit = toEdit;
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_crypcurrentry.fxml"), new VBox());
		getRoot().setSpacing(5);
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		
		paramMap.put(CrypCurrEntryAssembler.TRANSACTION_ID, transField.getText());
		paramMap.put(CrypCurrEntryAssembler.HISTORIC_FIAT_PRICE, histPriceField.getValue() + "");
		
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		transField.setText(toEdit != null ? toEdit.getTransactID() : "");
		
		Currency defaultCurr = Currency.getDefaultCurrency();
		histPriceField.setCurrency(defaultCurr);
		if (toEdit != null) {
			histPriceField.setValue(toEdit.getHistFiatValue());
		}
		
		ChangeListener<String> updateValueText = new ChangeListener<String>() {
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					valueText.setText(histPriceField.getValue() + "");
				} catch (NumberFormatException ex) {
					valueText.setText("Invalid price");
				}
			}
			
		};
		histPriceField.textProperty().addListener(updateValueText);
	}

}
