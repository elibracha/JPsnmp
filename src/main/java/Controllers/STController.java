package Controllers;

import Lancer.Main;
import Module.config.Properties;
import Module.config.XMLBinding;
import Module.core.QRCode;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class STController implements Initializable {

    public static final String LINK = QRCode.myCodeText;
    public static SimpleBooleanProperty flag = new SimpleBooleanProperty(false);
    @FXML
    private Label SMTPLabel, scanlabel;
    @FXML
    private Hyperlink tokenLabel;
    @FXML
    private JFXTextField code;
    @FXML
    private ImageView qrcode, logoImage, logoTitle;
    @FXML
    private GridPane top;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        top.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);

        double imageSize = Screen.getPrimary().getBounds().getHeight() / 100;
        logoImage.setFitHeight(imageSize*2.5);
        logoImage.setFitWidth(imageSize*2.5);

        logoTitle.setFitHeight(imageSize*1.4);
        logoTitle.setFitWidth(imageSize*10);;

        tokenLabel.setText(LINK);
        tokenLabel.setOnMouseClicked(event -> Main.service.showDocument(LINK));

        if (!new File(XMLBinding.XML_PATH).exists()) {
            QRCode.createQRCode();

            Image image = new Image(getClass().getResourceAsStream("/images/qr.png"));
            qrcode.setImage(image);
            SMTPLabel.setVisible(false);
        }
    }

    @FXML
    private void checkCode() {
       int hash = 0, i, chr;
        String result = null;

        for (i = 0; i < Properties.getInstance().getToken().length() && code.getText().length() == 4; i++) {
            chr = Properties.getInstance().getToken().charAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0;
            result = String.valueOf(Math.abs(hash));
        }

        if (Math.abs(hash) > 1000)
            result = result.substring(0, 4);


        if (result != null && code.getText().equals(result)) {
            code.setVisible(false);
            scanlabel.setVisible(false);
            qrcode.setVisible(false);
            SMTPLabel.setVisible(true);
            flag.setValue(true);
        }


    }

    @FXML
    private void exit() {
        System.exit(0);
    }
}
