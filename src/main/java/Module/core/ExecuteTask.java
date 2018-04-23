package Module.core;

import Module.config.Properties;
import Module.config.XMLBinding;
import Module.entities.NetworkXML;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipOutputStream;

public class ExecuteTask extends Task<Void> {

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final String LOCALHOST_IP = "Unknown";
    public static String FILE_PATH = "";
    private static Map<String, String> mappedSet = new HashMap<>();

    static {
        try {
            FILE_PATH = String.format("%srecords.zip",
                    ExecuteTask.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private double precent = Properties.getNetworks().size() / Properties.getInstance().getThread_pool_size();

    @Override
    protected Void call() {
        Properties ini = Properties.getInstance();

        File xml = new File(XMLBinding.XML_PATH);

        if (xml.exists())
            XMLBinding.unmarshell();
        else {
            try {
                if (xml.createNewFile())
                    XMLBinding.marshell();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            List<NetworkXML> networks = Properties.getNetworks();

            SnmpWorker[] snmpWorkers = new SnmpWorker[ini.getThread_pool_size()];

            int counter = 0;

            for (NetworkXML net : networks) {
                for (int i = 0; i < snmpWorkers.length; i++) {
                    if (counter == networks.size())
                        break;
                    if (snmpWorkers[i] == null || !snmpWorkers[i].isAlive()) {
                        snmpWorkers[i] = new SnmpWorker(net.getNetwork() + "." + net.getRange(), net.getCommunity());
                        snmpWorkers[i].start();
                        counter++;
                        break;
                    }

                }

                flag:
                while (true)
                    for (SnmpWorker worker : snmpWorkers)
                        if (worker == null || !worker.isAlive()) {
                            break flag;
                        }
            }

            for (SnmpWorker worker : snmpWorkers) {
                worker.join();
            }

            send();

        } catch (Exception e) {
            System.err.println("\n----- An Exception happened as follows. Please confirm the usage etc. -----\n");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void send() {
        File filePath = new File(FILE_PATH);
        SnmpZipMaker zw = new SnmpZipMaker();

        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(filePath))) {
            zw.addToZipFile(mappedSet, zipStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SnmpMailer mailer = new SnmpMailer();

        InetAddress ip;

        try {
            ip = InetAddress.getLocalHost();
            mailer.message(ip.getHostAddress());
        } catch (UnknownHostException e) {
            System.err.println("\n----Couldn't Get Localhost's ip----\n");
            mailer.message(LOCALHOST_IP);
        } finally {
            SnmpZipMaker.removeZip(FILE_PATH);
        }
    }

    public class SnmpWorker extends Thread {

        private String ip;
        private String commStr;

        public SnmpWorker(String IP, String commStr) {
            this.ip = IP;
            this.commStr = commStr;
        }

        @Override
        public void run() {
            try {
                SnmpWalk snmpWalk = new SnmpWalk(ip, commStr);

                InetAddress inet = InetAddress.getByName(ip);
                boolean reachable = inet.isReachable(2000);

                String currentData = null;

                if (reachable) {
                    boolean response = SnmpWalk.scanNet(ip, commStr, "mib-2");
                    if (response) {
                        for (NetworkXML net : Properties.getNetworks()) {
                            if (net.getNetwork().concat(".").concat(net.getRange()).equals(ip)) {
                                net.setPrinter(1);
                                break;
                            }
                        }
                        currentData = snmpWalk.execSnmpwalk();

                        synchronized (this) {
                            if (currentData != null && !currentData.isEmpty())
                                mappedSet.put(ip, currentData);
                        }
                    }
                } else {
                    Thread.currentThread().interrupt();
                }

            } catch (IOException e) {
                System.err.println("Thread crashed....\n" + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LOCK.lock();
                ++precent;
                LOCK.unlock();
                updateProgress(precent, Properties.getNetworks().size());
            }
        }
    }
}
