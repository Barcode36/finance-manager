package com.ccacic.financemanager.controller.account;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.children.SavingsAccount;
import com.ccacic.financemanager.model.account.children.SavingsAccountAssembler;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * The first frame for SavingsAccount
 * @author Cameron Cacic
 *
 */
public class SavingsAccountFrame1 extends FXPopupProgActivityFrame<Object, VBox> {

	@FXML
	private TextField apyField;
	
	private final SavingsAccount toEdit;
	
	/**
	 * Creates a new SavingsAccountFrame1
	 * @param toEdit the Account to edit, or null to create a new Account
	 */
	public SavingsAccountFrame1(SavingsAccount toEdit) {
		this.toEdit = toEdit;
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_savingsaccount_1.fxml"), new VBox());
		getRoot().setSpacing(5);
		getRoot().setAlignment(Pos.TOP_LEFT);
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(SavingsAccountAssembler.APY, apyField.getText());
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		apyField.setText(toEdit != null ? toEdit.getAPY() + "" : "0.00");
	}
}
