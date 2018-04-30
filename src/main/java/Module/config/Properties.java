package Module.config;

import Module.core.Token;
import Module.entities.NetworkXML;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.List;
import java.util.Vector;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Properties {

    public static final String DEFAULT_HOST = "smtp.prints.email";
    public static final String DEFAULT_USER = "AKIAILLED577TLV3W7PQ";
    public static final String DEFAULT_PASSWORD = "AgiPPhRCcXDypY/RKZITaI8IYBjJ3KNvT6d6nImMi88x";
    public static final int DEFAULT_PORT = 25;
    public static final String VERSION = "1.0.0.0";
    @XmlElementWrapper(name = "IPS")
    @XmlElement(name = "network")
    private static List<NetworkXML> networks = new Vector<>();
    private static Properties configProps;

    static {
        File xml = new File(XMLBinding.XML_PATH);
        if (xml.exists()) {
            XMLBinding.unmarshell();
        }
    }

    private String service = "service@prints.email";
    private int threadPoolSize;
    private String host;
    private int port;
    private int timeout;
    private String password;
    private String username;
    private String token;
    private boolean flag;

    private Properties() {
        this.threadPoolSize = 50;
        this.host = DEFAULT_HOST;
        this.username = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
        this.port = DEFAULT_PORT;
        this.timeout = 4000;
        this.token = (new Token(8)).nextString().toLowerCase();
        this.flag = false;
    }

    public static synchronized Properties getInstance() {
        if (configProps == null)
            configProps = new Properties();
        return configProps;
    }

    public static List<NetworkXML> getNetworks() {
        return networks;
    }

    public static void setIPS(List<NetworkXML> IPS) {
        Properties.networks = IPS;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
