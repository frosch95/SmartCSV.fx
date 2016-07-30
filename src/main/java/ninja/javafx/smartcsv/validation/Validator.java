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
    private GroovyShell shell = new GroovyShell();
    private Map<String, Script> scriptCache = new HashMap<>();


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * JSON configuration for this validator
     * @param validationConfig
     */
    public Validator(ValidationConfiguration validationConfig) {
         this.validationConfig = validationConfig;
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
    public ValidationError isValid(String column, String value, Integer lineNumber) {
        ValidationError result = null;
        if (hasConfig()) {

            ValidationError error = ValidationError.withLineNumber(lineNumber);
            checkBlankOrNull(column, value, error);
            if (value != null && !value.isEmpty()) {
                checkRegularExpression(column, value, error);
                checkAlphaNumeric(column, value, error);
                checkDate(column, value, error);
                checkMaxLength(column, value, error);
                checkMinLength(column, value, error);
                checkInteger(column, value, error);
                checkGroovy(column, value, error);
                checkValueOf(column, value, error);
                checkDouble(column, value, error);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkGroovy(String column, String value, ValidationError error) {
        String groovyScript = validationConfig.getGroovyRuleFor(column);
        if (groovyScript != null) {

            Script script = scriptCache.get(column);
            if (script == null) {
                script = shell.parse(groovyScript);
                scriptCache.put(column, script);
            }

            Binding binding = new Binding();
            binding.setVariable("value", value);
            script.setBinding(binding);

            Object groovyResult = null;
            try {
                groovyResult = script.run();
            } catch (CompilationFailedException e) {
                error.add("validation.message.groovy.exception", groovyScript, e.getMessage());
                e.printStackTrace();
            }
            if (groovyResult == null) {
                error.add("validation.message.groovy.return.null", groovyScript);
            }

            if (!isScriptResultTrue(groovyResult)) {
                error.add(groovyResult.toString());
            }

        }
    }

    private boolean isScriptResultTrue(Object groovyResult) {
        return groovyResult.equals(true) || groovyResult.toString().trim().toLowerCase().equals("true");
    }

    private void checkValueOf(String column, String value, ValidationError error) {
        List<String> values = validationConfig.getValueOfRuleFor(column);
        if (values != null) {
            if (!values.contains(value)) {
                String commaSeparated = values.stream().collect(joining(", "));
                error.add("validation.message.value.of", value, commaSeparated);
            }
        }
    }

    private void checkBlankOrNull(String column, String value, ValidationError error) {
        if (validationConfig.getNotEmptyRuleFor(column) != null && validationConfig.getNotEmptyRuleFor(column)) {
            if (isBlankOrNull(value)) {
                error.add("validation.message.not.empty");
            }
        }
    }

    private void checkInteger(String column, String value, ValidationError error) {
        if (validationConfig.getIntegerRuleFor(column) != null && validationConfig.getIntegerRuleFor(column)) {
            if (!isInt(value)) {
                error.add("validation.message.integer");
            }
        }
    }

    private void checkDouble(String column, String value, ValidationError error) {
        if (validationConfig.getDoubleRuleFor(column) != null && validationConfig.getDoubleRuleFor(column)) {
            if (!isDouble(value)) {
                error.add("validation.message.double");
            }
        }
    }

    private void checkMinLength(String column, String value, ValidationError error) {
        Integer minLength = validationConfig.getMinLengthRuleFor(column);
        if (minLength != null) {
            if (!minLength(value, minLength)) {
                error.add("validation.message.min.length", minLength.toString());
            }
        }
    }

    private void checkMaxLength(String column, String value, ValidationError error) {
        Integer maxLength = validationConfig.getMaxLengthRuleFor(column);
        if (maxLength != null) {
            if (!maxLength(value, maxLength)) {
                error.add("validation.message.max.length", maxLength.toString());
            }
        }
    }

    private void checkDate(String column, String value, ValidationError error) {
        String dateformat = validationConfig.getDateRuleFor(column);
        if (dateformat != null && !dateformat.trim().isEmpty()) {
            if (!isDate(value, dateformat, true)) {
                error.add("validation.message.date.format", dateformat);
            }
        }
    }

    private void checkAlphaNumeric(String column, String value, ValidationError error) {
        if (validationConfig.getAlphanumericRuleFor(column) != null && validationConfig.getAlphanumericRuleFor(column)) {
            if (!matchRegexp(value, "[0-9a-zA-Z]*")) {
                error.add("validation.message.alphanumeric");
            }
        }
    }

    private void checkRegularExpression(String column, String value, ValidationError error) {
        String regexp = validationConfig.getRegexpRuleFor(column);
        if (regexp != null && !regexp.trim().isEmpty()) {
            if (!matchRegexp(value, regexp)) {
                error.add("validation.message.regexp", regexp);
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

    public void clearScriptCache() {
        scriptCache.clear();
    }
}
