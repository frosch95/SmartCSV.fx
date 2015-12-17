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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
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

                    List<ValidationMessage> errorMessages = new ArrayList<>();
                    checkBlankOrNull(columnConfig, value, errorMessages);
                    if (value != null) {
                        checkRegularExpression(columnConfig, value, errorMessages);
                        checkAlphaNumeric(columnConfig, value, errorMessages);
                        checkDate(columnConfig, value, errorMessages);
                        checkMaxLength(columnConfig, value, errorMessages);
                        checkMinLength(columnConfig, value, errorMessages);
                        checkInteger(columnConfig, value, errorMessages);
                        checkGroovy(column, columnConfig, value, errorMessages);
                    }

                    if (!errorMessages.isEmpty()) {
                        result = new ValidationError(errorMessages, lineNumber);
                    }
                }
            }
        }
        return result;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkGroovy(String column, Config columnConfig, String value, List<ValidationMessage> result) {
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
                result.add(new ValidationMessage("groovy script '"+groovyScript+"' throws exception: "+e.getMessage()));
                e.printStackTrace();
            }
            if (groovyResult == null) {
                result.add(new ValidationMessage("groovy script '"+groovyScript+"' returns null"));
            }

            if (!isScriptResultTrue(groovyResult)) {
                result.add(new ValidationMessage(groovyResult.toString()));
            }

        }
    }

    private boolean isScriptResultTrue(Object groovyResult) {
        return groovyResult.equals(true) || groovyResult.toString().trim().toLowerCase().equals("true");
    }

    private void checkBlankOrNull(Config columnConfig, String value, List<ValidationMessage> result) {
        if (getBoolean(columnConfig, "not empty")) {
            if (isBlankOrNull(value)) {
                result.add(new ValidationMessage("validation.message.not.empty"));
            }
        }
    }

    private void checkInteger(Config columnConfig, String value, List<ValidationMessage> result) {
        if (getBoolean(columnConfig, "integer")) {
            if (!isInt(value)) {
                result.add(new ValidationMessage("validation.message.integer"));
            }
        }
    }

    private void checkMinLength(Config columnConfig, String value, List<ValidationMessage> result) {
        Integer minLength = getInteger(columnConfig, "minlength");
        if (minLength != null) {
            if (!minLength(value, minLength)) {
                result.add(new ValidationMessage("has not min length of " + minLength));
            }
        }
    }

    private void checkMaxLength(Config columnConfig, String value, List<ValidationMessage> result) {
        Integer maxLength = getInteger(columnConfig, "maxlength");
        if (maxLength != null) {
            if (!maxLength(value, maxLength)) {
                result.add(new ValidationMessage("has not max length of " + maxLength));
            }
        }
    }

    private void checkDate(Config columnConfig, String value, List<ValidationMessage> result) {
        String dateformat = getString(columnConfig, "date");
        if (dateformat != null && !dateformat.trim().isEmpty()) {
            if (!isDate(value, dateformat, true)) {
                result.add(new ValidationMessage("is not a date of format " + dateformat));
            }
        }
    }

    private void checkAlphaNumeric(Config columnConfig, String value, List<ValidationMessage> result) {
        if (getBoolean(columnConfig, "alphanumeric")) {
            if (!matchRegexp(value, "[0-9a-zA-Z]*")) {
                result.add(new ValidationMessage("validation.message.alphanumeric"));
            }
        }
    }

    private void checkRegularExpression(Config columnConfig, String value, List<ValidationMessage> result) {
        String regexp = getString(columnConfig, "regexp");
        if (regexp != null && !regexp.trim().isEmpty()) {
            if (!matchRegexp(value, regexp)) {
                result.add(new ValidationMessage("does not match " + regexp));
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

    public ValidationError isHeaderValid(String[] headerNames) {
        ValidationError result = null;
        if (validationConfig != null) {
            if (validationConfig.hasPath("headers")) {
                Config headerSectionConfig = validationConfig.getConfig("headers");
                if (headerSectionConfig.hasPath("list")) {
                    List<String> headerConfig = headerSectionConfig.getStringList("list");
                    if (headerConfig != null) {
                        if (headerNames.length != headerConfig.size()) {
                            result = new ValidationError(singletonList(new ValidationMessage("number of headers is not correct! there are " +
                                    headerNames.length +
                                    " but there should be " +
                                    headerConfig.size())));
                            return result;
                        }

                        List<ValidationMessage> errorMessages = new ArrayList<>();

                        for(int i=0; i<headerConfig.size(); i++) {
                            String header = headerConfig.get(i);
                            if (!header.equals(headerNames[i])) {
                                errorMessages.add(new ValidationMessage("header number " +
                                        i +
                                        " does not match \"" +
                                        header +
                                        "\" should be \"" +
                                        headerNames[i] +
                                        "\""));
                            }
                        }
                        if (!errorMessages.isEmpty()) {
                            result = new ValidationError(errorMessages);
                        }
                    }
                }
            }
        }
        return result;
    }
}
