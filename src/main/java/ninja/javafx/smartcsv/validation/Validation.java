package ninja.javafx.smartcsv.validation;

/**
 * Created by abi on 07.08.2016.
 */
public interface Validation {

    enum Type { NOT_EMPTY, UNIQUE, DOUBLE, INTEGER, MIN_LENGTH, MAX_LENGTH, DATE, ALPHANUMERIC, REGEXP, VALUE_OF, GROOVY }
    void check(int row, String value, ValidationError error);
    Type getType();
}
