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

import java.util.ArrayList;
import java.util.List;

/**
 * validation configuration
 */
public class ValidationConfiguration {

    @SerializedName("fields")
    private FieldConfiguration[] fieldConfigurations;

    public FieldConfiguration[] getFieldConfigurations() {
        return fieldConfigurations;
    }

    public void setFieldConfigurations(FieldConfiguration[] fieldConfigurations) {
        this.fieldConfigurations = fieldConfigurations;
    }

    public FieldConfiguration getFieldConfiguration(String column) {
        for (FieldConfiguration fieldConfiguration: fieldConfigurations) {
            if (fieldConfiguration.getName().equals(column)) {
                return fieldConfiguration;
            }
        }
        return null;
    }

    public String[] headerNames() {
        if (fieldConfigurations != null) {
            List<String> headerNames = new ArrayList<>();
            for (FieldConfiguration fieldConfiguration: fieldConfigurations) {
                headerNames.add(fieldConfiguration.getName());
            }
            return headerNames.toArray(new String[headerNames.size()]);
        }

        return null;
    }

    public void setHeaderNames(String[] header) {
        fieldConfigurations = new FieldConfiguration[header.length];
        int i = 0;
        for (String headerName: header) {
            fieldConfigurations[i] = new FieldConfiguration();
            fieldConfigurations[i].setName(headerName);
            i++;
        }
    }
}
