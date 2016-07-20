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

    public Boolean getIntegerRuleFor(String column) {
        return (Boolean)getValue(column, "integer");
    }

    public Boolean getDoubleRuleFor(String column) {
        return (Boolean)getValue(column, "double");
    }

    public Boolean getNotEmptyRuleFor(String column) {
        return (Boolean)getValue(column, "not empty");
    }

    public Integer getMinLengthRuleFor(String column) {
        return doubleToInteger((Double)getValue(column, "minlength"));
    }

    public Integer getMaxLengthRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return doubleToInteger((Double)getValue(column, "maxlength"));
    }

    public String getDateRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)getValue(column, "date");
    }

    public Boolean getAlphanumericRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (Boolean)getValue(column, "alphanumeric");
    }

    public String getRegexpRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)getValue(column, "regexp");
    }

    public List<String> getValueOfRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (List<String>)getValue(column, "value of");
    }

    public String getGroovyRuleFor(String column) {
        if (noRulesFor(column)) return null;
        return (String)getValue(column, "groovy");
    }

    public void setIntegerRuleFor(String column, Boolean value) {
        setValue(column, value, "integer");
    }

    public void setDoubleRuleFor(String column, Boolean value) {
        setValue(column, value, "double");
    }

    public void setNotEmptyRuleFor(String column, Boolean value) {
        setValue(column, value, "not empty");
    }

    public void setMinLengthRuleFor(String column, Integer value) {
        setValue(column, value == null ? null : value.doubleValue(), "minlength");
    }

    public void setMaxLengthRuleFor(String column, Integer value) {
        setValue(column, value == null ? null : value.doubleValue(), "maxlength");
    }

    public void setDateRuleFor(String column, String value) {
        setValue(column, value, "date");
    }

    public void setAlphanumericRuleFor(String column, Boolean value) {
        setValue(column, value, "alphanumeric");
    }

    public void setRegexpRuleFor(String column, String value) {
        setValue(column, value, "regexp");
    }

    public void setValueOfRuleFor(String column, List<String> value) {
        setValue(column, value, "value of");
    }

    public void setGroovyRuleFor(String column, String value) {
        setValue(column, value, "groovy");
    }

    private void setValue(String column, Object value, String key) {
        if (!columnConfigurations.containsKey(column)) {
            columnConfigurations.put(column, new HashMap<>());
        }

        if (value == null && columnConfigurations.get(column).containsKey(key)) {
            columnConfigurations.get(column).remove(key);
        } else {
            columnConfigurations.get(column).put(key, value);
        }
    }

    private Object getValue(String column, String key) {
        if (noRulesFor(column)) return null;
        return columnConfigurations.get(column).get(key);
    }

    private boolean noHeader() {
        return headerConfiguration == null;
    }

    private boolean noRulesFor(String column) {
        return columnConfigurations == null || columnConfigurations.get(column) == null;
    }

    private Integer doubleToInteger(Double value) {
        if (value == null) return null;
        return (int)Math.round(value);
    }

}
