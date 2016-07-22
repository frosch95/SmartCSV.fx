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

import com.google.gson.GsonBuilder;
import ninja.javafx.smartcsv.FileReader;
import org.springframework.stereotype.Service;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.AlwaysQuoteMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ninja.javafx.smartcsv.preferences.QuoteModeHelper.getQuoteMode;

/**
 * file reader for the preferences
 */
public class PreferencesFileReader implements FileReader<CsvPreference> {

    private Map config;
    private CsvPreference csvPreference;

    public PreferencesFileReader() {
        csvPreference = new CsvPreference.
                Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).
                useQuoteMode(new AlwaysQuoteMode()).build();
    }

    @Override
    public void read(File filename) throws IOException {
        config = new GsonBuilder().create().fromJson(new java.io.FileReader(filename), HashMap.class);

        if (config != null) {
            char quoteChar = config.get("quoteChar").toString().charAt(0);
            char delimiterChar = config.get("delimiterChar").toString().charAt(0);
            String endOfLineSymbols = config.get("endOfLineSymbols").toString();
            boolean surroundingSpacesNeedQuotes = (Boolean)config.get("surroundingSpacesNeedQuotes");
            boolean ignoreEmptyLines = (Boolean)config.get("ignoreEmptyLines");
            String quoteMode = config.get("quoteMode").toString();

            csvPreference = new CsvPreference.Builder(quoteChar, delimiterChar, endOfLineSymbols)
                    .useQuoteMode(getQuoteMode(quoteMode))
                    .surroundingSpacesNeedQuotes(surroundingSpacesNeedQuotes)
                    .ignoreEmptyLines(ignoreEmptyLines)
                    .build();
        }
    }

    public CsvPreference getContent() {
        return csvPreference;
    }


}
