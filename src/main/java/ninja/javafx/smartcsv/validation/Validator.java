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

package ninja.javafx.smartcsv.validation;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.validator.GenericValidator.*;

/**
 * This class checks all the validations defined in the
 * Config against a given value
 */
public class Validator {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationConfiguration validationConfig;
    private ColumnValidations columnValidations = new ColumnValidations();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * JSON configuration for this validator
     * @param validationConfig
     */
    public Validator(ValidationConfiguration validationConfig) {
         this.validationConfig = validationConfig;
        initColumnValidations();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * checks if the value is valid for the column configuration
     * @param column the column name
     * @param value the value to check
     * @return ValidationError with information if valid and if not which getMessage happened
     */
    public ValidationError isValid(Integer lineNumber, String column, String value) {
        ValidationError result = null;
        if (hasConfig()) {
            ValidationError error = columnValidations.isValid(lineNumber, column, value);
            if (!error.isEmpty()) {
                result = error;
            }
        }
        return result;
    }


    public boolean hasConfig() {
        return validationConfig != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initColumnValidations() {
        if (hasConfig()) {
        String[] columns = validationConfig.headerNames();
        for(String column: columns) {
            Boolean alphaNumeric = validationConfig.getAlphanumericRuleFor(column);
            if (alphaNumeric != null && alphaNumeric) {
                columnValidations.add(column, new AlphaNumericValidation());
            }

            Boolean doubleRule = validationConfig.getDoubleRuleFor(column);
            if (doubleRule != null && doubleRule) {
                columnValidations.add(column, new DoubleValidation());
            }

            Boolean integerRule = validationConfig.getIntegerRuleFor(column);
            if (integerRule != null && integerRule) {
                columnValidations.add(column, new IntegerValidation());
            }

            Boolean notEmptyRule = validationConfig.getNotEmptyRuleFor(column);
            if (notEmptyRule != null && notEmptyRule) {
                columnValidations.add(column, new NotEmptyValidation());
            }

            Boolean uniqueRule = validationConfig.getUniqueRuleFor(column);
            if (uniqueRule != null && uniqueRule) {
                columnValidations.add(column, new UniqueValidation());
            }

            String dateRule = validationConfig.getDateRuleFor(column);
            if (dateRule != null && !dateRule.trim().isEmpty()) {
                columnValidations.add(column, new DateValidation(dateRule));
            }

            Integer minLength = validationConfig.getMinLengthRuleFor(column);
            if (minLength != null) {
                columnValidations.add(column, new MinLengthValidation(minLength));
            }

            Integer maxLength = validationConfig.getMaxLengthRuleFor(column);
            if (maxLength != null) {
                columnValidations.add(column, new MaxLengthValidation(maxLength));
            }

            String regexp = validationConfig.getRegexpRuleFor(column);
            if (regexp != null && !regexp.trim().isEmpty()) {
                columnValidations.add(column, new RegExpValidation(regexp));
            }

            String groovy = validationConfig.getGroovyRuleFor(column);
            if (groovy != null && !groovy.trim().isEmpty()) {
                columnValidations.add(column, new GroovyValidation(groovy));
            }
            List<String> valueOfRule = validationConfig.getValueOfRuleFor(column);
            if (valueOfRule != null && !valueOfRule.isEmpty()) {
                columnValidations.add(column, new ValueOfValidation(valueOfRule));
            }
        }
        }
    }


    public ValidationError isHeaderValid(String[] headerNames) {
        ValidationError result = null;
        if (validationConfig != null) {
            String[] headerNamesConfig = validationConfig.headerNames();
            if (headerNamesConfig != null) {
                if (headerNames.length != headerNamesConfig.length) {
                    result = ValidationError.withoutLineNumber().add("validation.message.header.length",
                            Integer.toString(headerNames.length),
                            Integer.toString(headerNamesConfig.length));
                    return result;
                }

                ValidationError error = ValidationError.withoutLineNumber();

                for(int i=0; i<headerNamesConfig.length; i++) {
                    if (!headerNamesConfig[i].equals(headerNames[i])) {
                        error.add("validation.message.header.match",
                                Integer.toString(i),
                                headerNamesConfig[i],
                                headerNames[i]);
                    }
                }
                if (!error.isEmpty()) {
                    result = error;
                }
            }
        }
        return result;
    }
}
