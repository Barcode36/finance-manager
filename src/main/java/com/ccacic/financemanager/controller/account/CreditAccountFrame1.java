package com.ccacic.financemanager.controller.account;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.control.DoubleTextField;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.children.CreditAccount;
import com.ccacic.financemanager.model.account.children.CreditAccountAssembler;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * The first frame for CreditAccount
 * @author Cameron Cacic
 *
 */
public class CreditAccountFrame1 extends FXPopupProgActivityFrame<Object, VBox> {
	
	@FXML
	private DoubleTextField creditLineField;
	@FXML
	private DoubleTextField annualFeeField;
	@FXML
	private DoubleTextField aprField;
	@FXML
	private DoubleTextField minPaymentField;
	
	private CreditAccount toEdit;
	
	/**
	 * Creates a new CreditAccountFrame1
	 * @param toEdit the CreditAccount to edit, or null to create a new one
	 */
	public CreditAccountFrame1(CreditAccount toEdit) {
		this.toEdit = toEdit;
	}

	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(CreditAccountAssembler.CREDIT_LINE, creditLineField.getText());
		paramMap.put(CreditAccountAssembler.ANNUAL_FEE, annualFeeField.getText());
		paramMap.put(CreditAccountAssembler.APR, aprField.getText());
		paramMap.put(CreditAccountAssembler.MIN_PAYMENT, minPaymentField.getText());
		return paramMap;
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_creditaccount_1.fxml"), new VBox());
		getRoot().setSpacing(5);
		getRoot().setAlignment(Pos.TOP_LEFT);
	}

	@Override
	protected void initializeActivity() {
		creditLineField.setText(toEdit != null ? toEdit.getCreditLine() + "" : "0");
		annualFeeField.setText(toEdit != null ? toEdit.getAnnualFee() + "" : "0");
		aprField.setText(toEdit != null ? toEdit.getAPR() + "" : "0");
		minPaymentField.setText(toEdit != null ? toEdit.getMinimumPayment() + "" : "0");
	}

}
