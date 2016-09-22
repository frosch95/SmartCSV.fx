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

import ninja.javafx.smartcsv.validation.ValidationError;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Checks if the value is part of a list of values
 */
public class ValueOfValidation extends EmptyValueIsValid {

    private List<String> values;

    public ValueOfValidation(List<String> values) {
        this.values = values;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!values.contains(value)) {
            String commaSeparated = values.stream().collect(joining(", "));
            error.add("validation.message.value.of", value, commaSeparated);
        }
    }

    @Override
    public Type getType() {
        return Type.VALUE_OF;
    }
}
