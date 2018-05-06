package Controllers;

import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class DebugController implements Initializable {

    public static SimpleBooleanProperty setLog = new SimpleBooleanProperty(false);
    private static String LOG_PATH = " log/history.txt";
    private static String ERR_PATH = " log/errors.txt";
    @FXML
    private JFXTextArea logger, errs;

    @FXML
    private BorderPane borderPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> setLogger());
        setLog.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    try {
                        load_logs();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                setLog.setValue(false);
            }
        });
    }

    @FXML
    private void exit() {
        setLog.setValue(false);
        borderPane.getScene().getWindow().hide();
    }

    private void load_logs() throws IOException {
        if (!Files.exists(Paths.get(LOG_PATH))) {
            try {
                Files.createFile(Paths.get(LOG_PATH));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        StringBuilder builder = new StringBuilder(4000);

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null && line.length() != 0) {
                line += System.lineSeparator();
                builder.append(line);
            }
            logger.setText(builder.toString());
        }
    }

    private void save_logs(String cha) throws IOException {
        if (!Files.exists(Paths.get(LOG_PATH))) {
            try {
                Files.createFile(Paths.get(LOG_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
            if (cha.equals("\n")) {
                cha.replace("\n", "\r\n");
            }
            writer.write(cha);
        }
    }

    private void save_err(String cha) throws IOException {
        if (!Files.exists(Paths.get(ERR_PATH))) {
            try {
                Files.createFile(Paths.get(ERR_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ERR_PATH, true))) {
            if (cha.equals("\n")) {
                cha.replace("\n", "\r\n");
            }
            writer.write(cha);
        }
    }

    private void setLogger() {
        LOG_PATH = LOG_PATH.substring(1, LOG_PATH.length());
        ERR_PATH = ERR_PATH.substring(1, ERR_PATH.length());

        try {
            load_logs();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                try {
                    save_logs(String.valueOf((char) b));
                    setLog.setValue(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, true));

        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                try {
                    save_err(String.valueOf((char) b));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, true));
    }
}
