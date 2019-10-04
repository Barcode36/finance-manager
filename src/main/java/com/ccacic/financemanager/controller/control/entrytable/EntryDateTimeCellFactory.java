package com.ccacic.financemanager.controller.control.entrytable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ccacic.financemanager.controller.control.UpdatableTableView;
import com.ccacic.financemanager.model.entry.Entry;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A CellFactory for displaying the LocalDateTime of an entry in
 * an EntryTable
 * @author Cameron Cacic
 *
 */
class EntryDateTimeCellFactory implements Callback<TableColumn<Entry, LocalDateTime>, TableCell<Entry, LocalDateTime>> {

	private static final double[] MAX_HEIGHT = new double[] {-1};

	private final boolean showsTime;
	
	/**
	 * Creates a new EntryDateTimeCellFactory
	 * @param showsTime if the time should be displayed alongside the date
	 */
	EntryDateTimeCellFactory(boolean showsTime) {
		this.showsTime = showsTime;
	}
	
	@Override
	public TableCell<Entry, LocalDateTime> call(TableColumn<Entry, LocalDateTime> param) {	
		TableCell<Entry, LocalDateTime> cell = new TableCell<>() {

			@Override
			public void updateItem(final LocalDateTime dateTime, boolean empty) {
				if (dateTime == null || empty) {
					setText("");
				} else {
					DateTimeFormatter formatter;
					if (!showsTime)
						formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
					else {
						formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
					}
					setText(formatter.format(dateTime));
				}
			}
		};
		cell.heightProperty().addListener((obv, oldVal, newVal) -> {
			if (newVal.doubleValue() > MAX_HEIGHT[0]) {
				((UpdatableTableView) param.getTableView()).registerFixedCellSize(newVal.doubleValue());
				MAX_HEIGHT[0] = newVal.doubleValue();
			}
		});
		return cell;
	}
	
}