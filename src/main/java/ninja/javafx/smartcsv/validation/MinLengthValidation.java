package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.minLength;

/**
 * Created by abi on 07.08.2016.
 */
public class MinLengthValidation implements Validation {

    private int minLength;

    public MinLengthValidation(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!minLength(value, minLength)) {
            error.add("validation.message.min.length", Integer.toString(minLength));
        }
    }

    @Override
    public Type getType() {
        return Type.MIN_LENGTH;
    }
}
