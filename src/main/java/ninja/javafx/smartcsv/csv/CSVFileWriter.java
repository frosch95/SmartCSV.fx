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

import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * filewriter for the csv
 */
public class CSVFileWriter extends CSVConfigurable implements ninja.javafx.smartcsv.FileWriter<CSVModel> {

    private CSVModel model;

    public void setContent(CSVModel model) {
        this.model = model;
    }

    @Override
    public void write(File filename) throws IOException {
        ICsvMapWriter mapWriter = null;
        try {
            mapWriter = new CsvMapWriter(new FileWriter(filename.getAbsolutePath()), csvPreference);
            mapWriter.writeHeader(model.getHeader());

            for(CSVRow row: model.getRows()) {
                Map<String, String> columns = convertMapFromModel(row);
                mapWriter.write(columns, model.getHeader());
            }
        }
        finally {
            if( mapWriter != null ) {
                mapWriter.close();
            }
        }
    }

    /**
     * transforms the column map from CSVValue to a simple Map<String,String>
     * @param row the row to convert
     * @return a simple map for the supercvs writer
     */
    private Map<String, String> convertMapFromModel(CSVRow row) {
        return row.getColumns().entrySet().stream()
                .collect(
                        toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getValue().getValue() != null ? e.getValue().getValue().getValue() : ""
                        )
                );
    }
}
