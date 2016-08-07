package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.maxLength;

/**
 * Created by abi on 07.08.2016.
 */
public class MaxLengthValidation implements Validation {

    private int maxLength;

    public MaxLengthValidation(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!maxLength(value, maxLength)) {
            error.add("validation.message.max.length", Integer.toString(maxLength));
        }
    }

    @Override
    public Type getType() {
        return Type.MAX_LENGTH;
    }
}
