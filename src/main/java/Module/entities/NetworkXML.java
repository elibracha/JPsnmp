package Module.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NetworkXML {

    @javax.xml.bind.annotation.XmlElement(name = "ip")
    private String network;
    private String range;
    private String community;
    private int printer;

    public NetworkXML() {
    }

    public NetworkXML(String network, String range, String community, int printer) {
        this.network = network;
        this.range = range;
        this.community = community;
        this.printer = printer;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public int getPrinters() {
        return printer;
    }

    public int getPrinter() {
        return printer;
    }

    public void setPrinter(int printer) {
        this.printer = printer;
    }

    @Override
    public String toString() {
        return "<NetworkXML{" +
                "network='" + network + '\'' +
                ", range='" + range + '\'' +
                ", community='" + community + '\'' +
                ", printer=" + printer +
                "}>";
    }
}
