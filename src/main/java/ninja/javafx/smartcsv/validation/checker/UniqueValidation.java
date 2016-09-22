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
package ninja.javafx.smartcsv.validation.checker;

import ninja.javafx.smartcsv.fx.table.model.ColumnValueProvider;
import ninja.javafx.smartcsv.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;

/**
 * Checks if the value is unique in the column
 */
public class UniqueValidation extends EmptyValueIsValid {

    private ColumnValueProvider columnValueProvider;
    private String column;

    public UniqueValidation(ColumnValueProvider columnValueProvider, String column) {
        this.columnValueProvider = columnValueProvider;
        this.column = column;
    }

    @Override
    public void check(int row, String value, ValidationError error) {

        List<Integer> lineNumbers = new ArrayList<>();

        int numberOfRows = columnValueProvider.getNumberOfRows();
        for (int currentRowOfIteration = 0; currentRowOfIteration < numberOfRows; currentRowOfIteration++) {
            String storedValue = columnValueProvider.getValue(currentRowOfIteration, column);

            if (value.equals(storedValue) && currentRowOfIteration != row) {
                lineNumbers.add(currentRowOfIteration + 1); // show not 0 based line numbers to user
            }
        }

        if (!lineNumbers.isEmpty()) {
            if (lineNumbers.size() > 1) {
                sort(lineNumbers);
                error.add("validation.message.uniqueness.multiple", value, lineNumbers.stream().map(Object::toString).collect(joining(", ")));
            } else {
                error.add("validation.message.uniqueness.single", value, lineNumbers.get(0).toString());
            }
        }

    }

    @Override
    public Type getType() {
        return Type.UNIQUE;
    }
}
