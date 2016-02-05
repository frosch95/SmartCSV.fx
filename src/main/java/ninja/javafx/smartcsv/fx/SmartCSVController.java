/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015 javafx.ninja <info@javafx.ninja>

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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.FileWriter;
import ninja.javafx.smartcsv.csv.CSVFileReader;
import ninja.javafx.smartcsv.csv.CSVFileWriter;
import ninja.javafx.smartcsv.fx.about.AboutController;
import ninja.javafx.smartcsv.fx.list.ErrorSideBar;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private PreferencesFileReader preferencesLoader;

    @Autowired
    private PreferencesFileWriter preferencesWriter;

    @Autowired
    private CSVFileReader csvLoader;

    @Autowired
    private ValidationFileReader validationLoader;

    @Autowired
    private CSVFileWriter csvFileWriter;

    @Autowired
    private ValidationFileWriter validationFileWriter;

    @Autowired
    private AboutController aboutController;

    @Autowired
    private PreferencesController preferencesController;

    @Autowired
    private ValidationEditorController validationEditorController;

    @Autowired
    private LoadFileService loadFileService;

    @Autowired
    private SaveFileService saveFileService;;

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
    private Button saveButton;

    @FXML
    private Button saveAsButton;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // members
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationCellFactory cellFactory;

    private CSVModel model;
    private TableView<CSVRow> tableView;
    private ErrorSideBar errorSideBar;
    private BooleanProperty fileChanged = new SimpleBooleanProperty(true);
    private ResourceBundle resourceBundle;
    private ObjectProperty<File> currentCsvFile = new SimpleObjectProperty<>();
    private ObjectProperty<File> currentConfigFile= new SimpleObjectProperty<>();

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

        bindMenuItemsToFileExistence(currentCsvFile, saveMenuItem, saveAsMenuItem, addRowMenuItem, loadConfigMenuItem);
        bindButtonsToFileExistence(currentCsvFile, saveButton, saveAsButton, addRowButton, loadConfigButton);

        bindMenuItemsToFileExistence(currentConfigFile, saveConfigMenuItem, saveAsConfigMenuItem);
        bindButtonsToFileExistence(currentConfigFile, saveAsConfigButton, saveConfigButton);

        bindCsvFileName();
        bindConfigFileName();

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
        currentCsvFile.setValue(
                loadFile(
                        csvLoader,
                        CSV_FILTER_TEXT,
                        CSV_FILTER_EXTENSION,
                        "Open CSV",
                        currentCsvFile.getValue()));
    }

    @FXML
    public void openConfig(ActionEvent actionEvent) {
        currentConfigFile.setValue(
                loadFile(
                        validationLoader,
                        JSON_FILTER_TEXT,
                        JSON_FILTER_EXTENSION,
                        "Open Validation Configuration",
                        currentConfigFile.getValue()));
    }

    @FXML
    public void saveCsv(ActionEvent actionEvent) {
        csvFileWriter.setModel(model);
        useSaveFileService(csvFileWriter, currentCsvFile.getValue());
    }

    @FXML
    public void saveAsCsv(ActionEvent actionEvent) {
        csvFileWriter.setModel(model);
        currentCsvFile.setValue(
                saveFile(
                        csvFileWriter,
                        CSV_FILTER_TEXT,
                        CSV_FILTER_EXTENSION,
                        currentCsvFile.getValue()));
    }

    @FXML
    public void saveConfig(ActionEvent actionEvent) {
        validationFileWriter.setValidationConfiguration(validationLoader.getValidationConfiguration());
        useSaveFileService(validationFileWriter, currentConfigFile.getValue());
    }

    @FXML
    public void saveAsConfig(ActionEvent actionEvent) {
        validationFileWriter.setValidationConfiguration(validationLoader.getValidationConfiguration());
        currentConfigFile.setValue(
                saveFile(
                        validationFileWriter,
                        JSON_FILTER_TEXT,
                        JSON_FILTER_EXTENSION,
                        currentConfigFile.getValue()));
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
            preferencesController.setCsvPreference(preferencesLoader.getCSVpreference());
        }
    }

    @FXML
    public void deleteRow(ActionEvent actionEvent) {
        model.getRows().removeAll(tableView.getSelectionModel().getSelectedItems());
        fileChanged.setValue(true);
        resetContent();
    }

    @FXML
    public void addRow(ActionEvent actionEvent) {
        CSVRow row = model.addRow();
        for (String column : model.getHeader()) {
            row.addValue(column, "");
        }
        fileChanged.setValue(true);
        resetContent();

        selectNewRow();
    }

    public boolean canExit() {
        boolean canExit = true;
        if (model != null && fileChanged.get()) {
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setTitle(resourceBundle.getString("dialog.validation.rules.title"));
        alert.setHeaderText(format(resourceBundle.getString("dialog.validation.rules.header"), column));
        alert.getDialogPane().setContent(validationEditorController.getView());
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            runLater(() -> {
                validationEditorController.updateConfiguration();
                fileChanged.setValue(true);
                model.revalidate();
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

    private void bindMenuItemsToFileExistence(ObjectProperty<File> file, MenuItem... items) {
        for (MenuItem item: items) {
            item.disableProperty().bind(isNull(file));
        }
    }

    private void bindButtonsToFileExistence(ObjectProperty<File> file, Button... items) {
        for (Button item: items) {
            item.disableProperty().bind(isNull(file));
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
        csvName.textProperty().bind(selectString(currentCsvFile, "name"));
    }

    private void bindConfigFileName() {
        configurationName.textProperty().bind(selectString(currentConfigFile, "name"));
    }

    private void loadCsvPreferencesFromFile() {
        if (PREFERENCES_FILE.exists()) {
            useLoadFileService(preferencesLoader, PREFERENCES_FILE,
                    event -> setCsvPreference(preferencesLoader.getCSVpreference()));
        }
    }

    private void saveCsvPreferences(CsvPreference csvPreference) {
        try {
            createPreferenceFile();
            preferencesWriter.setCsvPreference(csvPreference);
            useSaveFileService(preferencesWriter, PREFERENCES_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPreferenceFile() throws IOException {
        if (!PREFERENCES_FILE.exists()) {
            createPreferencesFileFolder();
            PREFERENCES_FILE.createNewFile();
        }
    }

    private void createPreferencesFileFolder() {
        if (!PREFERENCES_FILE.getParentFile().exists()) {
            PREFERENCES_FILE.getParentFile().mkdir();
        }
    }

    private void setCsvPreference(CsvPreference csvPreference) {
        csvLoader.setCsvPreference(csvPreference);
        csvFileWriter.setCsvPreference(csvPreference);
        preferencesController.setCsvPreference(csvPreference);
    }

    private File loadFile(FileReader fileReader,
                          String filterText,
                          String filter,
                          String title,
                          File initChildFile) {
        final FileChooser fileChooser = new FileChooser();

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(title);

        if (initChildFile != null) {
            fileChooser.setInitialDirectory(initChildFile.getParentFile());
        }

        //Show open file dialog
        File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
        if (file != null) {
            useLoadFileService(fileReader, file, event -> runLater(() -> {
                resetContent();
                fileChanged.setValue(false);
            }));
            return file;
        } else {
            return initChildFile;
        }
    }

    private File saveFile(FileWriter writer, String filterText, String filter, File initFile) {
        File file = initFile;
        if (model != null) {
            final FileChooser fileChooser = new FileChooser();

            //Set extension filter
            final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
            fileChooser.getExtensionFilters().add(extFilter);

            if (initFile != null) {
                fileChooser.setInitialDirectory(initFile.getParentFile());
                fileChooser.setInitialFileName(initFile.getName());
            }
            fileChooser.setTitle("Save File");

            //Show open file dialog
            file = fileChooser.showSaveDialog(applicationPane.getScene().getWindow());
            if (file != null) {
                useSaveFileService(writer, file);
            }
        }
        return file;
    }

    private void useLoadFileService(FileReader fileReader, File file, EventHandler<WorkerStateEvent> value) {
        loadFileService.setFile(file);
        loadFileService.setFileReader(fileReader);
        loadFileService.restart();
        loadFileService.setOnSucceeded(value);
    }

    private void useSaveFileService(FileWriter writer, File file) {
        saveFileService.setFile(file);
        saveFileService.setWriter(writer);
        saveFileService.restart();
        saveFileService.setOnSucceeded(event -> runLater(() -> {
            resetContent();
            fileChanged.setValue(false);
        }));
    }

    /**
     * Creates new table view and add the new content
     */
    private void resetContent() {
        model = csvLoader.getData();
        if (model != null) {
            model.getValidationError().addListener(weakErrorListListener);
            model.setValidationConfiguration(validationLoader.getValidationConfiguration());
            validationEditorController.setValidationConfiguration(validationLoader.getValidationConfiguration());
            tableView = new TableView<>();

            bindMenuItemsToTableSelection(deleteRowMenuItem);
            bindButtonsToTableSelection(deleteRowButton);

            for (String column : model.getHeader()) {
                addColumn(column, tableView);
            }

            tableView.getItems().setAll(model.getRows());
            tableView.setEditable(true);

            setBottomAnchor(tableView, 0.0);
            setTopAnchor(tableView, 0.0);
            setLeftAnchor(tableView, 0.0);
            setRightAnchor(tableView, 0.0);
            tableWrapper.getChildren().setAll(tableView);
            errorSideBar.setModel(model);
        }
    }

    /**
     * Adds a column with the given name to the tableview
     * @param header name of the column header
     * @param tableView the tableview
     */
    private void addColumn(String header, TableView tableView) {
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
                    fileChanged.setValue(true);
                    model.revalidate();
                });
            }
        });

        tableView.getColumns().add(column);
    }

    private ContextMenu contextMenuForColumn(String header) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editColumnRulesMenuItem = new MenuItem(resourceBundle.getString("context.menu.edit.column.rules"));
        bindMenuItemsToFileExistence(currentConfigFile, editColumnRulesMenuItem);
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
