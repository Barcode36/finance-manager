package com.ccacic.financemanager.controller.control.entrytable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import com.ccacic.financemanager.controller.control.UpdatableTableView;
import com.ccacic.financemanager.controller.entry.FXEntryFrameContainer;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * A UpdatableTableView for displaying Entries. By default the Entries are
 * sorted by date
 * @author Cameron Cacic
 *
 */
public class EntryTable extends UpdatableTableView<Entry> {
	
	private final Currency[] curr;
	
	/**
	 * Creates a new EntryTable
	 */
	public EntryTable() {
		super();
		curr = new Currency[1];
	}
	
	@Override
	public void refresh() {
		super.refresh();
		if (!getSortOrder().isEmpty()) {
			TableColumn<Entry, ?> sortColumn = getSortOrder().get(0);
			/*sortColumn.setSortType(TableColumn.SortType.ASCENDING);
			sort();*/
			sortColumn.setSortType(TableColumn.SortType.DESCENDING);
			sort();
		}
	}
	
	/**
	 * Chnages the Currency to display the entries in
	 * @param newCurr the new Currency
	 */
	public void changeCurrency(Currency newCurr) {
		curr[0] = newCurr;
		refresh();
	}
	
	/**
	 * Returns the current Currency
	 * @return the current Currency
	 */
	public Currency getCurrentCurrency() {
		return curr[0];
	}
	
	
	/**
	 * Builds the table with the given Currency and entryType key
	 * @param mainCurrency the Currency
	 * @param entryType the entry type key
	 */
	public void buildTable(Currency mainCurrency, String entryType) {
		EntryFactory entryFactory = EntryFactory.getInstance();
		
		curr[0] = mainCurrency;
		
		TableColumn<Entry, LocalDateTime> dateCol = new TableColumn<>("Date");
		TableColumn<Entry, String> amntCol = new TableColumn<>("Amount");
		TableColumn<Entry, List<File>> filesCol = new TableColumn<>("Files");
		TableColumn<Entry, String> descrCol = new TableColumn<>("Description");
		
		dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
		dateCol.setComparator((d1, d2) -> {
			if (d1 == null && d2 == null) {
				return 0;
			}
			if (d1 == null) {
				return -1;
			}
			if (d2 == null) {
				return 1;
			}
			return d1.compareTo(d2);
		});
		dateCol.setCellFactory(new EntryDateTimeCellFactory(entryFactory.showsTime(entryType)));
		dateCol.setSortType(TableColumn.SortType.DESCENDING);
		dateCol.setSortable(true);
		getSortOrder().add(dateCol);
		
		amntCol.setCellValueFactory(e -> new SimpleStringProperty(curr[0].format(e.getValue().getAmount())));
		amntCol.setComparator((s1, s2) -> {
			if (s1 == null && s2 == null) {
				return 0;
			}
			if (s1 == null) {
				return -1;
			}
			if (s2 == null) {
				return 1;
			}
			double d1 = Double.parseDouble(Currency.deformat(s1));
			double d2 = Double.parseDouble(Currency.deformat(s2));
			return (int) ((d1 - d2) / Math.abs(d1 - d2));
		});
		
		filesCol.setCellFactory(new EntryFileCellFactory());
		filesCol.setCellValueFactory(new PropertyValueFactory<>("files"));
		filesCol.setComparator((f1, f2) -> {
			if (f1 == null && f2 == null) {
				return 0;
			}
			if (f1 == null) {
				return -1;
			}
			if (f2 == null) {
				return 1;
			}
			return f1.size() - f2.size();
		});
		
		descrCol.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getDescription()));
		
		ObservableList<TableColumn<Entry, ?>> addTableCols = FXCollections.observableArrayList();
		addTableCols.add(dateCol);
		addTableCols.add(amntCol);
		addTableCols.add(filesCol);
		addTableCols.add(descrCol);
		FXEntryFrameContainer eContainer = FXEntryFrameContainer.getInstance();
		eContainer.insertEntryColumns(addTableCols, entryType);
		getColumns().setAll(addTableCols);
		sort();
	}
}
