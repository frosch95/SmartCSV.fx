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

package ninja.javafx.smartcsv.validation;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static ninja.javafx.smartcsv.validation.ConfigMock.headerSectionConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * unit test for header validator
 */
@RunWith(Parameterized.class)
public class HeaderValidationTest {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Config config;
    private Boolean expectedResult;
    private List<String> expectedErrors;
    private String[] headerNames;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // subject under test
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Validator sut;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameterized constructor
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public HeaderValidationTest(String[] configHeaderNames,
                                String[] headerNames,
                                Boolean expectedResult,
                                List<String> expectedErrors) {
        this.config = headerSectionConfig(configHeaderNames);
        this.headerNames = headerNames;
        this.expectedResult = expectedResult;
        this.expectedErrors = expectedErrors;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // init
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Before
    public void initialize() {
        sut = new Validator(config);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    public void validation() {
        // execution
        ValidationError result = sut.isHeaderValid(headerNames);

        // assertion
        assertThat(result == null, is(expectedResult));
        if (!expectedResult) {
            assertTrue(result.getMessages().containsAll(expectedErrors));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameters for tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Parameterized.Parameters
    public static Collection validationConfigurations() {
        return asList(new Object[][] {
                { new String[] {}, new String[] {}, true, null },
                { new String[] {"a"}, new String[] {"a"}, true, null },
                { new String[] {"a"}, new String[] {"b"}, false,  Arrays.asList("header number 0 does not match \"a\" should be \"b\"") },
                { new String[] {"a"}, new String[] {"a","b"}, false,  Arrays.asList("number of headers is not correct! there are 2 but there should be 1") },
                { new String[] {"a", "b"}, new String[] {"b", "a"}, false, Arrays.asList("header number 0 does not match \"a\" should be \"b\"", "header number 1 does not match \"b\" should be \"a\"") }
        });
    }
}
