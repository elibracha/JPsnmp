package Module.core;

import Module.config.Properties;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.List;


public class SnmpWalk {
    private static String commStr = "public";
    private static String portNum = "161";
    private String targetAddr;

    SnmpWalk(String targetAddr, String commStr) {
        this.targetAddr = targetAddr;
        SnmpWalk.commStr = commStr;
    }

    public static boolean scanNet(String address, String community) throws IOException {
        Address targetAddress = GenericAddress.parse(String.format("udp:%s/%s", address, portNum));
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(3);
        target.setTimeout(Properties.getInstance().getTimeout());
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.addOID(new VariableBinding(new OID(".1.3.6.1.2.1.43")));
        pdu.setType(PDU.GETNEXT);

        ResponseEvent response = snmp.getNext(pdu, target);

        if (response != null) {
            PDU responsePDU = response.getResponse();

            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();

                if (errorStatus == PDU.noError) {
                    System.out.println("Snmp GetNext Response for sysObjectID = " + responsePDU.getVariableBindings());
                    snmp.close();

                    return responsePDU.getVariableBindings().toString().contains("1.3.6.1.2.1.43.");
                }
            }
        }

        snmp.close();

        return false;
    }

    public String execSnmpwalk() throws IOException {

        Address targetAddress = GenericAddress.parse(String.format("udp:%s/%s", targetAddr, portNum));
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(commStr));
        target.setAddress(targetAddress);
        target.setRetries(3);
        target.setTimeout(Properties.getInstance().getTimeout());
        target.setVersion(SnmpConstants.version2c);

        OID oid;

        try {
            oid = new OID(".1");
        } catch (Exception e) {
            System.err.println("Failed to understand the OID or object name.");
            throw e;
        }

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = null;

        if (target.getVersion() == SnmpConstants.version1 || target.getVersion() == SnmpConstants.version2c) {
            target.setVersion(SnmpConstants.version2c);
            events = treeUtils.getSubtree(target, oid);
        }
        if ((events == null || events.isEmpty())) {
            target.setVersion(SnmpConstants.version1);
            events = treeUtils.getSubtree(target, oid);
        }

        if (events == null || events.size() == 0) {
            System.out.println("No result returned.");
        }

        snmp.close();

        StringBuilder buffer = new StringBuilder(50000);

        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.err.println(event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }

            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                buffer.append(varBinding.getOid()).append("|");
                buffer.append(varBinding.getSyntax()).append("|");

                if (varBinding.getVariable().getSyntax() == SMIConstants.SYNTAX_GAUGE32 ||
                        varBinding.getVariable().getSyntax() == SMIConstants.SYNTAX_COUNTER32 ||
                        varBinding.getVariable().getSyntax() == SMIConstants.SYNTAX_COUNTER64 ||
                        varBinding.getVariable().getSyntax() == SMIConstants.SYNTAX_TIMETICKS) {
                    buffer.append(varBinding.getVariable().toLong());
                } else {
                    buffer.append(varBinding.getVariable());
                }

                buffer.append("\r\n");
            }
        }
        return new String(buffer);
    }
}

