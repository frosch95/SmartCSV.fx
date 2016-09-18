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

package ninja.javafx.smartcsv.fx.validation;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ninja.javafx.smartcsv.validation.configuration.StringFormat;

import java.util.ResourceBundle;

/**
 * cell factory for string formats
 */
public class StringFormatEditorCellFactory implements Callback<ListView<StringFormat>, ListCell<StringFormat>> {

    private ResourceBundle resourceBundle;

    public StringFormatEditorCellFactory(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    public ListCell<StringFormat> call(ListView<StringFormat> param) {
        return new ListCell<StringFormat>(){
            @Override
            protected void updateItem(StringFormat item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setText(resourceBundle.getString("format.type."+item));
                }
            }
        } ;
    }
}
