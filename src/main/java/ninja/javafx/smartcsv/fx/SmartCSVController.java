/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2016 javafx.ninja <info@javafx.ninja>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.

*/

package ninja.javafx.smartcsv.fx;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import ninja.javafx.smartcsv.csv.CSVFileReader;
import ninja.javafx.smartcsv.csv.CSVFileWriter;
import ninja.javafx.smartcsv.export.ErrorExport;
import ninja.javafx.smartcsv.files.FileStorage;
import ninja.javafx.smartcsv.fx.about.AboutController;
import ninja.javafx.smartcsv.fx.list.ErrorSideBar;
import ninja.javafx.smartcsv.fx.list.GotoLineDialog;
import ninja.javafx.smartcsv.fx.preferences.PreferencesController;
import ninja.javafx.smartcsv.fx.table.ObservableMapValueFactory;
import ninja.javafx.smartcsv.fx.table.ValidationCellFactory;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import ninja.javafx.smartcsv.fx.table.model.CSVValue;
import ninja.javafx.smartcsv.fx.util.LoadFileService;
import ninja.javafx.smartcsv.fx.util.SaveFileService;
import ninja.javafx.smartcsv.fx.validation.ValidationEditorController;
import ninja.javafx.smartcsv.preferences.PreferencesFileReader;
import ninja.javafx.smartcsv.preferences.PreferencesFileWriter;
import ninja.javafx.smartcsv.validation.configuration.ValidationConfiguration;
import ninja.javafx.smartcsv.validation.ValidationError;
import ninja.javafx.smartcsv.validation.ValidationFileReader;
import ninja.javafx.smartcsv.validation.ValidationFileWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.lang.Math.max;
import static java.text.MessageFormat.format;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;
import static javafx.beans.binding.Bindings.*;
import static javafx.scene.layout.AnchorPane.*;

/**
 * main controller of the application
 */
@Component
public class SmartCSVController extends FXMLController {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final File PREFERENCES_FILE =  new File(System.getProperty("user.home") +
            File.separator +
            ".SmartCSV.fx" +
            File.separator + "" +
            "preferences.json");
    public static final String CSV_FILTER_TEXT = "CSV files (*.csv)";
    public static final String CSV_FILTER_EXTENSION = "*.csv";
    public static final String JSON_FILTER_TEXT = "JSON files (*.json)";
    public static final String JSON_FILTER_EXTENSION = "*.json";
    public static final String EXPORT_LOG_FILTER_TEXT = "Error log files (*.log)";
    public static final String EXPORT_LOG_FILTER_EXTENSION = "*.log";


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private AboutController aboutController;

    @Autowired
    private PreferencesController preferencesController;

    @Autowired
    private ValidationEditorController validationEditorController;

    @Autowired
    private LoadFileService loadFileService;

    @Autowired
    private SaveFileService saveFileService;

    @Autowired
    private ErrorExport errorExport;

    @FXML
    private BorderPane applicationPane;

    @FXML
    private Label csvName;

    @FXML
    private Label configurationName;

    @FXML
    private Label stateName;

    @FXML
    private AnchorPane tableWrapper;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem createConfigMenuItem;

    @FXML
    private MenuItem loadConfigMenuItem;

    @FXML
    private MenuItem saveConfigMenuItem;

    @FXML
    private MenuItem saveAsConfigMenuItem;

    @FXML
    private MenuItem deleteRowMenuItem;

    @FXML
    private MenuItem addRowMenuItem;

    @FXML
    private MenuItem gotoLineMenuItem;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private Button saveButton;

    @FXML
    private Button saveAsButton;

    @FXML
    private Button createConfigButton;

    @FXML
    private Button loadConfigButton;

    @FXML
    private Button saveConfigButton;

    @FXML
    private Button saveAsConfigButton;

    @FXML
    private Button deleteRowButton;

    @FXML
    private Button addRowButton;

    @FXML
    private Button exportButton;

    @FXML
    private Label currentLineNumber;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // members
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationCellFactory cellFactory;

    private TableView<CSVRow> tableView;
    private ErrorSideBar errorSideBar;
    private ResourceBundle resourceBundle;
    private CSVFileReader csvFileReader = new CSVFileReader();
    private CSVFileWriter csvFileWriter = new CSVFileWriter();

    private FileStorage<CSVModel> currentCsvFile = new FileStorage<>(csvFileReader, csvFileWriter);
    private FileStorage<ValidationConfiguration> currentConfigFile = new FileStorage<>(new ValidationFileReader(), new ValidationFileWriter());
    private FileStorage<CsvPreference> csvPreferenceFile = new FileStorage<>(new PreferencesFileReader(), new PreferencesFileWriter());

    private ListChangeListener<ValidationError> errorListListener = c -> tableView.refresh();
    private WeakListChangeListener<ValidationError> weakErrorListListener = new WeakListChangeListener<>(errorListListener);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // init
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        setupTableCellFactory();
        setupErrorSideBar(resourceBundle);

        bindMenuItemsToContentExistence(currentCsvFile, saveMenuItem, saveAsMenuItem, addRowMenuItem, gotoLineMenuItem, createConfigMenuItem, loadConfigMenuItem);
        bindButtonsToContentExistence(currentCsvFile, saveButton, saveAsButton, addRowButton, createConfigButton, loadConfigButton);

        bindMenuItemsToContentExistence(currentConfigFile, saveConfigMenuItem, saveAsConfigMenuItem);
        bindButtonsToContentExistence(currentConfigFile, saveAsConfigButton, saveConfigButton);

        bindCsvFileName();
        bindConfigFileName();

        csvPreferenceFile.setFile(PREFERENCES_FILE);

        loadCsvPreferencesFromFile();
    }

    private void setupErrorSideBar(ResourceBundle resourceBundle) {
        errorSideBar = new ErrorSideBar(resourceBundle);
        errorSideBar.selectedValidationErrorProperty().addListener((observable, oldValue, newValue) -> {
            scrollToError(newValue);
        });
        applicationPane.setRight(errorSideBar);
    }

    private void setupTableCellFactory() {
        cellFactory = new ValidationCellFactory(resourceBundle);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // setter
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Value("${fxml.smartcvs.view}")
    @Override
    public void setFxmlFilePath(String filePath) {
        this.fxmlFilePath = filePath;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // actions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    public void openCsv(ActionEvent actionEvent) {
        loadFile(CSV_FILTER_TEXT, CSV_FILTER_EXTENSION, "Open CSV", currentCsvFile);
    }

    @FXML
    public void openConfig(ActionEvent actionEvent) {
        loadFile(JSON_FILTER_TEXT, JSON_FILTER_EXTENSION, "Open Validation Configuration", currentConfigFile);
    }

    @FXML
    public void createConfig(ActionEvent actionEvent) {
        currentConfigFile.setContent(currentCsvFile.getContent().createValidationConfiguration());
        currentConfigFile.setFile(null);
        currentConfigFile.setFileChanged(true);
        resetContent();
    }

    @FXML
    public void saveCsv(ActionEvent actionEvent) {
        useSaveFileService(currentCsvFile);
    }

    @FXML
    public void saveAsCsv(ActionEvent actionEvent) {
        saveFile(CSV_FILTER_TEXT, CSV_FILTER_EXTENSION, currentCsvFile);
    }

    @FXML
    public void saveConfig(ActionEvent actionEvent) {
        if (currentConfigFile.getFile() == null) {
            saveAsConfig(actionEvent);
        } else {
            useSaveFileService(currentConfigFile);
        }
    }

    @FXML
    public void saveAsConfig(ActionEvent actionEvent) {
        saveFile(JSON_FILTER_TEXT, JSON_FILTER_EXTENSION, currentConfigFile);
    }

    @FXML
    public void close(ActionEvent actionEvent) {
        if (canExit()) {
            exit();
        }
    }

    @FXML
    public void about(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("SmartCSV.fx");
        alert.getDialogPane().setContent(aboutController.getView());
        alert.showAndWait();
    }

    @FXML
    public void preferences(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setTitle(resourceBundle.getString("dialog.preferences.title"));
        alert.setHeaderText(resourceBundle.getString("dialog.preferences.header.text"));
        alert.getDialogPane().setContent(preferencesController.getView());

        Node okButton = alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(preferencesController.validProperty().not());

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            CsvPreference csvPreference = preferencesController.getCsvPreference();
            setCsvPreference(csvPreference);
            saveCsvPreferences(csvPreference);
        } else {
            preferencesController.setCsvPreference(csvPreferenceFile.getContent());
        }
    }

    @FXML
    public void deleteRow(ActionEvent actionEvent) {
        currentCsvFile.getContent().getRows().removeAll(tableView.getSelectionModel().getSelectedItems());
        currentCsvFile.setFileChanged(true);
        resetContent();
    }

    @FXML
    public void addRow(ActionEvent actionEvent) {
        CSVRow row = currentCsvFile.getContent().addRow();
        for (String column : currentCsvFile.getContent().getHeader()) {
            currentCsvFile.getContent().addValue(row, column, "");
        }
        currentCsvFile.setFileChanged(true);
        resetContent();

        selectNewRow();
    }

    @FXML
    public void gotoLine(ActionEvent actionEvent) {
        int maxLineNumber = currentCsvFile.getContent().getRows().size();
        GotoLineDialog dialog = new GotoLineDialog(maxLineNumber);
        dialog.setTitle(resourceBundle.getString("dialog.goto.line.title"));
        dialog.setHeaderText(format(resourceBundle.getString("dialog.goto.line.header.text"), maxLineNumber));
        dialog.setContentText(resourceBundle.getString("dialog.goto.line.label"));
        Optional<Integer> result = dialog.showAndWait();
        if (result.isPresent()){
            Integer lineNumber = result.get();
            if (lineNumber != null) {
                tableView.scrollTo(max(0, lineNumber - 2));
                tableView.getSelectionModel().select(lineNumber - 1);
            }
        }
    }

    @FXML
    public void export(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(EXPORT_LOG_FILTER_TEXT, EXPORT_LOG_FILTER_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save");

        //Show open file dialog
        File file = fileChooser.showSaveDialog(applicationPane.getScene().getWindow());
        if (file != null) {
            errorExport.setCsvFilename(currentCsvFile.getFile().getName());
            errorExport.setModel(currentCsvFile.getContent());
            errorExport.setFile(file);
            errorExport.setResourceBundle(resourceBundle);
            errorExport.restart();
        }
    }

    public boolean canExit() {
        boolean canExit = true;
        if (currentCsvFile.getContent() != null && currentCsvFile.isFileChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(resourceBundle.getString("dialog.exit.title"));
            alert.setHeaderText(resourceBundle.getString("dialog.exit.header.text"));
            alert.setContentText(resourceBundle.getString("dialog.exit.text"));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK){
                canExit = false;
            }
        }

        return canExit;
    }

    public void showValidationEditor(String column) {
        validationEditorController.setSelectedColumn(column);
        validationEditorController.updateForm();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setTitle(resourceBundle.getString("dialog.validation.rules.title"));
        alert.setHeaderText(format(resourceBundle.getString("dialog.validation.rules.header"), column));
        alert.getDialogPane().setContent(validationEditorController.getView());
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            runLater(() -> {
                validationEditorController.updateConfiguration();
                currentCsvFile.setFileChanged(true);
                currentCsvFile.getContent().revalidate(column);
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void selectNewRow() {
        int lastRow = tableView.getItems().size()-1;
        tableView.scrollTo(lastRow);
        tableView.requestFocus();
        tableView.getSelectionModel().select(lastRow);
    }

    private void bindMenuItemsToContentExistence(FileStorage file, MenuItem... items) {
        for (MenuItem item: items) {
            item.disableProperty().bind(isNull(file.contentProperty()));
        }
    }

    private void bindButtonsToContentExistence(FileStorage file, Button... items) {
        for (Button item: items) {
            item.disableProperty().bind(isNull(file.contentProperty()));
        }
    }

    private void bindMenuItemsToTableSelection(MenuItem... items) {
        for (MenuItem item: items) {
            item.disableProperty().bind(lessThan(tableView.getSelectionModel().selectedIndexProperty(), 0));
        }
    }

    private void bindButtonsToTableSelection(Button... items) {
        for (Button item: items) {
            item.disableProperty().bind(lessThan(tableView.getSelectionModel().selectedIndexProperty(), 0));
        }
    }

    private void bindCsvFileName() {
        csvName.textProperty().bind(selectString(currentCsvFile.fileProperty(), "name"));
    }

    private void bindConfigFileName() {
        configurationName.textProperty().bind(selectString(currentConfigFile.fileProperty(), "name"));
    }

    private void bindLineNumber() {
        currentLineNumber.textProperty().bind(tableView.getSelectionModel().selectedIndexProperty().add(1).asString());
    }

    private void loadCsvPreferencesFromFile() {
        if (csvPreferenceFile.getFile().exists()) {
            useLoadFileService(csvPreferenceFile, event -> setCsvPreference(csvPreferenceFile.getContent()));
        } else {
            setCsvPreference(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
        }
    }

    private void saveCsvPreferences(CsvPreference csvPreference) {
        try {
            createPreferenceFile();
            csvPreferenceFile.setContent(csvPreference);
            useSaveFileService(csvPreferenceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPreferenceFile() throws IOException {
        if (!csvPreferenceFile.getFile().exists()) {
            createPreferencesFileFolder();
            csvPreferenceFile.getFile().createNewFile();
        }
    }

    private void createPreferencesFileFolder() {
        if (!csvPreferenceFile.getFile().getParentFile().exists()) {
            csvPreferenceFile.getFile().getParentFile().mkdir();
        }
    }

    private void setCsvPreference(CsvPreference csvPreference) {
        preferencesController.setCsvPreference(csvPreference);
        csvFileReader.setCsvPreference(csvPreference);
        csvFileWriter.setCsvPreference(csvPreference);

    }

    private void loadFile(String filterText,
                          String filter,
                          String title,
                          FileStorage storageFile) {
        final FileChooser fileChooser = new FileChooser();

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(title);

        if (storageFile.getFile() != null) {
            fileChooser.setInitialDirectory(storageFile.getFile().getParentFile());
        }

        //Show open file dialog
        File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
        if (file != null) {
            storageFile.setFile(file);
            useLoadFileService(storageFile, t -> resetContent());
        }
    }

    private File saveFile(String filterText, String filter, FileStorage fileStorage) {
        File file = fileStorage.getFile();
        if (fileStorage.getContent() != null) {
            final FileChooser fileChooser = new FileChooser();

            //Set extension filter
            final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
            fileChooser.getExtensionFilters().add(extFilter);

            if (fileStorage.getFile() != null) {
                fileChooser.setInitialDirectory(fileStorage.getFile().getParentFile());
                fileChooser.setInitialFileName(fileStorage.getFile().getName());
            }
            fileChooser.setTitle("Save File");

            //Show open file dialog
            file = fileChooser.showSaveDialog(applicationPane.getScene().getWindow());
            if (file != null) {
                fileStorage.setFile(file);
                useSaveFileService(fileStorage);
            }
        }
        return file;
    }

    private void useLoadFileService(FileStorage fileStorage, EventHandler<WorkerStateEvent> onSucceededHandler) {
        loadFileService.setFileStorage(fileStorage);
        loadFileService.restart();
        loadFileService.setOnSucceeded(onSucceededHandler);
    }

    private void useSaveFileService(FileStorage fileStorage) {
        saveFileService.setFileStorage(fileStorage);
        saveFileService.restart();
        saveFileService.setOnSucceeded(t -> resetContent());
    }

    /**
     * Creates new table view and add the new content
     */
    private void resetContent() {
        resetExportButtons();

        if (currentCsvFile.getContent() != null) {
            currentCsvFile.getContent().getValidationError().addListener(weakErrorListListener);
            currentCsvFile.getContent().setValidationConfiguration(currentConfigFile.getContent());
            validationEditorController.setValidationConfiguration(currentConfigFile.getContent());
            tableView = new TableView<>();
            bindLineNumber();

            bindMenuItemsToTableSelection(deleteRowMenuItem);
            bindButtonsToTableSelection(deleteRowButton);

            for (String column : currentCsvFile.getContent().getHeader()) {
                addColumn(column, tableView);
            }

            tableView.getItems().setAll(currentCsvFile.getContent().getRows());
            tableView.setEditable(true);

            setBottomAnchor(tableView, 0.0);
            setTopAnchor(tableView, 0.0);
            setLeftAnchor(tableView, 0.0);
            setRightAnchor(tableView, 0.0);
            tableWrapper.getChildren().setAll(tableView);
            errorSideBar.setModel(currentCsvFile.getContent());
            binExportButtons();
        }
    }

    private void binExportButtons() {
        exportButton.disableProperty().bind(Bindings.isEmpty(currentCsvFile.getContent().getValidationError()));
        exportMenuItem.disableProperty().bind(Bindings.isEmpty(currentCsvFile.getContent().getValidationError()));
    }

    private void resetExportButtons() {
        exportButton.disableProperty().unbind();
        exportMenuItem.disableProperty().unbind();
        exportButton.disableProperty().setValue(true);
        exportMenuItem.disableProperty().setValue(true);
    }

    /**
     * Adds a column with the given name to the tableview
     * @param header name of the column header
     * @param tableView the tableview
     */
    private void addColumn(final String header, TableView tableView) {
        TableColumn column = new TableColumn(header);
        column.setCellValueFactory(new ObservableMapValueFactory(header));
        column.setCellFactory(cellFactory);
        column.setEditable(true);
        column.setSortable(false);

        ContextMenu contextMenu = contextMenuForColumn(header);
        column.setContextMenu(contextMenu);

        column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CSVRow, CSVValue>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CSVRow, CSVValue> event) {
                event.getTableView().getItems().get(event.getTablePosition().getRow()).
                getColumns().get(header).setValue(event.getNewValue());
                runLater(() -> {
                    currentCsvFile.setFileChanged(true);
                });
            }
        });

        tableView.getColumns().add(column);
    }

    private ContextMenu contextMenuForColumn(String header) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editColumnRulesMenuItem = new MenuItem(resourceBundle.getString("context.menu.edit.column.rules"));
        bindMenuItemsToContentExistence(currentConfigFile, editColumnRulesMenuItem);
        editColumnRulesMenuItem.setOnAction(e -> showValidationEditor(header));
        contextMenu.getItems().addAll(editColumnRulesMenuItem);
        return contextMenu;
    }

    private void scrollToError(ValidationError entry) {
        if (entry != null) {
            if (entry.getLineNumber() != null) {
                tableView.scrollTo(max(0, entry.getLineNumber() - 1));
                tableView.getSelectionModel().select(entry.getLineNumber());
            } else {
                tableView.scrollTo(0);
            }
        }
    }
}
