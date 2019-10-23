/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2019 javafx.ninja <info@javafx.ninja>

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

package ninja.javafx.smartcsv.export;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;

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
