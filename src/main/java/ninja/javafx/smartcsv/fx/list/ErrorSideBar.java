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

package ninja.javafx.smartcsv.fx.list;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.fx.util.ColorConstants;
import ninja.javafx.smartcsv.validation.ValidationError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.geometry.Pos.CENTER;
import static ninja.javafx.smartcsv.fx.util.ColorConstants.ERROR_COLOR;
import static ninja.javafx.smartcsv.fx.util.ColorConstants.OK_COLOR;
import static ninja.javafx.smartcsv.fx.util.I18nValidationUtil.getI18nValidatioMessage;

/**
 * clickable side bar with error markers
 */
public class ErrorSideBar extends Region {

    private static final Logger logger = LogManager.getLogger(ErrorSideBar.class);

    private static final double WIDTH = 20.0;
    private static final int BORDER = 8;
    private static final double STATUS_BLOCK_HEIGHT = WIDTH - BORDER;
    private static final double STATUS_BLOCK_WIDTH = WIDTH - BORDER;
    private static final double STATUS_BLOCK_OFFSET = WIDTH + BORDER / 2;

    private ListChangeListener<ValidationError> errorListListener = c -> setErrorMarker();
    private WeakListChangeListener<ValidationError> weakErrorListListener = new WeakListChangeListener<>(errorListListener);
    private ObjectProperty<CSVModel> model = new SimpleObjectProperty<>();
    private ObjectProperty<ValidationError> selectedValidationError = new SimpleObjectProperty<>();
    private PopOver popOver = new PopOver();
    private ResourceBundle resourceBundle;
    private Region statusBlock;

    public ErrorSideBar(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        initPopOver();
        setFixWidth();
        addModelListener();

        statusBlock = new Region();
        statusBlock.setPrefSize(STATUS_BLOCK_WIDTH, STATUS_BLOCK_HEIGHT);
        statusBlock.setLayoutY(BORDER / 2);
        statusBlock.setLayoutX(BORDER / 2);
    }

    private void initPopOver() {
        popOver.setAutoHide(true);
        popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
    }

    public void setModel(CSVModel model) {
        this.model.set(model);
    }

    public CSVModel getModel() {
        return model.get();
    }

    public ObjectProperty<CSVModel> modelProperty() {
        return model;
    }

    public ValidationError getSelectedValidationError() {
        return selectedValidationError.get();
    }

    public ObjectProperty<ValidationError> selectedValidationErrorProperty() {
        return selectedValidationError;
    }

    public void setSelectedValidationError(ValidationError selectedValidationError) {
        this.selectedValidationError.set(selectedValidationError);
    }

    private void addModelListener() {
        model.addListener((observable, oldValue, newValue) -> {
            newValue.getValidationError().addListener(weakErrorListListener);
            setErrorMarker();
        });
    }

    private void setFixWidth() {
        setMinWidth(WIDTH);
        setPrefWidth(WIDTH);
        setMaxWidth(WIDTH);
    }

    private void setErrorMarker() {
        List<Region> errorMarkerList = new ArrayList<>();
        errorMarkerList.add(statusBlock);
        statusBlock.setStyle("-fx-background-color: " + OK_COLOR);
        if (model.get() != null) {
            List<ValidationError> errorList = model.get().getValidationError();
            if (errorList != null && !errorList.isEmpty()) {

                statusBlock.setStyle("-fx-background-color: " + ERROR_COLOR);

                int rows = model.get().getRows().size();
                double space = (double)heightWithoutStatusBlock() / rows;
                for (ValidationError error : errorList) {
                    errorMarkerList.add(generateErrorMarker(space, error));
                }
            }
        }
        getChildren().setAll(errorMarkerList);
    }

    private int heightWithoutStatusBlock() {
        return (int)(getHeight() - STATUS_BLOCK_OFFSET);
    }

    private Region generateErrorMarker(double space, ValidationError error) {
        logger.info("generate error marker for {} errors in line {}", error.getMessages().size(), error.getLineNumber());
        logger.info("layout y is set to {}", (space * error.getLineNumber() + STATUS_BLOCK_OFFSET));
        Region errorMarker = new Region();
        errorMarker.setLayoutY(space * error.getLineNumber() + STATUS_BLOCK_OFFSET);
        errorMarker.setPrefSize(WIDTH, 2);
        errorMarker.setStyle("-fx-background-color: " + ERROR_COLOR);
        errorMarker.setOnMouseClicked(event -> selectedValidationError.setValue(error));
        errorMarker.setOnMouseEntered(event -> {
            popOver.setContentNode(popupContent(getI18nValidatioMessage(resourceBundle, error)));
            popOver.show(errorMarker, -16);
        });
        return errorMarker;
    }

    private Node popupContent(String text) {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10,10,10,10));
        vBox.getChildren().add(new Text(text));
        vBox.setAlignment(CENTER);
        return vBox;
    }

}
