package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.isDouble;

/**
 * Created by abi on 07.08.2016.
 */
public class DoubleValidation implements Validation {

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!isDouble(value)) {
            error.add("validation.message.double");
        }
    }

    @Override
    public Type getType() {
        return Type.DOUBLE;
    }
}
