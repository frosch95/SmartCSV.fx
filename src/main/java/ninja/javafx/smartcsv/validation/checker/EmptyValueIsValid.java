package ninja.javafx.smartcsv.validation.checker;

/**
 * validations based on this are not validated when the value is null or empty
 */
public abstract class EmptyValueIsValid implements Validation {

    @Override
    public boolean canBeChecked(String value) {
        return value != null && !value.isEmpty();
    }
}
