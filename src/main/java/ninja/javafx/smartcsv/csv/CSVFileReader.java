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

package ninja.javafx.smartcsv.csv;

import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * reads the csv file and stores the values in csv model
 */
public class CSVFileReader extends CSVConfigurable implements FileReader<CSVModel> {

    private CSVModel model;

    @Override
    public void read(File file) throws IOException {

        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new java.io.FileReader(file.getAbsoluteFile()), csvPreference);
            model = new CSVModel();

            // the header columns are used as the keys to the Map
            String[] header = mapReader.getHeader(true);
            model.setHeader(header);

            Map<String, String> customerMap;
            while ((customerMap = mapReader.read(header)) != null) {
                CSVRow row = model.addRow();
                for (String column : header) {
                    model.addValue(row, column, customerMap.get(column));
                }
            }

        } finally {
            if (mapReader != null) {
                mapReader.close();
            }
        }
    }

    public CSVModel getContent() {
        return model;
    }

}
