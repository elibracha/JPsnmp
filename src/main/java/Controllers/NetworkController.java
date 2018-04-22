package Controllers;

import Module.config.Properties;
import Module.config.XMLBinding;
import Module.core.ExecuteTask;
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
import javafx.scene.control.TreeItem;
import org.controlsfx.control.RangeSlider;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

public class NetworkController implements Initializable {

    private NetworkView selectedNetwork;
    private ObservableList<NetworkView> nets = FXCollections.observableArrayList();

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

    @SuppressWarnings("unchecked")
	@Override
    public void initialize(URL location, ResourceBundle resources) {

        rangerSlider.setHighValue(255);
        rangerSlider.setLowValue(0);

        networkValue1.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue1));
        networkValue2.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue2));
        networkValue3.textProperty().addListener((ov, oldValue, newValue) -> validateValue(networkValue3));

        JFXTreeTableColumn<NetworkView, String> network = new JFXTreeTableColumn<>("Network");
        network.setPrefWidth(145);
        network.setCellValueFactory(param -> param.getValue().getValue().networkProperty());

        JFXTreeTableColumn<NetworkView, String> range = new JFXTreeTableColumn<>("Range");
        range.setPrefWidth(140);
        range.setCellValueFactory(param -> param.getValue().getValue().rangeProperty());

        JFXTreeTableColumn<NetworkView, String> community = new JFXTreeTableColumn<>("Community");
        community.setPrefWidth(145);
        community.setCellValueFactory(param -> param.getValue().getValue().communityProperty());

        JFXTreeTableColumn<NetworkView, String> printers = new JFXTreeTableColumn<>("Printers");
        printers.setPrefWidth(135);
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
        
        if((new File(XMLBinding.XML_PATH)).exists())
        	setTableView();
        
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
            networkView.getRoot().getChildren().add(
                    new TreeItem<>(new NetworkView(new SimpleStringProperty(networkBuilder.toString()),
                            new SimpleStringProperty(range),
                            new SimpleStringProperty(community.getText()),
                            new SimpleStringProperty("0"))));

            List<NetworkXML> allIPS = Properties.getNetworks();
            String[] rangeValues = range.split("-");

            for (int i = Integer.parseInt(rangeValues[0]); i <= Integer.parseInt(rangeValues[1]); i++) {
                allIPS.add((new NetworkXML(networkBuilder.toString(), String.valueOf(i), community.getText(), 0)));
            }

            Properties.setIPS(allIPS);

            XMLBinding.marshell();

            community.setText("public");

            runNetworkChecks();
        }
    }

    @FXML
    private void execute() {
        Platform.runLater(() -> {
            ExecuteTask task = new ExecuteTask();
            scanProgressBar.progressProperty().unbind();

            scanProgressBar.progressProperty().bind(task.progressProperty());

            task.setOnRunning(event -> Platform.runLater(() -> System.out.println(String.format("Execute Started At - %s", Calendar.getInstance().getTime()))));
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> System.out.println(String.format("Execute Done At - %s", Calendar.getInstance().getTime())));
                XMLBinding.marshell();;
                setTableView();
            });
            new Thread(task).start();
        });
    }

    private void setTableView() {

        String networkName = "";
        String netCommunity = "";
        int printersFound = 0;
        int start = -1;

        if (Properties.getNetworks().size() > 0) {
            int end = -1;
            for (NetworkXML net : Properties.getNetworks()) {
                if (!networkName.equals(net.getNetwork())) {
                    if (!networkName.isEmpty() && end != -1) {
                        nets.add(new NetworkView(new SimpleStringProperty(networkName),
                                new SimpleStringProperty(String.format("%d-%d", start, end)),
                                new SimpleStringProperty(netCommunity),
                                new SimpleStringProperty(String.valueOf(printersFound))));
                        printersFound = 0;
                    }

                    networkName = net.getNetwork();
                    start = Integer.parseInt(net.getRange());
                    netCommunity = net.getCommunity();
                    printersFound += net.getPrinters();
                } else {
                    printersFound += net.getPrinters();
                    end = Integer.parseInt(net.getRange());
                }
            }
            nets.add(new NetworkView(new SimpleStringProperty(networkName),
                    new SimpleStringProperty(String.format("%d-%d", start, end)),
                    new SimpleStringProperty(netCommunity),
                    new SimpleStringProperty(String.valueOf(printersFound))));
        }

        final TreeItem<NetworkView> root = new RecursiveTreeItem<>(nets, RecursiveTreeObject::getChildren);
        networkView.setRoot(null);
        networkView.setRoot(root);
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

}
