/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2019 javafx.ninja <info@javafx.ninja>
                                                                                                                    
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

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ninja.javafx.smartcsv.files.FileStorage;

/**
 * Service class for async save of a csv file
 */
@org.springframework.stereotype.Service
public class SaveFileService extends Service<Void> {

    private FileStorage<?> file;

    public void setFileStorage(FileStorage<?> value) {
        file = value;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    file.save();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        };
    }

}
