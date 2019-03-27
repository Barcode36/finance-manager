package com.ccacic.financemanager.controller.entry;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.control.DoubleTextField;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntry;
import com.ccacic.financemanager.model.entry.children.StockEntry;
import com.ccacic.financemanager.model.entry.children.StockEntryAssembler;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * The first frame for StockEntry
 * @author Cameron Cacic
 *
 */
public class StockEntryFrame1 extends FXPopupProgActivityFrame<CrypCurrEntry, VBox>{

	@FXML
	private DoubleTextField sharesField;
	
	private StockEntry toEdit;
	
	/**
	 * Creates a new StockEntryFrame1
	 * @param toEdit the StockEntry to edit, or null to create a new one
	 */
	public StockEntryFrame1(StockEntry toEdit) {
		this.toEdit = toEdit;
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("frame_stockentry.fxml"), new VBox());
		getRoot().setSpacing(5);
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(StockEntryAssembler.SHARES, sharesField.getText());
		return paramMap;
	}

	@Override
	protected void initializeActivity() {
		if (toEdit != null) {
			sharesField.setDoubleValue(toEdit.getShares());
		}
	}

}
