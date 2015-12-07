package ninja.javafx.smartcsv.validation;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static java.util.Arrays.asList;
import static ninja.javafx.smartcsv.validation.ConfigMock.columnSectionConfig;
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
    private Config config;
    private String column;
    private String value;
    private Boolean expectedResult;
    private String expectedError;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // subject under test
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Validator sut;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameterized constructor
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ValidatorTest(String configcolumn,
                         String configValidation,
                         Object configValue,
                         String column,
                         String value,
                         Boolean expectedResult,
                         String expectedError) {
        this.config = columnSectionConfig(configcolumn, configValidation, configValue);
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
        // execution
        ValidationError result = sut.isValid(column, value, 0);

        // assertion
        assertThat(result == null, is(expectedResult));
        if (!expectedResult) {
            assertThat(result.getMessage(), is(expectedError));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // parameters for tests
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Parameterized.Parameters
    public static Collection validationConfigurations() {
        return asList(new Object[][] {
                { "column", "not empty", true, "column", "value", true, null },
                { "column", "not empty", true, "column", "", false, "should not be empty\n" },
                { "column", "not empty", true, "column", null, false, "should not be empty\n" },
                { "column", "integer", true, "column", "999", true, null },
                { "column", "integer", true, "column", "a", false, "should be an integer\n" },
                { "column", "minlength", 2, "column", "12", true, null },
                { "column", "minlength", 2, "column", "1", false, "has not min length of 2\n" },
                { "column", "maxlength", 2, "column", "12", true, null },
                { "column", "maxlength", 2, "column", "123", false, "has not max length of 2\n" },
                { "column", "date", "yyyyMMdd", "column", "20151127", true, null },
                { "column", "date", "yyyyMMdd", "column", "27.11.2015", false, "is not a date of format yyyyMMdd\n" },
                { "column", "alphanumeric", true, "column", "abcABC123", true, null },
                { "column", "alphanumeric", true, "column", "-abcABC123", false, "should not be alphanumeric\n" },
                { "column", "regexp", "[a-z]*", "column", "abc", true, null },
                { "column", "regexp", "[a-z]*", "column", "abcA", false, "does not match [a-z]*\n" },
                { "column", "groovy", "value.contains('a')? 'true' : 'no a inside'", "column", "abcdef", true, null },
                { "column", "groovy", "value.contains('a')? 'true' : 'no a inside'", "column", "bcdefg", false, "no a inside\n" },
        });
    }


}