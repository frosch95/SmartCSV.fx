package ninja.javafx.smartcsv.validation;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Created by abi on 07.08.2016.
 */
public class ValueOfValidation implements Validation {

    private List<String> values;

    public ValueOfValidation(List<String> values) {
        this.values = values;
    }

    @Override
    public void check(int row, String value, ValidationError error) {
        if (!values.contains(value)) {
            String commaSeparated = values.stream().collect(joining(", "));
            error.add("validation.message.value.of", value, commaSeparated);
        }
    }

    @Override
    public Type getType() {
        return Type.VALUE_OF;
    }
}
