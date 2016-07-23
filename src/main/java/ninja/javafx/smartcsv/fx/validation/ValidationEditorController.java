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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ninja.javafx.smartcsv.fx.FXMLController;
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
    private CheckBox notEmptyRuleCheckBox;

    @FXML
    private CheckBox integerRuleCheckBox;

    @FXML
    private CheckBox doublerRuleCheckBox;

    @FXML
    private CheckBox alphanumericRuleCheckBox;

    @FXML
    private Spinner<Integer> minLengthSpinner;

    @FXML
    private Spinner<Integer>  maxLengthSpinner;

    @FXML
    private TextField dateformatRuleTextField;

    @FXML
    private TextField regexpRuleTextField;

    @FXML
    private TextField valueOfRuleTextField;

    @FXML
    private CodeArea groovyRuleTextArea;

    @FXML
    private CheckBox enableNotEmptyRule;

    @FXML
    private CheckBox enableIntegerRule;

    @FXML
    private CheckBox enableDoubleRule;

    @FXML
    private CheckBox enableAlphanumericRule;

    @FXML
    private CheckBox enableMinLengthRule;

    @FXML
    private CheckBox enableMaxLengthRule;

    @FXML
    private CheckBox enableDateRule;

    @FXML
    private CheckBox enableRegexpRule;

    @FXML
    private CheckBox enableValueOfRule;

    @FXML
    private CheckBox enableGroovyRule;


    @Value("${fxml.smartcvs.validation.editor.view}")
    @Override
    public void setFxmlFilePath(String filePath) {
        this.fxmlFilePath = filePath;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        minLengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        maxLengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));

        initCheckBox(notEmptyRuleCheckBox, enableNotEmptyRule);
        initCheckBox(integerRuleCheckBox, enableIntegerRule);
        initCheckBox(doublerRuleCheckBox, enableDoubleRule);
        initCheckBox(alphanumericRuleCheckBox, enableAlphanumericRule);
        initSpinner(minLengthSpinner, enableMinLengthRule);
        initSpinner(maxLengthSpinner, enableMaxLengthRule);
        initTextInputControl(dateformatRuleTextField, enableDateRule);
        initTextInputControl(regexpRuleTextField, enableRegexpRule);
        initTextInputControl(valueOfRuleTextField, enableValueOfRule);
        initCodeAreaControl(groovyRuleTextArea, enableGroovyRule);

        selectedColumn.addListener(observable -> {
            updateForm();
        });
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

        if (enableIntegerRule.isSelected()) {
            validationConfiguration.setIntegerRuleFor(selectedColumn.getValue(), integerRuleCheckBox.isSelected());
        } else {
            validationConfiguration.setIntegerRuleFor(selectedColumn.getValue(), null);
        }

        if (enableNotEmptyRule.isSelected()) {
            validationConfiguration.setNotEmptyRuleFor(selectedColumn.getValue(), notEmptyRuleCheckBox.isSelected());
        } else {
            validationConfiguration.setNotEmptyRuleFor(selectedColumn.getValue(), null);
        }

        if (enableDoubleRule.isSelected()) {
            validationConfiguration.setDoubleRuleFor(selectedColumn.getValue(), doublerRuleCheckBox.isSelected());
        } else {
            validationConfiguration.setDoubleRuleFor(selectedColumn.getValue(), null);
        }

        if (enableAlphanumericRule.isSelected()) {
            validationConfiguration.setAlphanumericRuleFor(selectedColumn.getValue(), alphanumericRuleCheckBox.isSelected());
        } else {
            validationConfiguration.setAlphanumericRuleFor(selectedColumn.getValue(), null);
        }

        if (enableDateRule.isSelected()) {
            validationConfiguration.setDateRuleFor(selectedColumn.getValue(), dateformatRuleTextField.getText());
        } else {
            validationConfiguration.setDateRuleFor(selectedColumn.getValue(), null);
        }

        if (enableGroovyRule.isSelected()) {
            validationConfiguration.setGroovyRuleFor(selectedColumn.getValue(), groovyRuleTextArea.getText());
        } else {
            validationConfiguration.setGroovyRuleFor(selectedColumn.getValue(), null);
        }

        if (enableMinLengthRule.isSelected()) {
            validationConfiguration.setMinLengthRuleFor(selectedColumn.getValue(), minLengthSpinner.getValue());
        } else {
            validationConfiguration.setMinLengthRuleFor(selectedColumn.getValue(), null);
        }

        if (enableMaxLengthRule.isSelected()) {
            validationConfiguration.setMaxLengthRuleFor(selectedColumn.getValue(), maxLengthSpinner.getValue());
        } else {
            validationConfiguration.setMaxLengthRuleFor(selectedColumn.getValue(), null);
        }

        if (enableRegexpRule.isSelected()) {
            validationConfiguration.setRegexpRuleFor(selectedColumn.getValue(), regexpRuleTextField.getText());
        } else {
            validationConfiguration.setRegexpRuleFor(selectedColumn.getValue(), null);
        }

        if (enableValueOfRule.isSelected()) {
            validationConfiguration.setValueOfRuleFor(selectedColumn.getValue(), asList(valueOfRuleTextField.getText().split(", ")));
        } else {
            validationConfiguration.setValueOfRuleFor(selectedColumn.getValue(), null);
        }
    }

    private void updateForm() {

        updateCheckBox(
                notEmptyRuleCheckBox,
                validationConfiguration.getNotEmptyRuleFor(getSelectedColumn()),
                enableNotEmptyRule
        );

        updateCheckBox(
                integerRuleCheckBox,
                validationConfiguration.getIntegerRuleFor(getSelectedColumn()),
                enableIntegerRule
        );

        updateCheckBox(
                doublerRuleCheckBox,
                validationConfiguration.getDoubleRuleFor(getSelectedColumn()),
                enableDoubleRule
        );

        updateCheckBox(
                alphanumericRuleCheckBox,
                validationConfiguration.getAlphanumericRuleFor(getSelectedColumn()),
                enableAlphanumericRule
        );

        updateSpinner(
                minLengthSpinner,
                validationConfiguration.getMinLengthRuleFor(getSelectedColumn()),
                enableMinLengthRule
        );

        updateSpinner(
                maxLengthSpinner,
                validationConfiguration.getMaxLengthRuleFor(getSelectedColumn()),
                enableMaxLengthRule
        );

        updateTextInputControl(
                dateformatRuleTextField,
                validationConfiguration.getDateRuleFor(getSelectedColumn()),
                enableDateRule
        );

        updateTextInputControl(
                regexpRuleTextField,
                validationConfiguration.getRegexpRuleFor(getSelectedColumn()),
                enableRegexpRule
        );

        updateTextInputControl(
                valueOfRuleTextField,
                validationConfiguration.getValueOfRuleFor(getSelectedColumn()),
                enableValueOfRule
        );

        updateCodeAreaControl(
                groovyRuleTextArea,
                validationConfiguration.getGroovyRuleFor(getSelectedColumn()),
                enableGroovyRule
        );
    }

    private void updateCheckBox(CheckBox rule, Boolean value, CheckBox ruleEnabled) {
        if (value == null) {
            ruleEnabled.setSelected(false);
        } else {
            rule.setSelected(value);
            ruleEnabled.setSelected(true);
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

    private void initCheckBox(CheckBox rule, CheckBox ruleEnabled) {
        rule.disableProperty().bind(ruleEnabled.selectedProperty().not());
        ruleEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                rule.setSelected(false);
            }
        });
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
