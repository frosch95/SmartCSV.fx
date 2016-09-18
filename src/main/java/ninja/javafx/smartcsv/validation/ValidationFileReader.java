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

package ninja.javafx.smartcsv.validation;

import com.google.gson.GsonBuilder;
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.validation.configuration.Field;
import ninja.javafx.smartcsv.validation.configuration.ValidationConfiguration;

import java.io.File;
import java.io.IOException;

import static ninja.javafx.smartcsv.validation.configuration.Type.*;

/**
 * This class loads the constraints as json config
 */
public class ValidationFileReader implements FileReader<ValidationConfiguration> {

    private ValidationConfiguration config;

    @Override
    public void read(File file) throws IOException {
        config = new GsonBuilder().create().fromJson(new java.io.FileReader(file), ValidationConfiguration.class);
        setDefaults();
    }

    private void setDefaults() {
        for (Field field : config.getFields()) {
            if (field.getType() == null) {
                field.setType(STRING);
            }
            if (field.getType() == DATE) {
                if (field.getFormat() == null) {
                    field.setFormat("yyyy-MM-dd");
                }
            }
            if (field.getType() == DATETIME) {
                if (field.getFormat() == null) {
                    field.setFormat("yyyy-MM-ddThh:mm:ssZ");
                }
            }
            if (field.getType() == TIME) {
                if (field.getFormat() == null) {
                    field.setFormat("hh:mm:ss");
                }
            }
        }
    }

    public ValidationConfiguration getContent() {
        return config;
    }
}
