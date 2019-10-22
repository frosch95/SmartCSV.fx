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

package ninja.javafx.smartcsv.fx.table.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * This class represents a single row in the csv file.
 */
public class CSVRow {
    private ObservableMap<String, ObjectProperty<CSVValue>> columns = FXCollections.observableHashMap();
    private int rowNumber;

    /**
     * sets the row number
     * @param rowNumber
     */
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * return the row number
     * @return row number
     */
    public int getRowNumber() {
        return rowNumber;
    }


    /**
     * returns the columns with data as Map
     * @return columns with data
     */
    public ObservableMap<String, ObjectProperty<CSVValue>> getColumns() {
        return columns;
    }

    /**
     * stores the given value in the given column of this row
     * @param column column name
     * @param value the value to store
     */
    CSVValue addValue(String column, String value) {
        CSVValue v = new CSVValue();
        v.setValue(value);
        columns.put(column, new SimpleObjectProperty<>(v));
        return v;
    }

}
