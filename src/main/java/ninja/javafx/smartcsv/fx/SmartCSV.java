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

package ninja.javafx.smartcsv.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ninja.javafx.smartcsv.fx.about.AboutController;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static javafx.application.Platform.exit;

@Configuration
@ComponentScan("ninja.javafx")
@PropertySource(value = "classpath:/ninja/javafx/smartcsv/fx/application.properties")
public class SmartCSV extends Application {

    private AnnotationConfigApplicationContext appContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        appContext = new AnnotationConfigApplicationContext(SmartCSV.class);
        String name = appContext.getEnvironment().getProperty("application.name");
        String version = appContext.getEnvironment().getProperty("application.version");

        Platform.setImplicitExit(false);

        AboutController aboutController = appContext.getBean(AboutController.class);
        aboutController.setHostServices(getHostServices());

        try {
            showUI(primaryStage, name, version);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void stop() throws Exception {
        if (appContext != null) {
            appContext.close();
        }

        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showUI(Stage primaryStage, String name, String version) {
        SmartCSVController smartCVSController = appContext.getBean(SmartCSVController.class);
        Scene scene = new Scene((Parent) smartCVSController.getView());

        primaryStage.setScene(scene);
        primaryStage.setTitle(String.format("%s %s", name, version));
        primaryStage.getIcons().add(new Image(SmartCSV.class.getResourceAsStream("/ninja/javafx/smartcsv/icon/logo.png")));
        primaryStage.show();
        primaryStage.setMaximized(true);

        primaryStage.setOnCloseRequest(event -> {
            if (!smartCVSController.canExit()) {
                event.consume();
            } else {
                exit();
            }
        });
    }

}
