/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015 javafx.ninja <info@javafx.ninja>                                              
                                                                                                                    
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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ninja.javafx.smartcsv.FileReader;
import org.springframework.stereotype.Service;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.AlwaysQuoteMode;
import org.supercsv.quote.ColumnQuoteMode;
import org.supercsv.quote.NormalQuoteMode;
import org.supercsv.quote.QuoteMode;

import java.io.File;
import java.io.IOException;

import static ninja.javafx.smartcsv.preferences.QuoteModeHelper.getQuoteMode;

/**
 * file reader for the preferences
 */
@Service
public class PreferencesFileReader implements FileReader {

    private Config config;
    private CsvPreference csvPreference;

    public PreferencesFileReader() {
        csvPreference = new CsvPreference.
                Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).
                useQuoteMode(new AlwaysQuoteMode()).build();
    }

    @Override
    public void read(File filename) throws IOException {
        config = ConfigFactory.parseFile(filename);

        if (config != null) {
            char quoteChar = config.getString("quoteChar").charAt(0);
            char delimiterChar = config.getString("delimiterChar").charAt(0);
            String endOfLineSymbols = config.getString("endOfLineSymbols");
            boolean surroundingSpacesNeedQuotes = config.getBoolean("surroundingSpacesNeedQuotes");
            boolean ignoreEmptyLines = config.getBoolean("ignoreEmptyLines");
            String quoteMode = config.getString("quoteMode");

            csvPreference = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLineSymbols)
                    .useQuoteMode(getQuoteMode(quoteMode))
                    .surroundingSpacesNeedQuotes(surroundingSpacesNeedQuotes)
                    .ignoreEmptyLines(ignoreEmptyLines)
                    .build();
        }
    }

    public CsvPreference getCSVpreference() {
        return csvPreference;
    }


}
