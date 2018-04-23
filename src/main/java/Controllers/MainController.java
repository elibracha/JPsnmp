package Controllers;

import Lancer.Main;
import Module.config.XMLBinding;
import Module.enums.Sections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

import static Controllers.STController.LINK;


public class MainController implements Initializable {

    @FXML
    private AnchorPane emailSetting, networkSetting, debugSetting;

    @FXML
    private HBox exit, select_settings, select_bugs;

    @FXML
    private GridPane top;

    @FXML
    private Hyperlink tokenLabel;

    @FXML
    private ImageView logoImage, logoTitle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        top.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);

        double imageSize = Screen.getPrimary().getBounds().getHeight() / 100;
        logoImage.setFitHeight(imageSize*2.5);
        logoImage.setFitWidth(imageSize*2.5);

        logoTitle.setFitHeight(imageSize*1.4);
        logoTitle.setFitWidth(imageSize*10);;

        tokenLabel.setText(LINK);
        tokenLabel.setStyle("-fx-font-size:".concat(String.valueOf(imageSize*1.4)).concat(";")
                .concat(" -fx-border-color: transparent; -fx-padding: 5 0 5 0;"));
        tokenLabel.setOnMouseClicked(event -> Main.service.showDocument(LINK));

        select_settings.setOnMouseClicked((MouseEvent e) -> display(Sections.EMAIL_SETTINGS.getSection_number()));
        select_bugs.setOnMouseClicked((MouseEvent e) -> display(Sections.DEBUG.getSection_number()));

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
                debugSetting.setVisible(false);
                break;
            case 1:
                emailSetting.setVisible(false);
                networkSetting.setVisible(true);
                debugSetting.setVisible(false);
                break;
            case 2:
                emailSetting.setVisible(false);
                networkSetting.setVisible(false);
                debugSetting.setVisible(false);
                break;
            case 3:
                emailSetting.setVisible(false);
                networkSetting.setVisible(false);
                debugSetting.setVisible(true);
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

}

