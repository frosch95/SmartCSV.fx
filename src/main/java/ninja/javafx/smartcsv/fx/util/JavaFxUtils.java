/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2021 javafx.ninja <info@javafx.ninja>

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
package ninja.javafx.smartcsv.fx.util;

import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import ninja.javafx.smartcsv.fx.SmartCSVController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class helps extract useful messages from exceptions
 * TODO: make I18n maybe?
 */
public final class JavaFxUtils {

    private static final Logger logger = LogManager.getLogger(SmartCSVController.class);

    private JavaFxUtils() {
    }

    public static void onServiceError(WorkerStateEvent event, String windowTitle, String errorHeader) {
        onServiceError(event, windowTitle, errorHeader, () -> {
        });
    }

    public static void onServiceError(WorkerStateEvent event, String windowTitle, String errorHeader, Runnable rollbackAction) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(windowTitle);
        alert.setHeaderText(errorHeader);
        alert.setContentText(findExceptionMessage(event.getSource()));
        alert.showAndWait();
    }

    private static String findExceptionMessage(Worker<?> source) {
        if (source == null) {
            return "Cannot identify the source of the event!";
        } else if (source.getException() == null) {
            return "The event did not have an exception?";
        }
        Throwable exception = source.getException();
        logger.error("Error in " + source.getClass().getSimpleName(), exception);
        if (exception.getMessage() == null) {
            return "Exception of type " + exception.getClass() + " had no message, check the logs.";
        }
        return exception.getMessage();
    }

}
