package ninja.javafx.smartcsv.export;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.validation.ValidationError;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static ninja.javafx.smartcsv.fx.util.I18nValidationUtil.getI18nValidatioMessage;

/**
 * this class exports the error messages into a log file
 */
@org.springframework.stereotype.Service
public class ErrorExport extends Service {

    private CSVModel model;
    private File file;
    private ResourceBundle resourceBundle;
    private String csvFilename;

    public void setCsvFilename(String csvFilename) {
        this.csvFilename = csvFilename;
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public void setModel(CSVModel model) {
        this.model = model;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Void call() throws Exception {
                try {
                    StringWriter log = new StringWriter();
                    log.append(
                            format(resourceBundle.getString("log.header.message"),
                                   csvFilename,
                                   Integer.toString(model.getValidationError().size()))).append("\n\n");
                    model.getValidationError().forEach(error ->
                        log.append(
                                format(resourceBundle.getString("log.message"),
                                       error.getLineNumber().toString(),
                                       error.getColumn(),
                                       getI18nValidatioMessage(resourceBundle, error))).append("\n")
                    );
                    Files.write(file.toPath(), log.toString().getBytes());

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        };
    }

}
