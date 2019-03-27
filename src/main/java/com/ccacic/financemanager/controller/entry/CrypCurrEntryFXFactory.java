package com.ccacic.financemanager.controller.entry;

import java.util.ArrayList;
import java.util.List;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * The FXFactory for CrypCurrEntry
 * @author Cameron Cacic
 *
 */
public class CrypCurrEntryFXFactory extends FXEntryFactory<CrypCurrEntry> {

	/**
	 * Creates a new CrypCurrEntryFXFactory
	 */
	public CrypCurrEntryFXFactory() {
		super(CrypCurrEntry.class.getSimpleName());
	}

	@Override
	public List<FXPopupProgActivityFrame<?, ? extends Pane>> createEntryFrameList(CrypCurrEntry toEdit) {
		CrypCurrEntryFrame1 frame = new CrypCurrEntryFrame1(toEdit);
		List<FXPopupProgActivityFrame<?, ? extends Pane>> list = new ArrayList<>();
		list.add(frame);
		return list;
	}

	@Override
	public void insertEntryColumns(List<TableColumn<CrypCurrEntry, ?>> columns) {
		TableColumn<CrypCurrEntry, String> histValueColumn = new TableColumn<CrypCurrEntry, String>("Value at Transaction Time");
		histValueColumn.setCellValueFactory(new Callback<CellDataFeatures<CrypCurrEntry, String>, ObservableValue<String>>() {

			public ObservableValue<String> call(CellDataFeatures<CrypCurrEntry, String> e) {
				CrypCurrEntry entry = e.getValue();
				return new SimpleStringProperty(Currency.getDefaultCurrency().format(entry.getHistFiatValue()));
			}

		});
		
		TableColumn<CrypCurrEntry, String> transIDColumn = new TableColumn<CrypCurrEntry, String>("Transaction ID");
		transIDColumn.setCellValueFactory(new Callback<CellDataFeatures<CrypCurrEntry, String>, ObservableValue<String>>() {

			public ObservableValue<String> call(CellDataFeatures<CrypCurrEntry, String> e) {
				return new SimpleStringProperty(e.getValue().getTransactID());
			}

		});
		
		columns.add(2, histValueColumn);
		columns.add(3, transIDColumn);
	}	

}
