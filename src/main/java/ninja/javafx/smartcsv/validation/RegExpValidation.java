package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.matchRegexp;

/**
 * Created by abi on 07.08.2016.
 */
public class RegExpValidation implements Validation {

    private String regexp;

    public RegExpValidation(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!matchRegexp(value, regexp)) {
            error.add("validation.message.regexp", regexp);
        }
    }

    @Override
    public Type getType() {
        return Type.REGEXP;
    }
}
