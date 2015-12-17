package ninja.javafx.smartcsv.validation;

import com.typesafe.config.Config;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class ConfigMock {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String HEADER_SECTION_KEY = "headers";
    private static final String COLUMN_SECTION_KEY = "columns";
    private static final String LIST_KEY = "list";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // mocks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Config headerSectionConfig(String[] headerNames) {

        Config headerSectionConfig = mock(Config.class);
        Config listConfig = mock(Config.class);

        when(headerSectionConfig.hasPath(HEADER_SECTION_KEY)).thenReturn(true);
        when(headerSectionConfig.getConfig(HEADER_SECTION_KEY)).thenReturn(listConfig);

        when(listConfig.hasPath(LIST_KEY)).thenReturn(true);
        when(listConfig.getStringList(LIST_KEY)).thenReturn(asList(headerNames));

        return headerSectionConfig;
    }


    public static Config columnSectionConfig(String column, String validation, Object value) {

        Config columnSectionConfig = mock(Config.class);
        Config columnConfig = mock(Config.class);
        Config validatorConfig = mock(Config.class);

        when(columnSectionConfig.hasPath(COLUMN_SECTION_KEY)).thenReturn(true);
        when(columnSectionConfig.getConfig(COLUMN_SECTION_KEY)).thenReturn(columnConfig);

        when(columnConfig.hasPath(column)).thenReturn(true);
        when(columnConfig.getConfig(column)).thenReturn(validatorConfig);

        when(validatorConfig.hasPath(validation)).thenReturn(true);
        if (value instanceof Boolean) {
            when(validatorConfig.getBoolean(validation)).thenReturn((Boolean) value);
        } else if (value instanceof String) {
            when(validatorConfig.getString(validation)).thenReturn((String) value);
        } else if (value instanceof Integer) {
            when(validatorConfig.getInt(validation)).thenReturn((Integer) value);
        }

        return columnSectionConfig;
    }
}