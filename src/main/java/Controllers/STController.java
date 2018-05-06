package Controllers;

import Lancer.Main;
import Module.config.Properties;
import Module.config.XMLBinding;
import Module.core.QRCode;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class STController implements Initializable {

    public static final String LINK = QRCode.myCodeText;
    public static SimpleBooleanProperty flag = new SimpleBooleanProperty(false);
    @FXML
    private Label SMTPLabel, scanlabel, validLabel;
    @FXML
    private Hyperlink tokenLabel;
    @FXML
    private JFXTextField code;
    @FXML
    private ImageView qrcode, logoImage;
    @FXML
    private GridPane top;

    public static void addTextLimiter(final JFXTextField tf, final int maxLength) {
        tf.textProperty().addListener((ov, oldValue, newValue) -> {
            if (tf.getText().length() > maxLength) {
                String s = tf.getText().substring(0, maxLength);
                tf.setText(s);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addTextLimiter(code, 4);

        Pattern pattern = Pattern.compile("\\d*|\\d+\\,\\d*");
        TextFormatter formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return pattern.matcher(change.getControlNewText()).matches() ? change : null;
        });

        code.setTextFormatter(formatter);
        top.setPrefHeight(Screen.getPrimary().getBounds().getHeight() / 30);

        double imageSize = Screen.getPrimary().getBounds().getHeight() / 100;
        logoImage.setFitHeight(imageSize * 2.5);
        logoImage.setFitWidth(imageSize * 2.5);

        tokenLabel.setText(LINK);
        tokenLabel.setOnMouseClicked(event -> Main.service.showDocument(LINK));

        if (!new File(XMLBinding.XML_PATH).exists()) {
            QRCode.createQRCode();

            Image image = null;
            try {
                image = new Image(new FileInputStream(new File("images/qr.png")));
                qrcode.setImage(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (image == null) {
                qrcode.setVisible(false);
                scanlabel.setVisible(false);
            }
            SMTPLabel.setVisible(false);
        } else {
            scanSMTP();
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


        if (code.getText().length() == 4 && result != null && code.getText().equals(result)) {
            scanSMTP();
        } else if (code.getText().length() == 4) {
            validLabel.setText("Try Again");
        } else {
            validLabel.setText("");
        }
    }

    private void scanSMTP() {
        code.setVisible(false);
        scanlabel.setVisible(false);
        qrcode.setVisible(false);
        SMTPLabel.setVisible(true);
        flag.setValue(true);
        flag.set(true);
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

}
