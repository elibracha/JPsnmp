package Controllers;

import Module.config.Properties;
import Module.config.XMLBinding;
import Module.core.ExecuteTask;
import Module.core.XLSXReader;
import Module.entities.NetworkView;
import Module.entities.NetworkXML;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

public class NetworkController implements Initializable {

    public static ObservableList<NetworkView> nets = FXCollections.observableArrayList();
    private NetworkView selectedNetwork;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RangeSlider rangerSlider;

    @FXML
    private JFXProgressBar scanProgressBar;

    @FXML
    private JFXTreeTableView<NetworkView> networkView;

    @FXML
    private JFXButton btn_addNet, btn_scan, btn_deleteNet;

    @FXML
    private JFXTextField rangeField, community, networkValue1, networkValue2, networkValue3;

    @FXML
    private Label precentLabel;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ExecuteTask.reload.addListener((observable, oldValue, newValue) -> setTableView(false));

        scanProgressBar.progressProperty().addListener((observable, oldValue, newValue) -> {
            precentLabel.setVisible(false);
            if ((newValue).doubleValue() * 100 > 0) {
                if((530 - (newValue.doubleValue() * 446)) < 485)
                    precentLabel.setVisible(true);
                AnchorPane.setRightAnchor(precentLabel, 530 - (newValue.doubleValue() * 446));
                precentLabel.setText(String.format("%.0f", (newValue).doubleValue() * 100) + '%');
            }
            if ((newValue).doubleValue() * 100 == 100) {
                precentLabel.setText("Sending...");
            }
        });
        rangerSlider.setHighValue(255);
        rangerSlider.setLowValue(0);

        networkValue1.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue1));
        networkValue2.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue2));
        networkValue3.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue3));

        JFXTreeTableColumn<NetworkView, String> network = new JFXTreeTableColumn<>("Network");
        network.setPrefWidth(175);
        network.setCellValueFactory(param -> param.getValue().getValue().networkProperty());

        JFXTreeTableColumn<NetworkView, String> range = new JFXTreeTableColumn<>("Range");
        range.setPrefWidth(160);
        range.setCellValueFactory(param -> param.getValue().getValue().rangeProperty());

        JFXTreeTableColumn<NetworkView, String> community = new JFXTreeTableColumn<>("Community");
        community.setPrefWidth(175);
        community.setCellValueFactory(param -> param.getValue().getValue().communityProperty());

        JFXTreeTableColumn<NetworkView, String> printers = new JFXTreeTableColumn<>("Printers");
        printers.setPrefWidth(160);
        printers.setCellValueFactory(param -> param.getValue().getValue().printerProperty());

        final TreeItem<NetworkView> root = new RecursiveTreeItem<>(nets, RecursiveTreeObject::getChildren);
        networkView.setRoot(root);
        networkView.getColumns().setAll(network, range, community, printers);
        networkView.setShowRoot(false);

        networkView.getFocusModel().focusedIndexProperty().addListener((ov, oldValue, newValue) -> {
            if (networkView.getFocusModel().getFocusedItem() != null) {
                btn_deleteNet.setDisable(false);
                selectedNetwork = networkView.getFocusModel().getFocusedItem().getValue();
            }
        });

        if (!Properties.getNetworks().isEmpty()) {
            setTableView(true);
        }

        if (!(new File(XMLBinding.XML_PATH).exists())) {
            setLocalNet();
            AddNewNet();
        } else {
            setTableView(true);
        }

        execute();
        runNetworkChecks();
    }

    @FXML
    private void deleteNetwork() {
        List<NetworkXML> listToRemove = new Vector<>();

        for (NetworkXML currentNet : Properties.getNetworks()) {
            if (currentNet.getNetwork().equals(selectedNetwork.getNetwork())) {
                listToRemove.add(currentNet);
            }
        }
        Properties.getNetworks().removeAll(listToRemove);
        XMLBinding.marshell();
        TreeItem<NetworkView> selectedItem = networkView.getSelectionModel().getSelectedItem();
        networkView.getRoot().getChildren().remove(selectedItem);
        runNetworkChecks();
    }

    @FXML
    private void handleRange() {
        rangeField.setText(String.format("%d-%d", (int) rangerSlider.getLowValue(), (int) rangerSlider.getHighValue()));
    }

    @FXML
    private void validateNetAndCommunity() {
        boolean firstVal = !networkValue1.getText().isEmpty();
        boolean secondVal = !networkValue2.getText().isEmpty();
        boolean thirdVal = !networkValue3.getText().isEmpty();
        boolean forthVal = !community.getText().isEmpty();

        if (firstVal && secondVal && thirdVal && forthVal) {
            btn_addNet.setDisable(false);
        } else {
            btn_addNet.setDisable(true);
        }
    }

    @FXML
    private void AddNewNet() {

        for (NetworkXML network : Properties.getNetworks()) {
            if (networkValue1.getText().concat(".").concat(networkValue2.getText())
                    .concat(".").concat(networkValue3.getText()).equals(network.getNetwork())) {
                networkValue1.setText("");
                networkValue2.setText("");
                networkValue3.setText("");
                return;
            }
        }

        int lowValue = (int) rangerSlider.getLowValue();
        int highValue = (int) rangerSlider.getHighValue();

        StringBuilder networkBuilder = new StringBuilder(50);
        networkBuilder.append(networkValue1.getText()).append('.').append(networkValue2.getText())
                .append('.').append(networkValue3.getText());

        if (!community.getText().trim().isEmpty() && !networkValue1.getText().trim().isEmpty() &&
                !networkValue2.getText().trim().isEmpty() && !networkValue3.getText().trim().isEmpty()) {
            String range = String.format("%d-%d", lowValue, highValue);
            nets.add(new NetworkView(new SimpleStringProperty(networkBuilder.toString()),
                    new SimpleStringProperty(range),
                    new SimpleStringProperty(community.getText()),
                    new SimpleStringProperty("0")));
            List<NetworkXML> allIPS = Properties.getNetworks();
            String[] rangeValues = range.split("-");

            for (int i = Integer.parseInt(rangeValues[0]); i <= Integer.parseInt(rangeValues[1]); i++) {
                allIPS.add((new NetworkXML(networkBuilder.toString(), String.valueOf(i), community.getText(), 0)));
            }

            Properties.setIPS(allIPS);
            community.setText("public");
            runNetworkChecks();
        }
    }

    @FXML
    private void execute() {
        Platform.runLater(() -> {
            ExecuteTask task = new ExecuteTask();

            task.killExecution();

            scanProgressBar.progressProperty().unbind();

            scanProgressBar.progressProperty().bind(task.progressProperty());

            task.setOnRunning(event -> Platform.runLater(() ->
                    System.out.println(String.format("Execute Started At - %s", Calendar.getInstance().getTime()))));
            task.setOnSucceeded(event -> {
                precentLabel.setText("Done");
                Platform.runLater(() -> System.out.println(String.format("Execute Done At - %s", Calendar.getInstance().getTime())));
            });
            for (NetworkXML net : Properties.getNetworks()) {
                net.setPrinter(0);
            }
            setTableView(false);
            new Thread(task).start();
        });
    }

    @FXML
    private void openFileChooser() {
        File file = new FileChooser().showOpenDialog(anchorPane.getScene().getWindow());
        if (file != null) {
            XLSXReader reader = new XLSXReader();
            reader.readFromCSV(file);
            setTableView(true);
            execute();
        }
    }

    private void setTableView(boolean flag) {

        String networkName = "";
        String netCommunity = "";
        int printersFound = 0;
        int start = -1;
        boolean oneRangeNet = true;

        if (flag) {
            if (Properties.getNetworks().size() > 0) {
                int end = -1;
                flag:
                for (NetworkXML net : Properties.getNetworks()) {
                    for (NetworkView view : nets) {
                        if (net.getNetwork().equals(view.getNetwork())) {
                            int lowRange = Integer.valueOf(view.getRange().split("-")[0]);
                            int highRange = Integer.valueOf(view.getRange().split("-")[1]);
                            if (Integer.valueOf(net.getRange()) < lowRange) {
                                view.setRange(String.format("%d-%d", Integer.valueOf(net.getRange()), highRange));
                            } else if (Integer.valueOf(net.getRange()) > highRange) {
                                view.setRange(String.format("%d-%d", lowRange, Integer.valueOf(net.getRange())));
                            }
                            continue flag;
                        }
                    }
                    if (!networkName.equals(net.getNetwork())) {
                        if (!networkName.isEmpty()) {
                            if (start > end) {
                                int temp = start;
                                start = end;
                                end = temp;
                            }
                            addToList(networkName, end, start, netCommunity, printersFound);
                            printersFound = 0;
                            oneRangeNet = true;
                        }

                        start = Integer.valueOf(net.getRange());
                        networkName = net.getNetwork();
                        netCommunity = net.getCommunity();
                        printersFound += net.getPrinters();
                    } else {
                        oneRangeNet = false;
                        printersFound += net.getPrinters();
                        end = Integer.parseInt(net.getRange());
                    }
                }
                if (oneRangeNet)
                    end = start;
                if (start != -1 && end != -1) {
                    if (start > end) {
                        int temp = start;
                        start = end;
                        end = temp;
                    }
                    addToList(networkName, end, start, netCommunity, printersFound);
                }
            }
        } else {
            for (NetworkView net : nets) {
                for (NetworkXML n : Properties.getNetworks()) {
                    if (n.getNetwork().equals(net.getNetwork())) {
                        printersFound = printersFound + n.getPrinters();
                        net.setPrinter(String.valueOf(printersFound));
                    }
                }
                printersFound = 0;
            }
            networkView.refresh();
        }
    }

    private void addToList(String networkName, int end, int start, String netCommunity, int printersFound) {
        nets.add(new NetworkView(new SimpleStringProperty(networkName),
                new SimpleStringProperty(String.format("%d-%d", start, end)),
                new SimpleStringProperty(netCommunity),
                new SimpleStringProperty(String.valueOf(printersFound))));
    }

    private void validateValue(JFXTextField field) {

        if (!field.getText().matches("[0-9]+") || Integer.parseInt(field.getText()) > 256 ||
                Integer.parseInt(field.getText()) < 0) {
            Platform.runLater(field::clear);
        }
        if (field.getText().length() > 3) {
            String s = field.getText().substring(0, 3);
            Platform.runLater(() -> field.setText(s));
        }
    }

    private void runNetworkChecks() {
        if (!Properties.getNetworks().isEmpty()) {
            btn_scan.setDisable(false);
        } else {
            btn_scan.setDisable(true);
        }
    }

    private void setLocalNet() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String[] values = ip.getHostAddress().split("\\.");
            networkValue1.setText(values[0]);
            networkValue2.setText(values[1]);
            networkValue3.setText(values[2]);
            rangerSlider.setHighValue(255);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
