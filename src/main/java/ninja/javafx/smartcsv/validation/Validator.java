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

import ninja.javafx.smartcsv.fx.table.model.ColumnValueProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class checks all the validations defined in the
 * Config against a given value
 */
public class Validator {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // member variables
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValidationConfiguration validationConfig;
    private ColumnValueProvider columnValueProvider;
    private Map<String, Map<Validation.Type, Validation>> columnValidationMap = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * JSON configuration for this validator
     *
     * @param validationConfig
     */
    public Validator(ValidationConfiguration validationConfig, ColumnValueProvider columnValueProvider) {
        this.validationConfig = validationConfig;
        this.columnValueProvider = columnValueProvider;
        initColumnValidations();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean needsColumnValidation(String column) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap != null) {
            return validationMap.containsKey(Validation.Type.UNIQUE);
        }
        return false;

    }


    /**
     * checks if the value is valid for the column configuration
     *
     * @param column the column name
     * @param value  the value to check
     * @return ValidationError with information if valid and if not which getMessage happened
     */
    public ValidationError isValid(Integer row, String column, String value) {
        ValidationError result = null;
        if (hasConfig()) {
            ValidationError error = ValidationError.withLineNumber(row).column(column);
            Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
            if (validationMap != null) {
                for (Validation validation: validationMap.values()) {
                    if (validation.canBeChecked(value)) {
                        validation.check(row, value, error);
                    }
                }
            }
            if (!error.isEmpty()) {
                result = error;
            }
        }
        return result;
    }


    public boolean hasConfig() {
        return validationConfig != null;
    }

    public void reinitializeColumn(String column) {
        clear(column);
        initializeColumnWithRules(column);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void add(String column, Validation validation) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap == null) {
            validationMap = new HashMap<>();
            columnValidationMap.put(column, validationMap);
        }
        validationMap.put(validation.getType(), validation);
    }

    private void clear(String column) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap != null) {
            validationMap.clear();
        }
    }

    private void initColumnValidations() {
        if (hasConfig()) {
            String[] columns = validationConfig.headerNames();
            for (String column : columns) {
                initializeColumnWithRules(column);
            }
        }
    }

    private void initializeColumnWithRules(String column) {
        Boolean alphaNumeric = validationConfig.getAlphanumericRuleFor(column);
        if (alphaNumeric != null && alphaNumeric) {
            add(column, new AlphaNumericValidation());
        }

        Boolean doubleRule = validationConfig.getDoubleRuleFor(column);
        if (doubleRule != null && doubleRule) {
            add(column, new DoubleValidation());
        }

        Boolean integerRule = validationConfig.getIntegerRuleFor(column);
        if (integerRule != null && integerRule) {
            add(column, new IntegerValidation());
        }

        Boolean notEmptyRule = validationConfig.getNotEmptyRuleFor(column);
        if (notEmptyRule != null && notEmptyRule) {
            add(column, new NotEmptyValidation());
        }

        Boolean uniqueRule = validationConfig.getUniqueRuleFor(column);
        if (uniqueRule != null && uniqueRule) {
            add(column, new UniqueValidation(columnValueProvider, column));
        }

        String dateRule = validationConfig.getDateRuleFor(column);
        if (dateRule != null && !dateRule.trim().isEmpty()) {
            add(column, new DateValidation(dateRule));
        }

        Integer minLength = validationConfig.getMinLengthRuleFor(column);
        if (minLength != null) {
            add(column, new MinLengthValidation(minLength));
        }

        Integer maxLength = validationConfig.getMaxLengthRuleFor(column);
        if (maxLength != null) {
            add(column, new MaxLengthValidation(maxLength));
        }

        String regexp = validationConfig.getRegexpRuleFor(column);
        if (regexp != null && !regexp.trim().isEmpty()) {
            add(column, new RegExpValidation(regexp));
        }

        String groovy = validationConfig.getGroovyRuleFor(column);
        if (groovy != null && !groovy.trim().isEmpty()) {
            add(column, new GroovyValidation(groovy));
        }
        List<String> valueOfRule = validationConfig.getValueOfRuleFor(column);
        if (valueOfRule != null && !valueOfRule.isEmpty()) {
            add(column, new ValueOfValidation(valueOfRule));
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

                for (int i = 0; i < headerNamesConfig.length; i++) {
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
