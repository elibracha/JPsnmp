package Module.core;

import Module.config.Properties;
import Module.entities.NetworkView;
import javafx.application.Platform;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

import static Controllers.NetworkController.nets;


public class SnmpMailer {

    public void message(String IP) {

        final String username = Properties.getInstance().getUsername();
        final String password = Properties.getInstance().getPassword();

        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", Properties.getInstance().getHost());
        props.put("mail.smtp.port", String.valueOf(Properties.getInstance().getPort()));

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            Multipart multipart = new MimeMultipart();

            message.setFrom(new InternetAddress("service@prints.email"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(Properties.getInstance().getService()));
            message.setSubject("Reciver-" + Properties.getInstance().getToken());

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String messageBody = " {\"version\": \"" + Properties.VERSION + "\", \"localip\" : \"" + IP + "\"" +
                    ", \"token\" : \"" + Properties.getInstance().getToken() + "\"" + ", \"networks\": [";


            int counter = 0;
            for (NetworkView net : nets) {
                messageBody = messageBody + "{\" network\":\"" + net.getNetwork()
                        + "\", \"range\":\"" + net.getRange() + "\",\"community\":\"" + net.getCommunity()
                        + "\",\"printers\":\"" + net.getPrinter() + "\"}";
                if (++counter != nets.size()) {
                    messageBody = messageBody + ",";
                }
            }

            messageBody = messageBody + "]}";

            messageBodyPart.setText(messageBody);

            String file = ExecuteTask.FILE_PATH;

            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.attachFile(file);

            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachPart);

            message.setContent(multipart, "text/plain");
            Transport.send(message);

            System.out.println("Done Email Sent To - " + Properties.getInstance().getService());
            System.out.println("Message Body And Info - " + messageBody);

        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean verify(String port, String host, String user, String pwd) {

        java.util.Properties props = new java.util.Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, null);
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.connect(host, Integer.parseInt(port), user, pwd);
            return true;
        } catch (MessagingException e) {
            Platform.runLater(e::printStackTrace);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    Platform.runLater(e::printStackTrace);
                }
            }
        }
        return false;
    }
}
