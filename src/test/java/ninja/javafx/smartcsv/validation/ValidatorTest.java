package ninja.javafx.smartcsv.validation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * unit test for validator
 */
@RunWith(Parameterized.class)
public class ValidatorTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private ValidationConfiguration config;
    private String column;
    private String value;
    private Boolean expectedResult;
    private ValidationMessage expectedError;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // subject under test
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Validator sut;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameterized constructor
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ValidatorTest(String config,
                         String column,
                         String value,
                         Boolean expectedResult,
                         ValidationMessage expectedError) {
        System.out.println(config);
        Gson gson = new GsonBuilder().create();
        this.config = gson.fromJson(config, ValidationConfiguration.class);
        this.column = column;
        this.value = value;
        this.expectedResult = expectedResult;
        this.expectedError = expectedError;
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
        System.out.println(column + "  " + value + "  " + expectedResult + "  " + expectedError);

        // execution
        ValidationError result = sut.isValid(0, column, value);

        // assertion
        assertThat(result == null, is(expectedResult));
        if (!expectedResult) {
            assertThat(result.getMessages(), contains(expectedError));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameters for tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Parameterized.Parameters
    public static Collection validationConfigurations() {
        return asList(new Object[][] {
                { json("column", "not empty", true), "column", "value", true, null },
                { json("column", "not empty", true), "column", "", false, new ValidationMessage("validation.message.not.empty") },
                { json("column", "not empty", true), "column", null, false, new ValidationMessage("validation.message.not.empty") },
                { json("column", "integer", true), "column", "999", true, null },
                { json("column", "integer", true), "column", "a", false, new ValidationMessage("validation.message.integer") },
                { json("column", "double", true), "column", "999", true, null },
                { json("column", "double", true), "column", "999.000", true, null },
                { json("column", "double", true), "column", "a", false, new ValidationMessage("validation.message.double") },
                { json("column", "minlength", 2), "column", "12", true, null },
                { json("column", "minlength", 2), "column", "1", false, new ValidationMessage("validation.message.min.length", "2") },
                { json("column", "maxlength", 2), "column", "12", true, null },
                { json("column", "maxlength", 2), "column", "123", false, new ValidationMessage("validation.message.max.length", "2") },
                { json("column", "date", "yyyyMMdd"), "column", "20151127", true, null },
                { json("column", "date", "yyyyMMdd"), "column", "27.11.2015", false, new ValidationMessage("validation.message.date.format", "yyyyMMdd") },
                { json("column", "alphanumeric", true), "column", "abcABC123", true, null },
                { json("column", "alphanumeric", true), "column", "-abcABC123", false, new ValidationMessage("validation.message.alphanumeric") },
                { json("column", "regexp", "[a-z]*"), "column", "abc", true, null },
                { json("column", "regexp", "[a-z]*"), "column", "abcA", false, new ValidationMessage("validation.message.regexp", "[a-z]*") },
                { json("column", "groovy", "value.contains('a')? 'true' : 'no a inside'"), "column", "abcdef", true, null },
                { json("column", "groovy", "value.contains('a')? 'true' : 'no a inside'"), "column", "bcdefg", false, new ValidationMessage("no a inside") },
                { json("column", "value of", asList("a","b","c","d","e")), "column", "c", true, null },
                { json("column", "value of", asList("a","b","c","d","e")), "column", "f", false, new ValidationMessage("validation.message.value.of", "f", "a, b, c, d, e") },
        });
    }

    public static String json(String column, String rule, Object value) {
        String json = "{\"headers\": { \"list\": [\""+column+"\"]},\"columns\":{\"" + column + "\":{\"" + rule + "\":";
        if (value instanceof String) {
            json += "\""+ value + "\"";
        } else if (value instanceof List) {
            List<String> list = (List<String>)value;
            json += "[" + list.stream().collect(joining(", ")) + "]";
        } else {
            json += value;
        }
        json += "}}}";
        return json;
    }
}