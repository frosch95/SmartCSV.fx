package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.isInt;

/**
 * Created by abi on 07.08.2016.
 */
public class IntegerValidation implements Validation {

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!isInt(value)) {
            error.add("validation.message.integer");
        }
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }
}
