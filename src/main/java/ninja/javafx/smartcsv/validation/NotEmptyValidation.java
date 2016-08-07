package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

/**
 * Created by abi on 07.08.2016.
 */
public class NotEmptyValidation implements Validation {

    @Override
    public void check(int row, String value, ValidationError error) {
        if (isBlankOrNull(value)) {
            error.add("validation.message.not.empty");
        }
    }

    @Override
    public Type getType() {
        return Type.NOT_EMPTY;
    }
}
