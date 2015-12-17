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

import com.typesafe.config.Config;
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

    private Config validationConfig;
    private GroovyShell shell = new GroovyShell();
    private Map<String, Script> scriptCache = new HashMap<>();


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * JSON configuration for this validator
     * @param validationConfig
     */
    public Validator(Config validationConfig) {
         this.validationConfig = validationConfig;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // oublic methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * checks if the value is valid for the column configuration
     * @param column the column name
     * @param value the value to check
     * @return ValidationError with information if valid and if not which getMessage happened
     */
    public ValidationError isValid(String column, String value, Integer lineNumber) {
        ValidationError result = null;
        if (validationConfig != null) {
            Config columnSectionConfig = getColumnSectionConfig();
            if (columnSectionConfig != null) {
                Config columnConfig = getColumnConfig(columnSectionConfig, column);
                if (columnConfig != null) {

                    ValidationError error = ValidationError.withLineNumber(lineNumber);
                    checkBlankOrNull(columnConfig, value, error);
                    if (value != null) {
                        checkRegularExpression(columnConfig, value, error);
                        checkAlphaNumeric(columnConfig, value, error);
                        checkDate(columnConfig, value, error);
                        checkMaxLength(columnConfig, value, error);
                        checkMinLength(columnConfig, value, error);
                        checkInteger(columnConfig, value, error);
                        checkGroovy(column, columnConfig, value, error);
                        checkValueOf(columnConfig, value, error);
                        checkDouble(columnConfig, value, error);
                    }

                    if (!error.isEmpty()) {
                        result = error;
                    }
                }
            }
        }
        return result;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkGroovy(String column, Config columnConfig, String value, ValidationError error) {
        String groovyScript = getString(columnConfig, "groovy");
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

    private void checkValueOf(Config columnConfig, String value, ValidationError error) {
        List<String> stringList = getStringList(columnConfig, "value of");
        if (stringList != null) {
            if (!stringList.contains(value)) {
                String commaSeparated = stringList.stream().collect(joining(", "));
                error.add("validation.message.value.of", value, commaSeparated);
            }
        }
    }

    private void checkBlankOrNull(Config columnConfig, String value, ValidationError error) {
        if (getBoolean(columnConfig, "not empty")) {
            if (isBlankOrNull(value)) {
                error.add("validation.message.not.empty");
            }
        }
    }

    private void checkInteger(Config columnConfig, String value, ValidationError error) {
        if (getBoolean(columnConfig, "integer")) {
            if (!isInt(value)) {
                error.add("validation.message.integer");
            }
        }
    }

    private void checkDouble(Config columnConfig, String value, ValidationError error) {
        if (getBoolean(columnConfig, "double")) {
            if (!isDouble(value)) {
                error.add("validation.message.double");
            }
        }
    }


    private void checkMinLength(Config columnConfig, String value, ValidationError error) {
        Integer minLength = getInteger(columnConfig, "minlength");
        if (minLength != null) {
            if (!minLength(value, minLength)) {
                error.add("validation.message.min.length", minLength.toString());
            }
        }
    }

    private void checkMaxLength(Config columnConfig, String value, ValidationError error) {
        Integer maxLength = getInteger(columnConfig, "maxlength");
        if (maxLength != null) {
            if (!maxLength(value, maxLength)) {
                error.add("validation.message.max.length", maxLength.toString());
            }
        }
    }

    private void checkDate(Config columnConfig, String value, ValidationError error) {
        String dateformat = getString(columnConfig, "date");
        if (dateformat != null && !dateformat.trim().isEmpty()) {
            if (!isDate(value, dateformat, true)) {
                error.add("validation.message.date.format", dateformat);
            }
        }
    }

    private void checkAlphaNumeric(Config columnConfig, String value, ValidationError error) {
        if (getBoolean(columnConfig, "alphanumeric")) {
            if (!matchRegexp(value, "[0-9a-zA-Z]*")) {
                error.add("validation.message.alphanumeric");
            }
        }
    }

    private void checkRegularExpression(Config columnConfig, String value, ValidationError error) {
        String regexp = getString(columnConfig, "regexp");
        if (regexp != null && !regexp.trim().isEmpty()) {
            if (!matchRegexp(value, regexp)) {
                error.add("validation.message.regexp", regexp);
            }
        }
    }

    private Config getColumnSectionConfig() {
        return validationConfig.hasPath("columns") ? validationConfig.getConfig("columns") : null;
    }

    private Config getColumnConfig(Config columnSectionConfig, String column) {
        return columnSectionConfig.hasPath(column) ? columnSectionConfig.getConfig(column) : null;
    }


    private String getString(Config columnConfig, String path) {
        return columnConfig.hasPath(path) ? columnConfig.getString(path) : null;
    }

    private Integer getInteger(Config columnConfig, String path) {
        return columnConfig.hasPath(path) ? columnConfig.getInt(path) : null;
    }

    private boolean getBoolean(Config columnConfig, String path) {
        return columnConfig.hasPath(path) && columnConfig.getBoolean(path);
    }

    private List<String> getStringList(Config columnConfig, String path) {
        return columnConfig.hasPath(path) ? columnConfig.getStringList(path) : null;
    }

    public ValidationError isHeaderValid(String[] headerNames) {
        ValidationError result = null;
        if (validationConfig != null) {
            if (validationConfig.hasPath("headers")) {
                Config headerSectionConfig = validationConfig.getConfig("headers");
                List<String> headerConfig = getStringList(headerSectionConfig, "list");
                if (headerConfig != null) {
                    if (headerNames.length != headerConfig.size()) {
                        result = ValidationError.withoutLineNumber().add("validation.message.header.length",
                                Integer.toString(headerNames.length),
                                Integer.toString(headerConfig.size()));
                        return result;
                    }

                    ValidationError error = ValidationError.withoutLineNumber();

                    for(int i=0; i<headerConfig.size(); i++) {
                        String header = headerConfig.get(i);
                        if (!header.equals(headerNames[i])) {
                            error.add("validation.message.header.match",
                                    Integer.toString(i),
                                    header,
                                    headerNames[i]);
                        }
                    }
                    if (!error.isEmpty()) {
                        result = error;
                    }
                }
            }
        }
        return result;
    }
}
