package ninja.javafx.smartcsv.validation;

import static org.apache.commons.validator.GenericValidator.isDate;

/**
 * Created by abi on 07.08.2016.
 */
public class DateValidation implements Validation {

    private String dateformat;

    public DateValidation(String dateformat) {
        assert dateformat != null && !dateformat.trim().isEmpty() : "empty date format for date validation";
        this.dateformat = dateformat;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!isDate(value, dateformat, true)) {
            error.add("validation.message.date.format", dateformat);
        }
    }

    @Override
    public Type getType() {
        return Type.DATE;
    }
}
