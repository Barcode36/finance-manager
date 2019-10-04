package com.ccacic.financemanager.controller.account;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.children.StockAccount;
import com.ccacic.financemanager.model.account.children.StockAccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * The first frame for StockAccount
 * @author Cameron Cacic
 *
 */
public class StockAccountFrame1 extends FXPopupProgActivityFrame<Void, VBox>{

	@FXML
	private TextField tickerField;
	@FXML
	private TextField yieldField;
	
	private final StockAccount toEdit;
	
	/**
	 * Creates a new StockAccountFrame1
	 * @param toEdit the Account to edit, or null to create a new Account
	 */
	public StockAccountFrame1(StockAccount toEdit) {
		this.toEdit = toEdit;
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_stockaccount_1.fxml"), new VBox());
		getRoot().setSpacing(5);
		getRoot().setAlignment(Pos.TOP_LEFT);
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(StockAccountAssembler.TICKER, tickerField.getText());
		paramMap.put(StockAccountAssembler.YIELD, yieldField.getText());
		Currency.createNewStockCurrency(tickerField.getText());
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		tickerField.setText(toEdit != null ? toEdit.getTicker() : "");
		yieldField.setText(toEdit != null ? toEdit.getYield() + "" : "");
	}
	
}
