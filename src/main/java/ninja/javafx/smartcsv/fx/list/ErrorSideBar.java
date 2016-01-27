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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ninja.javafx.smartcsv.fx.table.model.CSVModel;
import ninja.javafx.smartcsv.validation.ValidationError;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static ninja.javafx.smartcsv.fx.util.I18nValidationUtil.getI18nValidatioMessage;

/**
 * clickable side bar with error markers
 */
public class ErrorSideBar extends Pane {

    private static final double WIDTH = 20.0;
    private static final int BLOCKSIZE = 6;

    private ListChangeListener<ValidationError> errorListListener = c -> requestLayout();
    private WeakListChangeListener<ValidationError> weakErrorListListener = new WeakListChangeListener<>(errorListListener);
    private ObjectProperty<CSVModel> model = new SimpleObjectProperty<>();
    private Canvas canvas = new Canvas();
    private ObjectProperty<ValidationError> selectedValidationError = new SimpleObjectProperty<>();
    private PopOver popOver = new PopOver();
    private List<ValidationError> lastPopupErrors = null;

    public ErrorSideBar(ResourceBundle resourceBundle) {
        getChildren().add(canvas);
        setFixWidth();
        configurePopOver();
        addModelListener();
        addMouseClickListener();
        addOnMouseOverListenerForPopOver(resourceBundle);
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

    private void addOnMouseOverListenerForPopOver(ResourceBundle resourceBundle) {
        setOnMouseMoved(event -> showPopOver(event, resourceBundle));
        setOnMouseEntered(event -> showPopOver(event, resourceBundle));
    }

    private void addMouseClickListener() {
        setOnMouseClicked(event -> {
            List<ValidationError> errors = findValidationErrors(event.getY());
            if (!errors.isEmpty()) {
                selectedValidationError.setValue(errors.get(0));
            }
        });
    }

    private void addModelListener() {
        model.addListener((observable, oldValue, newValue) -> {
            newValue.getValidationError().addListener(weakErrorListListener);
            requestLayout();
        });
    }

    private void showPopOver(MouseEvent event, ResourceBundle resourceBundle) {
        List<ValidationError> errors = findValidationErrors(event.getY());
        if (!errors.isEmpty()) {
            if (!areErrorsAlreadyInPopup(errors)) {
                lastPopupErrors = errors;
                popOver.setContentNode(popupContent(getI18nValidatioMessage(resourceBundle, errors)));
                popOver.show(ErrorSideBar.this.getParent(), event.getScreenX() - WIDTH, event.getScreenY());
            }
        } else {
            lastPopupErrors = null;
            popOver.hide(Duration.millis(50));
        }
    }

    private boolean areErrorsAlreadyInPopup(List<ValidationError> errors) {
        return lastPopupErrors != null && lastPopupErrors.size() == errors.size() && lastPopupErrors.containsAll(errors);
    }

    private void configurePopOver() {
        popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_CENTER);
    }

    private void setFixWidth() {
        setMinWidth(WIDTH);
        setPrefWidth(WIDTH);
        setMaxWidth(WIDTH);
    }

    @Override
    protected void layoutChildren() {
        int top = (int) snappedTopInset();
        int right = (int) snappedRightInset();
        int bottom = (int) snappedBottomInset();
        int left = (int) snappedLeftInset();
        int w = (int) getWidth() - left - right;
        int h = (int) getHeight() - top - bottom;
        canvas.setLayoutX(left);
        canvas.setLayoutY(top);
        if (w != canvas.getWidth() || h != canvas.getHeight()) {
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        drawErrorMarker(w, h);
    }

    private List<ValidationError> findValidationErrors(double y) {
        List<ValidationError> errors = new ArrayList<>();
        if (model.get() != null) {
            List<ValidationError> errorList = model.get().getValidationError();
            if (errorList != null && !errorList.isEmpty()) {
                int rows = model.get().getRows().size();
                double space = ((int)canvas.getHeight()) / rows;
                for (ValidationError error : errorList) {
                    double blockStart = space * error.getLineNumber();
                    if (blockStart - 1 <= y && y <= blockStart + BLOCKSIZE + 1) {
                        errors.add(error);
                    }
                }
            }
        }
        return errors;
    }

    private void drawErrorMarker(int w, int h) {

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        g.setFill(Color.valueOf("#ff8888"));

        if (model.get() != null) {
            List<ValidationError> errorList = model.get().getValidationError();
            if (errorList != null && !errorList.isEmpty()) {
                int rows = model.get().getRows().size();
                double space = h / rows;
                for (ValidationError error : errorList) {
                    double blockStart = space * error.getLineNumber();
                    g.fillRect(0, blockStart, w, BLOCKSIZE - 2);
                }
                for (ValidationError error : errorList) {
                    double blockStart = space * error.getLineNumber();
                    g.clearRect(0, blockStart + BLOCKSIZE + 1, w, 1);
                }

            }
        }
    }


    private Node popupContent(String text) {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10,10,10,10));
        vBox.getChildren().add(new Text(text));
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }
}
