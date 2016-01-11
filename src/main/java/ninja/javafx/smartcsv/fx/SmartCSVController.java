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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.csv.CSVFileReader;
import ninja.javafx.smartcsv.csv.CSVFileWriter;
import ninja.javafx.smartcsv.fx.about.AboutController;
import ninja.javafx.smartcsv.fx.list.ValidationErrorListCell;
import ninja.javafx.smartcsv.fx.preferences.PreferencesController;
import ninja.javafx.smartcsv.fx.table.ObservableMapValueFactory;
import ninja.javafx.smartcsv.fx.table.ValidationCellFactory;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import ninja.javafx.smartcsv.fx.table.model.CSVValue;
import ninja.javafx.smartcsv.preferences.PreferencesFileReader;
import ninja.javafx.smartcsv.validation.ValidationError;
import ninja.javafx.smartcsv.validation.ValidationFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.lang.Math.max;
import static javafx.application.Platform.exit;
import static javafx.application.Platform.runLater;

/**
 * main controller of the application
 */
@Component
public class SmartCSVController extends FXMLController {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private PreferencesFileReader preferencesLoader;

    @Autowired
    private CSVFileReader csvLoader;

    @Autowired
    private ValidationFileReader validationLoader;

    @Autowired
    private CSVFileWriter csvFileWriter;

    @Autowired
    private AboutController aboutController;

    @Autowired
    private PreferencesController preferencesController;

    @FXML
    private BorderPane applicationPane;

    @FXML
    private Label csvName;

    @FXML
    private Label configurationName;

    @FXML
    private Label stateName;

    @FXML
    private ListView errorList;

    @FXML
    private AnchorPane tableWrapper;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // members
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationCellFactory cellFactory;
    private final LoadCSVService loadCSVService = new LoadCSVService();
    private final SaveCSVService saveCSVService = new SaveCSVService();
    private CSVModel model;
    private TableView<CSVRow> tableView;
    private File lastDirectory;
    private BooleanProperty fileChanged = new SimpleBooleanProperty(true);
    private ResourceBundle resourceBundle;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // init
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        saveCSVService.setWriter(csvFileWriter);
        cellFactory = new ValidationCellFactory(resourceBundle);
        errorList.setCellFactory(param -> new ValidationErrorListCell(resourceBundle));
        errorList.getSelectionModel().selectedItemProperty().addListener(observable -> scrollToError());
        fileChanged.addListener(observable -> setStateName());
        setStateName();
        initCsvPreferences();
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
        loadFile(csvLoader, "CSV files (*.csv)", "*.csv", "Open CSV", csvName);
    }

    @FXML
    public void openConfig(ActionEvent actionEvent) {
        loadFile(validationLoader, "JSON files (*.json)", "*.json", "Open Validation Configuration", configurationName);
    }

    @FXML
    public void saveCsv(ActionEvent actionEvent) {
        saveCSVService.restart();
    }

    @FXML
    public void saveAsCsv(ActionEvent actionEvent) {
        saveFile(csvFileWriter, "CSV files (*.csv)", "*.csv");
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
        alert.setTitle("Preferences");
        alert.setHeaderText("Preferences");
        alert.getDialogPane().setContent(preferencesController.getView());
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            setCsvPreference(preferencesController.getCsvPreference());
        }
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initCsvPreferences() {
        try {
            File preferencesFile = new File(
                    getClass().getResource("/ninja/javafx/smartcsv/fx/preferences/preferences.json").toURI());
            preferencesLoader.read(preferencesFile);
            CsvPreference csvPreference = preferencesLoader.getCSVpreference();
            setCsvPreference(csvPreference);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void setCsvPreference(CsvPreference csvPreference) {
        csvLoader.setCsvPreference(csvPreference);
        csvFileWriter.setCsvPreference(csvPreference);
        preferencesController.setCsvPreference(csvPreference);
    }

    private void loadFile(FileReader fileReader, String filterText, String filter, String title, Label fileLabel) {
        final FileChooser fileChooser = new FileChooser();

        //Set extension filter
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(title);

        if (lastDirectory != null) {
            fileChooser.setInitialDirectory(lastDirectory);
        }

        //Show open file dialog
        final File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
        if (file != null) {
            loadCSVService.setFileLabel(fileLabel);
            loadCSVService.setFile(file);
            loadCSVService.setFileReader(fileReader);
            loadCSVService.restart();
        }
    }

    private void saveFile(CSVFileWriter writer, String filterText, String filter) {
        if (model != null) {
            final FileChooser fileChooser = new FileChooser();

            //Set extension filter
            final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterText, filter);
            fileChooser.getExtensionFilters().add(extFilter);

            File initfile = new File(model.getFilepath());
            fileChooser.setInitialDirectory(initfile.getParentFile());
            fileChooser.setInitialFileName(initfile.getName());
            fileChooser.setTitle("Save File");

            //Show open file dialog
            final File file = fileChooser.showOpenDialog(applicationPane.getScene().getWindow());
            if (file != null) {
                model.setFilepath(file.getAbsolutePath());
                saveCSVService.setWriter(writer);
                saveCSVService.restart();
            }
        }
    }

    /**
     * Creates new table view and add the new content
     */
    private void resetContent() {
        model = csvLoader.getData();
        if (model != null) {
            model.setValidator(validationLoader.getValidator());
            tableView = new TableView<>();

            for (String column : model.getHeader()) {
                addColumn(column, tableView);
            }
            tableView.getItems().setAll(model.getRows());
            tableView.setEditable(true);

            AnchorPane.setBottomAnchor(tableView, 0.0);
            AnchorPane.setTopAnchor(tableView, 0.0);
            AnchorPane.setLeftAnchor(tableView, 0.0);
            AnchorPane.setRightAnchor(tableView, 0.0);
            tableWrapper.getChildren().setAll(tableView);

            errorList.setItems(model.getValidationError());
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

    private void scrollToError() {
        ValidationError entry = (ValidationError)errorList.getSelectionModel().getSelectedItem();
        if (entry != null) {
            if (entry.getLineNumber() != null) {
                tableView.scrollTo(max(0, entry.getLineNumber() - 1));
                tableView.getSelectionModel().select(entry.getLineNumber());
            } else {
                tableView.scrollTo(0);
            }
        }
    }

    private void setStateName() {
        if (model != null) {
            if (fileChanged.get()) {
                stateName.setText(resourceBundle.getString("state.changed"));
            } else {
                stateName.setText(resourceBundle.getString("state.unchanged"));
            }
        } else {
            stateName.setText("");
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // inner class
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Service class for async load of a csv file
     */
    private class LoadCSVService extends Service {

        private File file = null;
        private FileReader fileReader;
        private Label fileLabel;

        public void setFile(File value) {
            file = value;
        }
        public void setFileReader(FileReader fileReader) {
            this.fileReader = fileReader;
        }
        public void setFileLabel(Label fileLabel) {
            this.fileLabel = fileLabel;
        }

        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Void call() throws Exception {
                    if (file != null) {
                        try {
                            lastDirectory = file.getParentFile();
                            fileReader.read(file);
                            runLater(() -> {
                                fileLabel.setText(file.getName());
                                resetContent();
                                fileChanged.setValue(false);
                            });
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                }
            };
        }

    }

    /**
     * Service class for async load of a csv file
     */
    private class SaveCSVService extends Service {

        private CSVFileWriter writer;

        public void setWriter(CSVFileWriter writer) {
            this.writer = writer;
        }

        @Override
        protected Task createTask() {
            return new Task() {
                @Override
                protected Void call() throws Exception {
                    try {
                        writer.saveFile(model);
                        runLater(() -> {
                            resetContent();
                            fileChanged.setValue(false);
                        });
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
        }

    }
}
