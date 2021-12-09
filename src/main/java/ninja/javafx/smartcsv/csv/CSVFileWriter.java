/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2021 javafx.ninja <info@javafx.ninja>
                                                                                                                    
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

import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.table.model.CSVRow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
        try (var writer = getCsvWriter(filename)){
            writer.writeRow(model.getHeader());
            for(CSVRow row: model.getRows()) {
                writer.writeRow(convertMapFromModel(row));
            }
        }
    }

    private CsvWriter getCsvWriter(File filename) throws IOException {
        var writer = CsvWriter.builder().fieldSeparator(csvPreference.delimiterChar());
        if (csvPreference.quoteChar() != null) {
            writer.quoteCharacter(csvPreference.quoteChar());
            writer.quoteStrategy(QuoteStrategy.ALWAYS);
        }

        return writer.build(filename.toPath(), Charset.forName(fileEncoding));
    }

    /**
     * transforms the column map from CSVValue to a simple Map<String,String>
     * @param row the row to convert
     * @return a simple map for the supercvs writer
     */
    private List<String> convertMapFromModel(CSVRow row) {
        return row.getColumns().values().stream().map(v -> v.get().getValue())
                .collect(toList());
    }
}
