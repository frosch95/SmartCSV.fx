package ninja.javafx.smartcsv.validation;

/**
 * Created by abi on 09.08.2016.
 */
public abstract class EmptyAllowedValidation  implements Validation {

    @Override
    public boolean canBeChecked(String value) {
        return value != null && !value.isEmpty();
    }
}
