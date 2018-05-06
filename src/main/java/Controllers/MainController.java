package Controllers;

import Lancer.Main;
import Module.config.XMLBinding;
import Module.enums.Sections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static Controllers.STController.LINK;


public class MainController implements Initializable {

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage = new Stage();

    @FXML
    private AnchorPane emailSetting, networkSetting;

    @FXML
    private HBox exit, select_settings, select_bugs;

    @FXML
    private Hyperlink tokenLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/DebugDocument.fxml"));
            setMovable(root, stage);
            stage.initStyle(StageStyle.UNDECORATED);
            Scene sc = new Scene(root);
            stage.setScene(sc);
        }catch (IOException e){
            e.printStackTrace();
        }
        tokenLabel.setText(LINK);
        tokenLabel.setOnMouseClicked(event -> Main.service.showDocument(LINK));

        select_settings.setOnMouseClicked((MouseEvent e) -> display(Sections.EMAIL_SETTINGS.getSection_number()));
        select_bugs.setOnMouseClicked((MouseEvent e) -> {
                stage.show();
                DebugController.setLog.setValue(true);
        });

        EmailController.jump.addListener((observable, oldValue, newValue) -> {
            display(Sections.NETWORK_SETTINGS.getSection_number());
            EmailController.jump.setValue(false);
        });

        setEffect();

        if (EmailController.flag.getValue())
            display(Sections.NETWORK_SETTINGS.getSection_number());
        else
            display(Sections.EMAIL_SETTINGS.getSection_number());
    }

    @FXML
    private void exit() {
        XMLBinding.marshell();
        System.exit(0);
    }

    private void display(int value) {
        switch (value) {
            case 0:
                emailSetting.setVisible(true);
                networkSetting.setVisible(false);
                break;
            case 1:
                emailSetting.setVisible(false);
                networkSetting.setVisible(true);
                break;
            case 2:
                emailSetting.setVisible(false);
                networkSetting.setVisible(false);
                break;
            case 3:
                emailSetting.setVisible(false);
                networkSetting.setVisible(false);
                break;
        }
    }

    private void setEffect() {
        select_settings.setOnMouseEntered(e -> select_settings.setStyle("-fx-background-color: rgba(255,255,255,0.3);"));
        select_settings.setOnMouseExited(e -> select_settings.setStyle("-fx-background-color: rgba(255,255,255,0);"));
        select_bugs.setOnMouseEntered(e -> select_bugs.setStyle("-fx-background-color: rgba(255,255,255,0.3);"));
        select_bugs.setOnMouseExited(e -> select_bugs.setStyle("-fx-background-color: rgba(255,255,255,0);"));
        exit.setOnMouseEntered(e -> exit.setStyle("-fx-background-color: rgba(255,255,255,0.3);"));
        exit.setOnMouseExited(e -> exit.setStyle("-fx-background-color: rgba(255,255,255,0);"));
        select_settings.setPrefWidth(Screen.getPrimary().getBounds().getHeight() / 30);
        select_settings.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);
        select_bugs.setPrefWidth(Screen.getPrimary().getBounds().getHeight() / 30);
        select_bugs.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);
        exit.setPrefWidth(Screen.getPrimary().getBounds().getHeight() / 30);
        exit.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);
    }

    private void setMovable(Parent root, Stage stage) {
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

