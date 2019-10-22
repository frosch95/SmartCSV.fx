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

import ninja.javafx.smartcsv.fx.table.model.ColumnValueProvider;
import ninja.javafx.smartcsv.validation.checker.*;
import ninja.javafx.smartcsv.validation.configuration.Constraints;
import ninja.javafx.smartcsv.validation.configuration.Field;
import ninja.javafx.smartcsv.validation.configuration.ValidationConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ninja.javafx.smartcsv.validation.ValidationFormatHelper.dateFormat;
import static ninja.javafx.smartcsv.validation.configuration.StringFormat.*;
import static ninja.javafx.smartcsv.validation.configuration.Type.*;

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

    private void remove(String column, Validation.Type type) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap == null) {
            validationMap = new HashMap<>();
            columnValidationMap.put(column, validationMap);
        }
        validationMap.remove(type);
    }

    private void clear(String column) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap != null) {
            validationMap.clear();
        }
    }

    private void initColumnValidations() {
        if (hasConfig()) {
            for (Field column : validationConfig.getFields()) {
                initializeColumnWithRules(column);
            }
        }
    }

    private void initializeColumnWithRules(String columnName) {
        if (hasConfig()) {
            for (Field column : validationConfig.getFields()) {
                if (column.getName().equals(columnName)) {
                    initializeColumnWithRules(column);
                    break;
                }
            }
        }

    }

    private void initializeColumnWithRules(Field column) {

        if (column.getType() != null) {
            if (column.getType() == NUMBER) {
                add(column.getName(), new DoubleValidation());
            }

            if (column.getType() == INTEGER) {
                add(column.getName(), new IntegerValidation());
            }

            if (column.getType() == DATE) {
                String format = dateFormat(column.getFormat(), "yyyy-MM-dd");
                add(column.getName(), new DateValidation(format));
            }

            if (column.getType() == DATETIME) {
                String format = dateFormat(column.getFormat(), "yyyy-MM-ddThh:mm:ssZ");
                add(column.getName(), new DateValidation(format));
            }

            if (column.getType() == TIME) {
                String format = dateFormat(column.getFormat(), "hh:mm:ss");
                add(column.getName(), new DateValidation(format));
            }

            if (column.getType() == STRING && column.getFormat() == null) {
                remove(column.getName(), Validation.Type.STRING);
            } else {

                if (column.getType() == STRING && column.getFormat().equalsIgnoreCase(EMAIL.getExternalValue())) {
                    add(column.getName(), new EmailValidation());
                }

                if (column.getType() == STRING && column.getFormat().equalsIgnoreCase(URI.getExternalValue())) {
                    add(column.getName(), new UriValidation());
                }

                if (column.getType() == STRING && column.getFormat().equalsIgnoreCase(UUID.getExternalValue())) {
                    add(column.getName(), new UuidValidation());
                }

                if (column.getType() == STRING && column.getFormat().equalsIgnoreCase(BINARY.getExternalValue())) {
                    add(column.getName(), new BinaryValidation());
                }

            }
        }

        String groovy = column.getGroovy();
        if (groovy != null && !groovy.trim().isEmpty()) {
            add(column.getName(), new GroovyValidation(groovy));
        }

        Constraints constraints = column.getConstraints();
        if (constraints != null) {
            Boolean notEmptyRule = constraints.getRequired();
            if (notEmptyRule != null && notEmptyRule) {
                add(column.getName(), new NotEmptyValidation());
            }

            Boolean uniqueRule = constraints.getUnique();
            if (uniqueRule != null && uniqueRule) {
                add(column.getName(), new UniqueValidation(columnValueProvider, column.getName()));
            }

            Integer minLength = constraints.getMinLength();
            if (minLength != null) {
                add(column.getName(), new MinLengthValidation(minLength));
            }

            Integer maxLength = constraints.getMaxLength();
            if (maxLength != null) {
                add(column.getName(), new MaxLengthValidation(maxLength));
            }

            String regexp = constraints.getPattern();
            if (regexp != null && !regexp.trim().isEmpty()) {
                add(column.getName(), new RegExpValidation(regexp));
            }



            List<String> valueOfRule =  constraints.getEnumeration();
            if (valueOfRule != null && !valueOfRule.isEmpty()) {
                add(column.getName(), new ValueOfValidation(valueOfRule));
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
