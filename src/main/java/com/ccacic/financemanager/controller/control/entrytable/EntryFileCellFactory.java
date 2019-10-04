package com.ccacic.financemanager.controller.control.entrytable;

import java.io.File;
import java.util.List;

import com.ccacic.financemanager.controller.control.UpdatableTableView;
import com.ccacic.financemanager.model.entry.Entry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * A CellFactory for displaying the Files of an entry in an EntryTable
 * @author Cameron Cacic
 *
 */
class EntryFileCellFactory implements Callback<TableColumn<Entry, List<File>>, TableCell<Entry, List<File>>> {

	private static final String EMPTY_MESSAGE = "No Files";
	private static final double[] MAX_HEIGHT = new double[] {-1};
	
	@Override
	public TableCell<Entry, List<File>> call(TableColumn<Entry, List<File>> param) {
		TableCell<Entry, List<File>> cell = new TableCell<>() {

			@Override
			public void updateItem(final List<File> files, boolean empty) {
				if (!empty) {
					ComboBox<String> comboBox = new ComboBox<>();
					comboBox.setPromptText(EMPTY_MESSAGE);
					comboBox.prefWidthProperty().bind(widthProperty());
					ObservableList<String> fileNames = FXCollections.observableArrayList();
					if (files != null && !files.isEmpty()) {
						for (File f : files) {
							fileNames.add(f.getName());
						}
						comboBox.setItems(fileNames);
						comboBox.getSelectionModel().selectFirst();
					}
					setGraphic(comboBox);
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