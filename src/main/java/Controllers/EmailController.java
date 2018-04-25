package Controllers;

import Module.config.Properties;
import Module.config.XMLBinding;
import Module.core.SnmpMailer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailController implements Initializable {

    public static BooleanProperty flag = new SimpleBooleanProperty(Properties.getInstance().isFlag());
    public static BooleanProperty jump = new SimpleBooleanProperty(false);

    @FXML
    private Label labelInfo;

    @FXML
    private JFXTextField emailUser, host, port;

    @FXML
    private CheckBox checkBox;

    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton next;

    @FXML
    private Label loaderSMTP;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        checkBox.setOnMouseClicked(e -> checkBoxHandle());

        port.setText(String.valueOf(Properties.getInstance().getPort()));
        host.setText(Properties.getInstance().getHost());
        emailUser.setText(Properties.getInstance().getUsername());
        password.setText(Properties.getInstance().getPassword());

        password.setOnKeyReleased(e -> checkFieldsAndEnableSections(true));
        emailUser.setOnKeyReleased(e -> checkFieldsAndEnableSections(true));
        host.setOnKeyReleased(e -> checkFieldsAndEnableSections(true));
        port.setOnKeyReleased(e -> checkFieldsAndEnableSections(true));

        EmailController.flag.addListener((observable, oldValue, newValue) -> buildLabel());
        next.setOnMouseClicked((MouseEvent e) -> new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                next.getScene().setCursor(Cursor.WAIT);
                check_smtp();
                next.getScene().setCursor(Cursor.DEFAULT);
                return null;
            }
        }).start());

        buildLabel();
        checkBoxChecks();
        checkFieldsAndEnableSections(false);
    }

    @FXML
    public void exit() {
        setFields();
        XMLBinding.marshell();
        System.exit(0);
    }

    private void buildLabel() {
        if (EmailController.flag.getValue()) {
            labelInfo.setText("For Custom SMTP, Please Check The Box Below And Set Up The Properties.");
        } else {
            labelInfo.setText("*Your SMTP Settings Changed Or Can't Connect, Please Try Again.");
        }
    }

    private void check_smtp() {
        if (!flag.getValue()) {
            loaderSMTP.setTextFill(Paint.valueOf("#000"));
            Platform.runLater(() -> loaderSMTP.setText("Checking SMTP Settings ..."));
            if ((new SnmpMailer()).verify(port.getText(), host.getText(), emailUser.getText(), password.getText())) {
                Platform.runLater(() -> loaderSMTP.setText("Success: Correct SMTP Settings."));
                Properties.getInstance().setFlag(true);
                Platform.runLater(() -> flag.setValue(true));
                jumpSectionNetworks();
            } else {
                Platform.runLater(() -> loaderSMTP.setText("Failed: Incorrect SMTP Settings."));
                loaderSMTP.setTextFill(Paint.valueOf("red"));
                Properties.getInstance().setFlag(false);
                Platform.runLater(() -> flag.setValue(false));
            }
            Platform.runLater(() -> loaderSMTP.setLayoutX(270));
        } else {
            jumpSectionNetworks();
        }
    }

    private boolean checkFieldsAndEnableSections(boolean check) {

        boolean flag;

        if (emailUser.getText() != null &&
                password.getText() != null && host.getText() != null && port.getText() != null) {

            if (!emailUser.getText().isEmpty() && !password.getText().isEmpty() && !host.getText().isEmpty() && !port.getText().isEmpty()) {

                if (check) {
                    Properties.getInstance().setFlag(false);
                    EmailController.flag.setValue(false);
                }

                Pattern VALID_NUMBER_REGEX = Pattern.compile("[0-9]+");
                Matcher matcher = VALID_NUMBER_REGEX.matcher(this.port.getText());
                boolean resultPort = matcher.find();

                if (resultPort) {
                    Properties.getInstance().setPort(Integer.parseInt(this.port.getText()));
                    flag = true;
                } else {
                    flag = false;
                }

                if (flag) {
                    next.setDisable(false);
                    return true;
                }
            } else {
                next.setDisable(true);
                Properties.getInstance().setFlag(false);
                EmailController.flag.setValue(false);
            }
        } else {
            next.setDisable(true);
            Properties.getInstance().setFlag(false);
            EmailController.flag.setValue(false);
        }
        return false;
    }


    private void setFields() {
        Properties.getInstance().setPort(Integer.parseInt(this.port.getText()));
        Properties.getInstance().setHost(this.host.getText());
        Properties.getInstance().setPassword(this.password.getText());
        Properties.getInstance().setUsername(this.emailUser.getText());
    }

    private void checkBoxChecks() {
        if (port.getText().equals(String.valueOf(Properties.DEFAULT_PORT)) &&
                host.getText().equals(Properties.DEFAULT_HOST) && password.getText().equals(Properties.DEFAULT_PASSWORD)
                && emailUser.getText().equals(Properties.DEFAULT_USER)) {
            checkBox.setSelected(false);
            disableSMTP(true);
        } else {
            checkBox.setSelected(true);
        }
    }

    private void jumpSectionNetworks() {
        setFields();
        XMLBinding.marshell();
        jump.setValue(true);
    }

    private void checkBoxHandle() {
        if (!checkBox.isSelected())
            disableSMTP(true);
        else
            disableSMTP(false);
    }

    private void disableSMTP(boolean state) {
        emailUser.setDisable(state);
        port.setDisable(state);
        host.setDisable(state);
        password.setDisable(state);

        if (state) {
            emailUser.setText(Properties.DEFAULT_USER);
            host.setText(Properties.DEFAULT_HOST);
            password.setText(Properties.DEFAULT_PASSWORD);
            port.setText(String.valueOf(Properties.DEFAULT_PORT));
        }
    }
}
