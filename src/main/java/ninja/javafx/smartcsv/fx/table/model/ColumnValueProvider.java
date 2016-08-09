package ninja.javafx.smartcsv.fx.table.model;

/**
 * interface for easier access to values in a column
 */
public interface ColumnValueProvider {

    String getValue(int row, String column);
    int getNumberOfRows();

}
