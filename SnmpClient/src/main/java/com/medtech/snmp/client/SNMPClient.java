package com.medtech.snmp.client;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SNMPClient {
    private static Snmp snmp;
    private String address;

    public SNMPClient(String address) throws IOException {
        super();
        this.address = address;
        start();
    }

    private static void start() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    public static void stop() throws IOException {
        snmp.close();
    }

    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[]{oid});
        return event.getResponse().get(0).getVariable().toString();
    }

    public ResponseEvent get(OID oids[]) throws IOException {
        ResponseEvent event = snmp.send(getPDU(oids), getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    private PDU getPDU(OID oids[]) {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        return pdu;
    }

    public List<List<String>> getTableAsStrings(OID[] oids) {
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());

        List<TableEvent> events = tUtils.getTable(
                getTarget(),
                oids,
                null,
                null
        );

        List<List<String>> list = new ArrayList<List<String>>();
        for (TableEvent event : events) {
            if(event.isError()) {
                throw new RuntimeException(event.getErrorMessage());
            }
            List<String> strList = new ArrayList<String>();
            list.add(strList);
            for(VariableBinding vb: event.getColumns()) {
                strList.add(vb.getVariable().toString());
            }
        }
        return list;
    }

    public static String extractSingleString(ResponseEvent event) {
        return event.getResponse().get(0).getVariable().toString();
    }
}
