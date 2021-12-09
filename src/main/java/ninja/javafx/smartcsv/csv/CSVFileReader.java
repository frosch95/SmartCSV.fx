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

import de.siegmar.fastcsv.reader.NamedCsvReader;
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * reads the csv file and stores the values in csv model
 */
public class CSVFileReader extends CSVConfigurable implements FileReader<CSVModel> {

    private CSVModel model;

    @Override
    public void read(File file) throws IOException {

        System.out.println(csvPreference);
        try (var csv = getNamedCsvReader(file)) {
            model = new CSVModel();

            // the header columns are used as the keys to the Map
            var header = csv.getHeader().toArray(new String[csv.getHeader().size()]);
            model.setHeader(header);

            csv.forEach(csvRow -> {
                var row = model.addRow();
                for (String column : header) {
                    model.addValue(row, column, csvRow.getField(column));
                }
            });

        } catch (IOException ex) {
            // TODO perhaps a custom NinjaException that can properly identify and localize the exception message
            // is this a file not found? is this a corrupt csv? etc
            throw new IOException("Failed to read " + file + ": " + ex.getMessage(), ex);
        }
    }

    private NamedCsvReader getNamedCsvReader(File file) throws IOException {
        var builder = NamedCsvReader.builder()
                .fieldSeparator(csvPreference.delimiterChar());
        if (csvPreference.quoteChar() != null) {
            builder.quoteCharacter(csvPreference.quoteChar());
        }

        return builder.build(file.toPath(), Charset.forName(fileEncoding));
    }

    public CSVModel getContent() {
        return model;
    }

}
