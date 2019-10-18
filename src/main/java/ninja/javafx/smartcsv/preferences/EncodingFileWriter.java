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

package ninja.javafx.smartcsv.preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ninja.javafx.smartcsv.FileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Save the file encoding to configuration file
 */
public class EncodingFileWriter implements FileWriter<String> {

    private String fileEncoding;

    @Override
    public void setContent(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    @Override
    public void write(File filename) throws IOException {
        Map<String, String> encodings = new HashMap<>();
        encodings.put("fileEncoding", CharsetHelper.getCharsetName(fileEncoding));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.write(filename.toPath(), gson.toJson(encodings).getBytes());
    }

}
