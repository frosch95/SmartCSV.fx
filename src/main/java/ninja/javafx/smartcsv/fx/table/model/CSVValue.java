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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ninja.javafx.smartcsv.validation.ValidationError;
import ninja.javafx.smartcsv.validation.Validator;

/**
 * The csv value represents the value of a single cell.
 * It also knows about the position (row and column)
 * and if the value is valid based on the validator.
 */
public class CSVValue {
    private Validator validator;
    private int rowNumber;
    private String column;
    private StringProperty value = new SimpleStringProperty();
    private ValidationError valid;

    /**
     * single value of a cell
     * @param validator the reference to the validator
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * the row number this value is stored in
     * @param row row number
     */
    public void setRowNumber(int row) {
        this.rowNumber = row;
    }

    /**
     * the column this value is stored in
     * @param column header name of the column
     */
    public void setColumn(String column) {
        this.column = column;
    }

    /**
     * returns the real value
     * @return the real value
     */
    public String getValue() {
        return value.get();
    }

    /**
     * JavaFX property representation of the real value
     * @return property of real value
     */
    public StringProperty valueProperty() {
        return value;
    }

    /**
     * sets the real value
     * @param value the real value
     */
    public void setValue(String value) {
        if (validator != null) {
            valid = validator.isValid(rowNumber, column, value);
        }
        this.value.set(value);
    }

    /**
     * returns if the value is valid to the rules of the validator
     * @return
     */
    public ValidationError getValidationError() {
        return valid;
    }

    /**
     * sets the state if a value is valid or not
     * @param valid the validation state
     */
    protected void setValidationError(ValidationError valid) {
        this.valid = valid;
    }
}
