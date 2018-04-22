package Module.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class XMLBinding {

    public static final String XML_PATH = "conf/config.xml";

    public static void marshell() {
        try {
            File file = new File(XML_PATH);
            JAXBContext jaxbContext = JAXBContext.newInstance(Properties.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(Properties.getInstance(), file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    public static void unmarshell() {
        try {
            File file = new File(XML_PATH);
            JAXBContext jaxbContext = JAXBContext.newInstance(Properties.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Properties configPropsXML = (Properties) jaxbUnmarshaller.unmarshal(file);
            Properties.getInstance().setHost(configPropsXML.getHost());
            Properties.getInstance().setPort(configPropsXML.getPort());
            Properties.getInstance().setPassword(configPropsXML.getPassword());
            Properties.getInstance().setService(configPropsXML.getService());
            Properties.getInstance().setUsername(configPropsXML.getUsername());
            Properties.getInstance().setThread_pool_size(configPropsXML.getThread_pool_size());
            Properties.getInstance().setTimeout(configPropsXML.getTimeout());
            Properties.getInstance().setToken(configPropsXML.getToken());
            Properties.getInstance().setFlag(configPropsXML.isFlag());
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
}
