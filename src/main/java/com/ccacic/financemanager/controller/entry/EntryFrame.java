package com.ccacic.financemanager.controller.entry;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ccacic.financemanager.controller.FXPopupProgActivityFrame;
import com.ccacic.financemanager.controller.StageStack;
import com.ccacic.financemanager.controller.control.CurrencyTextField;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryAssembler;
import com.ccacic.financemanager.model.entry.EntryFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The base frame for collecting data for an Entry
 * @author Cameron Cacic
 *
 */
public class EntryFrame extends FXPopupProgActivityFrame<Stage, BorderPane>{

	@FXML
	private DatePicker datePicker;
	@FXML
	private TextField hourTextField;
	@FXML
	private TextField minTextField;
	@FXML
	private Text hourText;
	@FXML
	private Text minText;
	@FXML
	private VBox dateBox;
	@FXML
	private CurrencyTextField currTextField;
	@FXML
	private Button removeFileButton;
	@FXML
	private ComboBox<String> filesCombo;
	@FXML
	private Button addFileButton;
	@FXML
	private TextArea descrArea;
	@FXML
	private VBox rightBox;
	@FXML
	private Button createEntryButton;
	@FXML
	private Button cancelButton;
	
	private final String key;
	private final Currency intendedCurr;
	private final Entry toEdit;
	
	/**
	 * Creates a new EntryFrame
	 * @param key the entry type key
	 * @param intendedCurr the Currency intended for the Entry
	 * @param toEdit the Entry to edit, or null to create a new one
	 */
	public EntryFrame(String key, Currency intendedCurr, Entry toEdit) {
		this.key = key;
		this.intendedCurr = intendedCurr;
		this.toEdit = toEdit;
	}
	
	@Override
	protected void callLoader() {
		load(FileHandler.getLayout("activity_entry.fxml"), new BorderPane());
	}
	
	@Override
	public ParamMap getParamMap() {
		ParamMap paramMap = new ParamMap();
		
		paramMap.putType(key);
		if (toEdit != null) {
			paramMap.put(EntryAssembler.ID, toEdit.getIdentifier());
		}
		
		paramMap.put(EntryAssembler.DATE_TIME, LocalDateTime.of(datePicker.getValue(), 
						LocalTime.of(Integer.parseInt(hourTextField.getText()),
								Integer.parseInt(minTextField.getText()),
								LocalTime.now().getSecond())).toString());
		paramMap.put(EntryAssembler.DESCRIPTION, "{" + descrArea.getText() + "}");
		paramMap.put(EntryAssembler.AMOUNT, Currency.deformat(currTextField.getText()));
		if (!filesCombo.getItems().isEmpty()) {
			paramMap.put(EntryAssembler.FILES, filesCombo.getItems());
		}

		return paramMap;
	}

	@Override
	protected void initializeActivity() {

		ObservableList<String> filesList = FXCollections.observableArrayList();
		
		if (toEdit != null) {
			datePicker.setValue(toEdit.getDateTime().toLocalDate());
			hourTextField.setText(toEdit.getDateTime().getHour() + "");
			minTextField.setText(toEdit.getDateTime().getMinute() + "");
		} else {
			LocalDateTime now = LocalDateTime.now();
			datePicker.setValue(now.toLocalDate());
			hourTextField.setText(now.getHour() + "");
			minTextField.setText(now.getMinute() + "");
		}
		
		if (!EntryFactory.getInstance().showsTime(key)) {
			dateBox.getChildren().removeAll(hourText, hourTextField, minText, minTextField);
		}
		
		currTextField.setPromptText(intendedCurr.format(0));
		currTextField.setCurrency(intendedCurr);
		if (toEdit != null) {
			currTextField.setText(intendedCurr.format(toEdit.getAmount()));
		} else {
			currTextField.setText(intendedCurr.format(0));
		}
		
		removeFileButton.setOnAction(e -> {
			
			int index = filesCombo.getSelectionModel().getSelectedIndex();
			filesList.remove(index);
			filesCombo.getSelectionModel().selectFirst();
			if (filesList.isEmpty()) {
				removeFileButton.disableProperty().set(true);
			}
			
		});
		
		addFileButton.setOnAction(e -> {
			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select File");
			File selectedFile = fileChooser.showOpenDialog(StageStack.peekStage());
			if (selectedFile != null) {
				filesList.add(selectedFile.getName());
				filesCombo.setItems(filesList);
				filesCombo.show();
				filesCombo.getSelectionModel().select(filesList.size() - 1);
				removeFileButton.disableProperty().set(false);
			}
			
		});
		if (toEdit != null && toEdit.getFiles() != null) {
			for (File f: toEdit.getFiles()) {
				filesList.add(f.getName());
			}
			if (!filesList.isEmpty()) {
				filesCombo.setItems(filesList);
				filesCombo.getSelectionModel().selectFirst();
				removeFileButton.disableProperty().set(false);
			}
		}
		
		descrArea.setPrefColumnCount(20);
		descrArea.setPrefRowCount(EntryFactory.getInstance().showsTime(key) ? 13 : 10);
		if (toEdit != null) {
			descrArea.setText(toEdit.getDescription());
		}
	}

}
