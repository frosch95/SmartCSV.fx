package ninja.javafx.smartcsv.validation;

import java.util.HashMap;

/**
 * Created by abi on 07.08.2016.
 */
public class UniqueValidation implements Validation {

    private HashMap<String, Integer> columnValueMap = new HashMap<>();

    @Override
    public void check(int row, String value, ValidationError error) {
        Integer valueInLineNumber = columnValueMap.get(value);
        if (valueInLineNumber != null) {
            if (!valueInLineNumber.equals(row)) {
                valueInLineNumber += 1; // show not 0 based line numbers to user
                error.add("validation.message.uniqueness", value, valueInLineNumber.toString());
            }
        } else {
            columnValueMap.put(value, row);
        }
    }

    @Override
    public Type getType() {
        return Type.UNIQUE;
    }
}
