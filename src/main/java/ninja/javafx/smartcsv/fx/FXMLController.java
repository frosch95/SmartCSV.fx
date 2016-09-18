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

package ninja.javafx.smartcsv.fx;


import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public abstract class FXMLController implements InitializingBean, Initializable {

    protected Node view;
    protected String fxmlFilePath;
    protected String resourcePath;

    public abstract void setFxmlFilePath(String filePath);

    @Value("${resource.main}")
    public void setResourceBundle(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadFXML();
    }

    protected final void loadFXML() throws IOException {
        try (InputStream fxmlStream = getClass().getResourceAsStream(fxmlFilePath)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundle.getBundle(this.resourcePath));
            loader.setController(this);
            this.view = (loader.load(fxmlStream));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Node getView() {
        return view;
    }
}