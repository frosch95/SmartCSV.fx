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

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;

/**
 * validation configuration
 */
public class ValidationConfiguration {

    @SerializedName("headers")
    private HeaderConfiguration headerConfiguration = new HeaderConfiguration();

    @SerializedName("columns")
    private Map<String, Map<String, Object>> columnConfigurations = new HashMap<>();

    public String[] headerNames() {
        if (noHeader()) return null;
        return headerConfiguration.getNames();
    }

    public Boolean integerRuleFor(String column) {
        if (noRulesFor(column)) return FALSE;
        return defaultValue((Boolean)columnConfigurations.get(column).get("integer"), FALSE);
    }

    public Boolean doubleRuleFor(String column) {
        if (noRulesFor(column)) return FALSE;
        return defaultValue((Boolean)columnConfigurations.get(column).get("double"), FALSE);
    }

    public Boolean notEmptyRuleFor(String column) {
        if (noRulesFor(column)) return FALSE;
        return defaultValue((Boolean)columnConfigurations.get(column).get("not empty"), FALSE);
    }

    public Integer minLengthRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return doubleToInteger((Double)columnConfigurations.get(column).get("minlength"));
    }

    public Integer maxLengthRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return doubleToInteger((Double)columnConfigurations.get(column).get("maxlength"));
    }

    public String dateRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)columnConfigurations.get(column).get("date");
    }

    public Boolean alphanumericRuleFor(String column) {
        if (noRulesFor(column)) return FALSE;
        return defaultValue((Boolean)columnConfigurations.get(column).get("alphanumeric"), FALSE);
    }

    public String regexpRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)columnConfigurations.get(column).get("regexp");
    }

    public List<String> valueOfRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (List<String>)columnConfigurations.get(column).get("value of");
    }

    public String groovyRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)columnConfigurations.get(column).get("groovy");
    }

    private boolean noHeader() {
        return headerConfiguration == null;
    }

    private boolean noRulesFor(String column) {
        return columnConfigurations == null || columnConfigurations.get(column) == null;
    }

    private <T> T defaultValue(T value, T defaultValue) {
        if (value == null) return defaultValue;
        return value;
    }

    private Integer doubleToInteger(Double value) {
        if (value == null) return null;
        return (int)Math.round(value);
    }

}
