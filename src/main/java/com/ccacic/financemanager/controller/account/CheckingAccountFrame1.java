package com.ccacic.financemanager.controller.account;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.control.DoubleTextField;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.children.CheckingAccount;
import com.ccacic.financemanager.model.account.children.CheckingAccountAssembler;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * The first frame for CheckingAccount
 * @author Cameron Cacic
 *
 */
public class CheckingAccountFrame1 extends FXPopupProgActivityFrame<Void, VBox> {
	
	@FXML
	private DoubleTextField apyField;
	
	private CheckingAccount toEdit;
	
	/**
	 * Creates a new CheckingAccountFrame1
	 * @param toEdit the CheckingAccount to edit, or null to create a new one
	 */
	public CheckingAccountFrame1(CheckingAccount toEdit) {
		this.toEdit = toEdit;
	}

	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_checkingaccount_1.fxml"), new VBox());
		getRoot().setSpacing(5);
		getRoot().setAlignment(Pos.TOP_LEFT);
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(CheckingAccountAssembler.APY, apyField.getText());
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		apyField.setText(toEdit != null ? toEdit.getAPY() + "" : "0.00");
	}

}
