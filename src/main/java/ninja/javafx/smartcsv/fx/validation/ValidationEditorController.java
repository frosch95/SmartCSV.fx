/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2019 javafx.ninja <info@javafx.ninja>
                                                                                                                    
   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
  
*/

package ninja.javafx.smartcsv.fx.validation;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import ninja.javafx.smartcsv.fx.FXMLController;
import ninja.javafx.smartcsv.validation.configuration.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static javafx.beans.binding.Bindings.when;
import static ninja.javafx.smartcsv.validation.configuration.Type.STRING;

/**
 * controller for editing column validations
 *
 * RichText groovy highlighting code is based on the java example of
 * https://github.com/TomasMikula/RichTextFX
 */
@Component
public class ValidationEditorController extends FXMLController {

    private StringProperty selectedColumn = new SimpleStringProperty();
    private ValidationConfiguration validationConfiguration;

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class",
            "continue", "def", "default", "do", "double", "else",
            "enum", "extends", "false", "final", "finally", "float",
            "for", "if", "implements", "import", "in",
            "instanceof", "int", "interface", "length", "long", "native",
            "new", "null", "package", "private", "property", "protected", "public",
            "return", "short", "static", "super",
            "switch", "synchronized", "this", "threadsafe", "throw", "throws",
            "transient", "true", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", (CharSequence[]) KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String STRING2_PATTERN = "'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<STRING2>" + STRING2_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    @FXML
    private ComboBox<Type> typeComboBox;

    @FXML
    private ComboBox<StringFormat> formatComboBox;

    @FXML
    private TextField formatTextField;

    @FXML
    private Spinner<Integer> minLengthSpinner;

    @FXML
    private Spinner<Integer>  maxLengthSpinner;

    @FXML
    private TextField regexpRuleTextField;

    @FXML
    private TextField valueOfRuleTextField;

    @FXML
    private CodeArea groovyRuleTextArea;

    @FXML
    private CheckBox enableNotEmptyRule;

    @FXML
    private CheckBox enableMinLengthRule;

    @FXML
    private CheckBox enableMaxLengthRule;

    @FXML
    private CheckBox enableRegexpRule;

    @FXML
    private CheckBox enableValueOfRule;

    @FXML
    private CheckBox enableGroovyRule;

    @FXML
    private CheckBox enableUniqueRule;


    @Value("${fxml.smartcvs.validation.editor.view}")
    @Override
    public void setFxmlFilePath(String filePath) {
        this.fxmlFilePath = filePath;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTypeAndFormatInput(resources);
        initMinMaxSpinner();
        initSpinner(minLengthSpinner, enableMinLengthRule);
        initSpinner(maxLengthSpinner, enableMaxLengthRule);
        initTextInputControl(regexpRuleTextField, enableRegexpRule);
        initTextInputControl(valueOfRuleTextField, enableValueOfRule);
        initCodeAreaControl(groovyRuleTextArea, enableGroovyRule);
    }

    public String getSelectedColumn() {
        return selectedColumn.get();
    }

    public StringProperty selectedColumnProperty() {
        return selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn.set(selectedColumn);
    }

    public void setValidationConfiguration(ValidationConfiguration validationConfiguration) {
        this.validationConfiguration = validationConfiguration;
    }

    private void initTypeAndFormatInput(ResourceBundle resources) {
        typeComboBox.getItems().addAll(STRING,
                Type.INTEGER,
                Type.NUMBER,
                Type.DATE,
                Type.DATETIME,
                Type.TIME);
        formatComboBox.setCellFactory(new StringFormatEditorCellFactory(resources));
        formatComboBox.setConverter(new StringFormatStringConverter(resources));
        formatComboBox.getItems().addAll(
                StringFormat.DEFAULT,
                StringFormat.EMAIL,
                StringFormat.URI,
                StringFormat.BINARY,
                StringFormat.UUID
        );
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> changeFormat());
    }

    private void updateFormatTextField() {
        switch (typeComboBox.getValue()) {
            case DATE:
            case DATETIME:
            case TIME:
                    formatTextField.setVisible(true);
                    formatTextField.setText(getCurrentFieldConfig().getFormat());
                break;
            default:
                formatTextField.setVisible(false);
                formatTextField.setText(null);
                break;
        }
    }

    private void updateFormatComboBox() {
        switch (typeComboBox.getValue()) {
            case STRING:
                formatComboBox.setVisible(true);
                formatComboBox.getSelectionModel().select(StringFormat.fromExternalValue(getCurrentFieldConfig().getFormat()));
                break;
            default:
                formatComboBox.setVisible(false);
                break;
        }
    }

    private void initMinMaxSpinner() {
        IntegerSpinnerValueFactory minValueFactory = new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        minLengthSpinner.setValueFactory(minValueFactory);
        IntegerSpinnerValueFactory maxValueFactory = new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        maxLengthSpinner.setValueFactory(maxValueFactory);

        minValueFactory.maxProperty().bind(
                when(enableMaxLengthRule.selectedProperty()).
                        then(maxLengthSpinner.valueProperty()).
                        otherwise(Integer.MAX_VALUE));
        maxValueFactory.minProperty().bind(
                when(enableMinLengthRule.selectedProperty()).
                        then(minLengthSpinner.valueProperty()).
                        otherwise(0));

    }


    private void changeFormat() {
        switch (typeComboBox.getValue()) {
            case STRING:
                updateFormatComboBox();
                break;
            case DATE:
            case DATETIME:
            case TIME:
                updateFormatTextField();
                break;
            case INTEGER:
            case NUMBER:
            default:
                // format: no options
                formatComboBox.setVisible(false);
                formatTextField.setVisible(false);
                break;
        }
    }

    public void updateConfiguration() {

        Field config = getCurrentFieldConfig();
        config.setType(typeComboBox.getValue());

        switch (typeComboBox.getValue()) {
            case STRING:
                config.setFormat(formatComboBox.getValue().getExternalValue());
                break;
            case DATE:
            case DATETIME:
            case TIME:
                if (formatTextField.getText().trim().isEmpty()) {
                    config.setFormat(null);
                } else {
                    // TODO: validate input
                    config.setFormat(formatTextField.getText());
                }
                break;
            case INTEGER:
            case NUMBER:
            default:
                // format: no options
                config.setFormat(null);
                break;
        }

        if (enableGroovyRule.isSelected()) {
            config.setGroovy(groovyRuleTextArea.getText());
        } else {
            config.setGroovy(null);
        }

        Constraints constraints = config.getConstraints();
        if (constraints == null) {
            constraints = new Constraints();
            config.setConstraints(constraints);
        }

        if (enableNotEmptyRule.isSelected()) {
            constraints.setRequired(enableNotEmptyRule.isSelected());
        } else {
            constraints.setRequired(null);
        }

        if (enableUniqueRule.isSelected()) {
            constraints.setUnique(enableUniqueRule.isSelected());
        } else {
            constraints.setUnique(null);
        }

        if (enableMinLengthRule.isSelected()) {
            constraints.setMinLength(minLengthSpinner.getValue());
        } else {
            constraints.setMinLength(null);
        }

        if (enableMaxLengthRule.isSelected()) {
            constraints.setMaxLength(maxLengthSpinner.getValue());
        } else {
            constraints.setMaxLength(null);
        }

        if (enableRegexpRule.isSelected()) {
            constraints.setPattern(regexpRuleTextField.getText());
        } else {
            constraints.setPattern(null);
        }

        if (enableValueOfRule.isSelected()) {
            constraints.setEnumeration(asList(valueOfRuleTextField.getText().split(", ")));
        } else {
            constraints.setEnumeration(null);
        }

    }

    private Field getCurrentFieldConfig() {
        return validationConfiguration.getFieldConfiguration(getSelectedColumn());
    }

    private void addDependencyListener(CheckBox rule, CheckBox... dependentRules) {
        rule.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                for (CheckBox dependentRule: dependentRules) {
                    dependentRule.selectedProperty().setValue(false);
                }
            }
        });
    }

    public void updateForm() {

        Field config = getCurrentFieldConfig();

        if (config.getType() != null) {
            typeComboBox.setValue(config.getType());
        } else {
            typeComboBox.setValue(STRING);
        }

        updateFormatComboBox();
        updateFormatTextField();

        updateCodeAreaControl(
                groovyRuleTextArea,
                config.getGroovy(),
                enableGroovyRule
        );

        Constraints constraints = config.getConstraints();
        updateCheckBox(constraints != null ? constraints.getRequired() : FALSE, enableNotEmptyRule);
        updateCheckBox(constraints != null ? constraints.getUnique() : FALSE, enableUniqueRule);

        updateSpinner(
                minLengthSpinner,
                constraints != null ? constraints.getMinLength() : null,
                enableMinLengthRule
        );

        updateSpinner(
                maxLengthSpinner,
                constraints != null ? constraints.getMaxLength() : null,
                enableMaxLengthRule
        );

        updateTextInputControl(
                regexpRuleTextField,
                constraints != null ? constraints.getPattern() : null,
                enableRegexpRule
        );

        updateTextInputControl(
                valueOfRuleTextField,
                constraints != null ? constraints.getEnumeration() : null,
                enableValueOfRule
        );


    }

    private void updateCheckBox(Boolean value, CheckBox ruleEnabled) {
        if (value == null) {
            ruleEnabled.setSelected(false);
        } else {
            ruleEnabled.setSelected(value);
        }
    }

    private void updateSpinner(Spinner rule, Integer value, CheckBox ruleEnabled) {
        if (value == null) {
            ruleEnabled.setSelected(false);
        } else {
            ruleEnabled.setSelected(true);
            rule.getValueFactory().setValue(value);
        }
    }

    private void updateTextInputControl(TextInputControl rule, String value, CheckBox ruleEnabled) {
        if (value == null) {
            ruleEnabled.setSelected(false);
        } else {
            ruleEnabled.setSelected(true);
            rule.setText(value);
        }
    }

    private void updateTextInputControl(TextInputControl rule, List<String> values, CheckBox ruleEnabled) {
        if (values == null || values.isEmpty()) {
            ruleEnabled.setSelected(false);
        } else {
            ruleEnabled.setSelected(true);
            rule.setText(values.stream().collect(joining(", ")));
        }
    }

    private void updateCodeAreaControl(CodeArea rule, String value, CheckBox ruleEnabled) {
        if (value == null) {
            ruleEnabled.setSelected(false);
        } else {
            ruleEnabled.setSelected(true);
            rule.replaceText(0, rule.getLength(), value);
        }
    }

    private void initSpinner(Spinner rule, CheckBox ruleEnabled) {
        rule.disableProperty().bind(ruleEnabled.selectedProperty().not());
        ruleEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                rule.getValueFactory().setValue(0);
            }
        });
    }

    private void initTextInputControl(TextInputControl rule, CheckBox ruleEnabled) {
        rule.disableProperty().bind(ruleEnabled.selectedProperty().not());
        ruleEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                rule.setText("");
            }
        });
    }

    private void initCodeAreaControl(CodeArea rule, CheckBox ruleEnabled) {
        rule.disableProperty().bind(ruleEnabled.selectedProperty().not());
        ruleEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                rule.clear();
            }
        });
        rule.setParagraphGraphicFactory(LineNumberFactory.get(rule));
        rule.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    rule.setStyleSpans(0, computeHighlighting(rule.getText()));
                });
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("STRING2") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                        null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
