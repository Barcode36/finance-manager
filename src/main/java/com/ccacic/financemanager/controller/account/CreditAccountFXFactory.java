package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.account.children.CreditAccount;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * The FXFactory for CreditAccount
 * @author Cameron Cacic
 *
 */
public class CreditAccountFXFactory extends FXAccountFactory<CreditAccount>{

	/**
	 * Creates a new CreditAccountFXFactory
	 */
	public CreditAccountFXFactory() {
		super(CreditAccount.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createAcctFrameList(CreditAccount toEdit) {
		CreditAccountFrame1 frame1 = new CreditAccountFrame1(toEdit);
		List<FXPopupProgActivityFrame<?, ? extends Pane>> list = new ArrayList<>();
		list.add(frame1);
		return list;
	}

	@Override
	public List<Parent> createAcctExpandedViewAddOnList(CreditAccount account) {
		List<Parent> list = new ArrayList<>();
		Text aprText = new Text("APR " + account.getAPR() + "%");
		aprText.setFill(Color.WHITE);
		aprText.setFont(new Font("Calibri", 28));
		HBox aprBox = new HBox();
		aprBox.setAlignment(Pos.CENTER_RIGHT);
		aprBox.setPadding(new Insets(0, 5, 0, 5));
		aprBox.getChildren().add(aprText);
		aprBox.visibleProperty().bind(aprBox.managedProperty());
		aprBox.setManaged(account.getAPR() != 0);
		list.add(aprBox);
		return list;
	}

}
