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

package ninja.javafx.smartcsv.fx.table.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ninja.javafx.smartcsv.validation.ValidationConfiguration;
import ninja.javafx.smartcsv.validation.ValidationError;
import ninja.javafx.smartcsv.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * The CSVModel is the client representation for the csv filepath.
 * It holds the data in rows, stores the header and manages the validator.
 */
public class CSVModel {

    private Validator validator;
    private ObservableList<CSVRow> rows = FXCollections.observableArrayList();
    private String[] header;
    private ObservableList<ValidationError> validationError  = FXCollections.observableArrayList();

    /**
     * sets the validator configuration for the data revalidates
     * @param validationConfiguration the validator configuration for this data
     */
    public void setValidationConfiguration(ValidationConfiguration validationConfiguration) {
        this.validator = new Validator(validationConfiguration);
        revalidate();
    }

    /**
     * returns the data as a list of rows of the
     * @return list of rows
     */
    public ObservableList<CSVRow> getRows() {
        return rows;
    }

    public ObservableList<ValidationError> getValidationError() {
        return validationError;
    }

    /**
     * adds a new and empty row
     * @return the new row
     */
    public CSVRow addRow() {
        CSVRow row = new CSVRow();
        row.setValidator(validator);
        row.setRowNumber(rows.size());
        rows.add(row);
        return row;
    }

    /**
     * sets the column headers as string array
     * @param header the headers of the columns
     */
    public void setHeader(String[] header) {
        this.header = header;
        revalidate();
    }

    /**
     * returns the column headers
     * @return the column headers
     */
    public String[] getHeader() {
        return header;
    }


    /**
     * walks through the data and validates each value
     */
    public void revalidate() {
        validationError.clear();

        if (!hasValidator()) return;

        List<ValidationError> errors = new ArrayList<>();

        if (header != null) {
            ValidationError headerError = validator.isHeaderValid(header);
            if (headerError != null) {
                errors.add(headerError);
            }
        }

        for (int lineNumber = 0; lineNumber < rows.size(); lineNumber++) {
            CSVRow row = rows.get(lineNumber);
            row.setValidator(validator);
            for (String column: row.getColumns().keySet()) {
                CSVValue value = row.getColumns().get(column).getValue();
                value.setValidator(validator);
                if (validator != null) {
                    ValidationError validationError = validator.isValid(column, value.getValue(), lineNumber);
                    if (validationError != null) {
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

        validationError.setAll(errors);
    }

    private boolean hasValidator() {
        return validator != null && validator.hasConfig();
    }

}
