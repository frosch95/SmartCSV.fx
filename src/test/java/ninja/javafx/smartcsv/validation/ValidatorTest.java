package ninja.javafx.smartcsv.validation;

import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        this.config = config(configcolumn, configValidation, configValue);
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
        ValidationState result = sut.isValid(column, value);

        // assertion
        assertThat(result.isValid(), is(expectedResult));
        if (!expectedResult) {
            assertThat(result.error(), is(expectedError));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // mocks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Config config(String column, String validation, Object value) {

        Config columnConfig = mock(Config.class);
        Config validatorConfig = mock(Config.class);

        when(columnConfig.hasPath(column)).thenReturn(true);
        when(columnConfig.getConfig(column)).thenReturn(validatorConfig);

        when(validatorConfig.hasPath(validation)).thenReturn(true);
        if (value instanceof Boolean) {
            when(validatorConfig.getBoolean(validation)).thenReturn((Boolean) value);
        } else if (value instanceof String) {
            when(validatorConfig.getString(validation)).thenReturn((String) value);
        } else if (value instanceof Integer) {
            when(validatorConfig.getInt(validation)).thenReturn((Integer)value);
        }

        return columnConfig;
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