package Controllers;

import Module.config.XMLBinding;
import Module.enums.Sections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML
    private AnchorPane emailSetting, networkSetting, debugSetting;

    @FXML
    private HBox exit, select_settings, select_bugs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        select_settings.setOnMouseClicked((MouseEvent e) -> display(Sections.EMAIL_SETTINGS.getSection_number()));
        select_bugs.setOnMouseClicked((MouseEvent e) -> display(Sections.DEBUG.getSection_number()));

        EmailController.jump.addListener((observable, oldValue, newValue) -> display(Sections.NETWORK_SETTINGS.getSection_number()));

        setEffect();

        if(EmailController.flag.getValue())
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
    }

}

