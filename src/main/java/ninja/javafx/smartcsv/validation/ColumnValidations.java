package ninja.javafx.smartcsv.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abi on 07.08.2016.
 */
public class ColumnValidations {

    private Map<String, Map<Validation.Type, Validation>> columnValidationMap = new HashMap<>();

    public void add(String column, Validation validation) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap == null) {
            validationMap = new HashMap<>();
            columnValidationMap.put(column, validationMap);
        }
        validationMap.put(validation.getType(), validation);
    }

    public void remove(String column, Validation.Type type) {
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap != null) {
            validationMap.remove(type);
        }
    }

    public ValidationError isValid(int row, String column, String value) {
        ValidationError error = ValidationError.withLineNumber(row);
        Map<Validation.Type, Validation> validationMap = columnValidationMap.get(column);
        if (validationMap != null) {
            for (Validation validation: validationMap.values()) {

                if (validation.getType() == Validation.Type.NOT_EMPTY) {
                    validation.check(row, value, error);
                } else {
                    if (value != null && !value.isEmpty()) {
                        validation.check(row, value, error);
                    }
                }
            }
        }
        return error;
    }

}
