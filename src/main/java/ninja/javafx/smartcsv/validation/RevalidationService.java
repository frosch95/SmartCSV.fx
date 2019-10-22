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
package ninja.javafx.smartcsv.validation;

import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import ninja.javafx.smartcsv.fx.table.model.CSVValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for running the validation async of the ui thread
 */
public class RevalidationService extends Service<List<ValidationError>> {

    private static final Logger logger = LogManager.getLogger(RevalidationService.class);

    private Validator validator;
    private List<CSVRow> rows;
    private String[] header;

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setRows(List<CSVRow> rows) {
        this.rows = rows;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    @Override
    protected Task<List<ValidationError>> createTask() {
        return new Task<List<ValidationError>>() {
            @Override
            protected List<ValidationError> call() throws Exception {
                List<ValidationError> errors = new ArrayList<>();
                try {
                    if (header != null) {
                        ValidationError headerError = validator.isHeaderValid(header);
                        if (headerError != null) {
                            logger.info("revalidate: header error found");
                            errors.add(headerError);
                        }
                    }

                    int maxRows = rows.size();
                    for (int lineNumber = 0; lineNumber < maxRows; lineNumber++) {
                        CSVRow row = rows.get(lineNumber);

                        Map<String, ObjectProperty<CSVValue>> table = row.getColumns();
                        Set<String> columns = table.keySet();

                        for (String column : columns) {
                            CSVValue value = table.get(column).getValue();
                            if (validator != null) {
                                ValidationError validationError = validator.isValid(lineNumber, column, value.getValue());
                                if (validationError != null) {
                                    logger.info("revalidate: {} errors found in line {}", validationError.getMessages().size(), lineNumber);
                                    errors.add(validationError);
                                    value.setValidationError(validationError);
                                } else {
                                    value.setValidationError(null);
                                }
                            } else {
                                value.setValidationError(null);
                            }
                        }
                    }

                } catch (Throwable t) {
                    logger.error("validation error", t);
                }
                return errors;
            }
        };
    }
}