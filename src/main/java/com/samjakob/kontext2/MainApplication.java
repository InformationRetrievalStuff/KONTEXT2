package com.samjakob.kontext2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.setProperty("prism.lcdtext", "false");

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("ui/MainWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(MainApplication.class.getResource("styles/dark.css").toString());

        stage.setTitle("KONTEXT 2 | University of Surrey");
        stage.setScene(scene);

        stage.setWidth(600);
        stage.setMinWidth(600);
        stage.setHeight(428);
        stage.setMinHeight(428);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}