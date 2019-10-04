package com.ccacic.financemanager.controller.entry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.StockEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;

/**
 * The FXFactory for StockEntry
 * @author Cameron Cacic
 *
 */
public class StockEntryFXFactory extends FXEntryFactory<StockEntry> {

	/**
	 * Formats to 3 decimal places for stock shares
	 */
	private static final DecimalFormat SHARES_FORMAT = new DecimalFormat("#.000");
	
	/**
	 * Creates a new StockEntryFXFactory
	 */
	public StockEntryFXFactory() {
		super(StockEntry.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createEntryFrameList(StockEntry toEdit) {
		StockEntryFrame1 frame = new StockEntryFrame1(toEdit);
		List<FXPopupProgActivityFrame<?, ? extends Pane>> list = new ArrayList<>();
		list.add(frame);
		return list;
	}

	@Override
	public void insertEntryColumns(List<TableColumn<StockEntry, ?>> columns) {

		TableColumn<StockEntry, String> sharesColumn = new TableColumn<>("Shares");
		sharesColumn.setCellValueFactory(e -> {
			StockEntry entry = e.getValue();
			return new SimpleStringProperty(SHARES_FORMAT.format(entry.getShares()));
		});
		
		TableColumn<StockEntry, String> pricePerShareColumn = new TableColumn<>("Price per Share");
		pricePerShareColumn.setCellValueFactory(e -> {
			StockEntry entry = e.getValue();
			return new SimpleStringProperty(Currency.getDefaultCurrency().format(entry.getPricePerShare()));
		});
		
		columns.add(1, sharesColumn);
		columns.add(3, pricePerShareColumn);
		
	}

}
