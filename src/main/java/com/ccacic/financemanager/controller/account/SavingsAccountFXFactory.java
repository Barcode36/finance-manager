package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.account.children.SavingsAccount;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * The FXFactory for SavingsAccount
 * @author Cameron Cacic
 *
 */
public class SavingsAccountFXFactory extends FXAccountFactory<SavingsAccount> {

	/**
	 * Creates a new SavingsAccountFXFactory
	 */
	public SavingsAccountFXFactory() {
		super(SavingsAccount.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createAcctFrameList(SavingsAccount toEdit) {
		SavingsAccountFrame1 frame1 = new SavingsAccountFrame1(toEdit);
		List<FXPopupProgActivityFrame<?, ? extends Pane>> list = new ArrayList<>();
		list.add(frame1);
		return list;
	}

	@Override
	public List<Parent> createAcctExpandedViewAddOnList(SavingsAccount toEdit) {
		List<Parent> list = new ArrayList<>();
		Text apyText = new Text("APY " + toEdit.getAPY() + "%");
		apyText.setFill(Color.WHITE);
		apyText.setFont(new Font("Calibri", 28));
		HBox apyBox = new HBox();
		apyBox.setAlignment(Pos.CENTER_RIGHT);
		apyBox.setPadding(new Insets(0, 5, 0, 5));
		apyBox.getChildren().add(apyText);
		apyBox.visibleProperty().bind(apyBox.managedProperty());
		apyBox.setManaged(toEdit.getAPY() != 0);
		list.add(apyBox);
		return list;
	}

}
