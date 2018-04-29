package Module.entities;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;

public class NetworkView extends RecursiveTreeObject<NetworkView> {

    private SimpleStringProperty network;
    private SimpleStringProperty range;
    private SimpleStringProperty community;
    private SimpleStringProperty printer;

    public NetworkView(SimpleStringProperty network, SimpleStringProperty range, SimpleStringProperty community, SimpleStringProperty printer) {
        this.network = network;
        this.range = range;
        this.community = community;
        this.printer = printer;
    }

    public SimpleStringProperty printerProperty() {
        return printer;
    }

    public String getNetwork() {
        return network.get();
    }

    public SimpleStringProperty networkProperty() {
        return network;
    }

    public SimpleStringProperty rangeProperty() {
        return range;
    }

    public SimpleStringProperty communityProperty() {
        return community;
    }

    public String getRange() {
        return range.get();
    }

    public String getCommunity() {
        return community.get();
    }

    public String getPrinter() {
        return printer.get();
    }

    public void setPrinter(String printer) {
        this.printer.set(printer);
    }
}