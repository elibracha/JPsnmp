package Lancer;

import Controllers.STController;
import Module.config.Properties;
import Module.core.SnmpMailer;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {

    public static HostServices service;

    private double xOffset = 0;
    private double yOffset = 0;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) throws Exception {

        service = getHostServices();

        stage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("FXML/STDocument.fxml"));
        Parent root = loader.load();

        setMoveAble(root, stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        centerInScreen(stage);

        STController.flag.addListener((observable, oldValue, newValue) -> checkSMTP(root, scene, stage).start());

        if (STController.flag.getValue()) {
            checkSMTP(root, scene, stage).start();
        }
    }

    private Thread checkSMTP(Parent root, Scene scene, Stage stage) {
        return new Thread(() -> {
            if (new SnmpMailer().verify(String.valueOf(Properties.getInstance().getPort()), Properties.getInstance().getHost(),
                    Properties.getInstance().getUsername(), Properties.getInstance().getPassword())) {
                Properties.getInstance().setFlag(true);
            } else {
                Properties.getInstance().setFlag(false);
            }
            Platform.runLater(() -> {
                try {
                    setMainDoc(root, scene, stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void setMainDoc(Parent root, Scene scene, Stage stage) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/FXML/MainDocument.fxml"));

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(primScreenBounds.getWidth() / 2.6);
        stage.setHeight(primScreenBounds.getHeight() / 3);
        setMoveAble(root, stage);

        setMoveAble(root, stage);

        scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        centerInScreen(stage);
    }

    private void centerInScreen(Stage stage) {
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void setMoveAble(Parent root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
