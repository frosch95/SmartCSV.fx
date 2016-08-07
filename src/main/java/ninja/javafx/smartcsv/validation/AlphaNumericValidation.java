package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.matchRegexp;

/**
 * Created by abi on 07.08.2016.
 */
public class AlphaNumericValidation implements Validation {
    @Override
    public void check(int row, String value, ValidationError error) {
        if (!matchRegexp(value, "[0-9a-zA-Z]*")) {
            error.add("validation.message.alphanumeric");
        }
    }

    @Override
    public Type getType() {
        return Type.ALPHANUMERIC;
    }
}
