/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015 javafx.ninja <info@javafx.ninja>                                              
                                                                                                                    
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import ninja.javafx.smartcsv.fx.FXMLController;
import ninja.javafx.smartcsv.validation.FieldConfiguration;
import ninja.javafx.smartcsv.validation.ValidationConfiguration;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static javafx.beans.binding.Bindings.when;
import static ninja.javafx.smartcsv.validation.ValidationFormatHelper.doubleToInteger;

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

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
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
    private ComboBox<FieldConfiguration.Type> typeComboBox;

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

        typeComboBox.getItems().addAll(FieldConfiguration.Type.STRING,
                FieldConfiguration.Type.INTEGER,
                FieldConfiguration.Type.NUMBER,
                FieldConfiguration.Type.DATE,
                FieldConfiguration.Type.DATETIME,
                FieldConfiguration.Type.TIME);

        initMinMaxSpinner();

        initSpinner(minLengthSpinner, enableMinLengthRule);
        initSpinner(maxLengthSpinner, enableMaxLengthRule);
        initTextInputControl(regexpRuleTextField, enableRegexpRule);
        initTextInputControl(valueOfRuleTextField, enableValueOfRule);
        initCodeAreaControl(groovyRuleTextArea, enableGroovyRule);


        selectedColumn.addListener(observable -> {
            updateForm();
        });
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

    public void updateConfiguration() {

//        if (enableIntegerRule.isSelected()) {
//            validationConfiguration.setIntegerRuleFor(selectedColumn.getValue(), enableIntegerRule.isSelected());
//        } else {
//            validationConfiguration.setIntegerRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableNotEmptyRule.isSelected()) {
//            validationConfiguration.setNotEmptyRuleFor(selectedColumn.getValue(), enableNotEmptyRule.isSelected());
//        } else {
//            validationConfiguration.setNotEmptyRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableUniqueRule.isSelected()) {
//            validationConfiguration.setUniqueRuleFor(selectedColumn.getValue(), enableUniqueRule.isSelected());
//        } else {
//            validationConfiguration.setUniqueRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableDoubleRule.isSelected()) {
//            validationConfiguration.setDoubleRuleFor(selectedColumn.getValue(), enableDoubleRule.isSelected());
//        } else {
//            validationConfiguration.setDoubleRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableAlphanumericRule.isSelected()) {
//            validationConfiguration.setAlphanumericRuleFor(selectedColumn.getValue(), enableAlphanumericRule.isSelected());
//        } else {
//            validationConfiguration.setAlphanumericRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableDateRule.isSelected()) {
//            validationConfiguration.setDateRuleFor(selectedColumn.getValue(), dateformatRuleTextField.getText());
//        } else {
//            validationConfiguration.setDateRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableGroovyRule.isSelected()) {
//            validationConfiguration.setGroovyRuleFor(selectedColumn.getValue(), groovyRuleTextArea.getText());
//        } else {
//            validationConfiguration.setGroovyRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableMinLengthRule.isSelected()) {
//            validationConfiguration.setMinLengthRuleFor(selectedColumn.getValue(), minLengthSpinner.getValue());
//        } else {
//            validationConfiguration.setMinLengthRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableMaxLengthRule.isSelected()) {
//            validationConfiguration.setMaxLengthRuleFor(selectedColumn.getValue(), maxLengthSpinner.getValue());
//        } else {
//            validationConfiguration.setMaxLengthRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableRegexpRule.isSelected()) {
//            validationConfiguration.setRegexpRuleFor(selectedColumn.getValue(), regexpRuleTextField.getText());
//        } else {
//            validationConfiguration.setRegexpRuleFor(selectedColumn.getValue(), null);
//        }
//
//        if (enableValueOfRule.isSelected()) {
//            validationConfiguration.setValueOfRuleFor(selectedColumn.getValue(), asList(valueOfRuleTextField.getText().split(", ")));
//        } else {
//            validationConfiguration.setValueOfRuleFor(selectedColumn.getValue(), null);
//        }

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

    private void updateForm() {

        updateCheckBox(
                (Boolean)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("required"),
                enableNotEmptyRule
        );

        updateCheckBox(
                (Boolean)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("unique"),
                enableUniqueRule
        );

        updateSpinner(
                minLengthSpinner,
                doubleToInteger((Double)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("minLength")),
                enableMinLengthRule
        );

        updateSpinner(
                maxLengthSpinner,
                doubleToInteger((Double)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("maxLength")),
                enableMaxLengthRule
        );

        updateTextInputControl(
                regexpRuleTextField,
                (String)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("pattern"),
                enableRegexpRule
        );

        updateTextInputControl(
                valueOfRuleTextField,
                (String)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("enum"),
                enableValueOfRule
        );

        updateCodeAreaControl(
                groovyRuleTextArea,
                (String)validationConfiguration.getFieldConfiguration(getSelectedColumn()).getConstraints().get("groovy"),
                enableGroovyRule
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
            rule.replaceText(0, 0, value);
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
