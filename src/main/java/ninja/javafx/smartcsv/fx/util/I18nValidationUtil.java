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

package ninja.javafx.smartcsv.fx.util;

import ninja.javafx.smartcsv.validation.ValidationError;
import ninja.javafx.smartcsv.validation.ValidationMessage;

import java.io.StringWriter;
import java.util.List;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;

/**
 * This class makes validation messages readable in supported languages
 */
public class I18nValidationUtil {

    public static String getI18nValidatioMessage(ResourceBundle resourceBundle, List<ValidationError> errors) {

        StringWriter message = new StringWriter();
        errors.forEach(error -> message.append(getI18nValidatioMessage(resourceBundle, error)).append("\n"));


        if (message.toString().length() != 0) {
            return cutOffLastLineBreak(message.toString());
        }

        return "";
    }

    public static String getI18nValidatioMessageWithColumn(ResourceBundle resourceBundle, ValidationError error) {
        return getI18nValidatioMessage(resourceBundle, error, resourceBundle.getString("column") + " " + error.getColumn() + " : ");
    }

    public static String getI18nValidatioMessage(ResourceBundle resourceBundle, ValidationError error) {
        return getI18nValidatioMessage(resourceBundle, error, "");
    }

    private static String getI18nValidatioMessage(ResourceBundle resourceBundle, ValidationError error, String prefix) {

        List<ValidationMessage> validationMessages = error.getMessages();
        StringWriter message = new StringWriter();
        validationMessages.forEach(validationMessage -> {
            message.append(prefix);
            if (resourceBundle.containsKey(validationMessage.getKey())) {
                String resourceText = resourceBundle.getString(validationMessage.getKey());
                if (validationMessage.getParameters().length > 0) {
                    message.append(format(resourceText, (Object[]) validationMessage.getParameters())).append("\n");
                } else {
                    message.append(resourceText).append("\n");
                }
            } else {
                message.append(validationMessage.getKey()).append("\n");
            }
        });

        if (!validationMessages.isEmpty()) {
            return cutOffLastLineBreak(message.toString());
        }

        return "";
    }

    private static String cutOffLastLineBreak(String message) {
        return message.substring(0, message.length()-1);
    }

}
