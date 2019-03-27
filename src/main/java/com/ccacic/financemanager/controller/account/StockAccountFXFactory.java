package com.ccacic.financemanager.controller.account;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.account.children.StockAccount;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * The FXFactory for the StockAccount
 * @author Cameron Cacic
 *
 */
public class StockAccountFXFactory extends FXAccountFactory<StockAccount>{

	/**
	 * Creates a new StockAccountFXFactory
	 */
	public StockAccountFXFactory() {
		super(StockAccount.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createAcctFrameList(StockAccount toEdit) {
		StockAccountFrame1 frame1 = new StockAccountFrame1(toEdit);
		List<FXPopupProgActivityFrame<?, ? extends Pane>> list = new ArrayList<>();
		list.add(frame1);
		return list;
	}

	@Override
	public List<Parent> createAcctExpandedViewAddOnList(StockAccount toEdit) {
		List<Parent> list = new ArrayList<>();
		Text yieldText = new Text("Yield " + toEdit.getYield() + "%");
		yieldText.setFill(Color.WHITE);
		yieldText.setFont(new Font("Calibri", 28));
		HBox yieldBox = new HBox();
		yieldBox.setAlignment(Pos.CENTER_RIGHT);
		yieldBox.setPadding(new Insets(0, 5, 0, 5));
		yieldBox.getChildren().add(yieldText);
		yieldBox.visibleProperty().bind(yieldBox.managedProperty());
		yieldBox.setManaged(toEdit.getYield() != 0);
		list.add(yieldBox);
		return list;
	}

}
