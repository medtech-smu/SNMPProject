package com.medtech.snmp.agent;

import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;

import java.io.File;
import java.io.IOException;

public class SNMPAgent extends BaseAgent {
    private String address;
    private TransportMapping<? extends Address> transportMapping;

    public SNMPAgent(String address) throws IOException {
        super(
                new File("conf.agent"),
                new File("bootCounter.agent"),
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())
                )
        );
        this.setAddress(address);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void start() throws IOException {
        init();
        addShutdownHook();
        getServer().addContext(new OctetString("public"));
        finishInit();
        run();
        sendColdStartNotification();
    }


    @Override
    protected void registerManagedObjects() {
        // TODO Auto-generated method stub

    }

    public void registerManagedObject(ManagedObject mo) {
        try {
            server.register(mo, null);
        } catch (DuplicateRegistrationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void unregisterManagedObjects() {
        // TODO Auto-generated method stub

    }

    public void unregisterManagedObject(MOGroup moGroup) {
        moGroup.unregisterMOs(server, getContext(moGroup));
    }

    /**
     * User based Security Model, only applicable to
     * SNMP v.3, we are working with SNMPv2c for now.
     */
    @Override
    protected void addUsmUser(USM usm) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addNotificationTargets(
            SnmpTargetMIB targetMIB,
            SnmpNotificationMIB notificationMIB
    ) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addViews(VacmMIB vacmMIB) {
        vacmMIB.addGroup(
                SecurityModel.SECURITY_MODEL_SNMPv2c,
                new OctetString("cpublic"),
                new OctetString("v1v2group"),
                StorageType.nonVolatile
        );

        vacmMIB.addAccess(
                new OctetString("v1v2group"),
                new OctetString("public"),
                SecurityModel.SECURITY_MODEL_ANY,
                SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT,
                new OctetString("fullReadView"),
                new OctetString("fullWriteView"),
                new OctetString("fullNotifyView"),
                StorageType.nonVolatile
        );

        vacmMIB.addViewTreeFamily(
                new OctetString("fullReadView"),
                new OID("1.3"),
                new OctetString(),
                VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile
        );
    }

    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[]{
                new OctetString("public"), // community name
                new OctetString("cpublic"), // security name
                getAgent().getContextEngineID(), // local engine ID
                new OctetString("public"), // default context name
                new OctetString(), // transport tag
                new Integer32(StorageType.nonVolatile), // storage type
                new Integer32(RowStatus.active) // row status
        };
        MOTableRow<?> row = communityMIB.getSnmpCommunityEntry()
                .createRow(
                        new OctetString("public2public").toSubIndex(true),
                        com2sec
                );
        communityMIB.getSnmpCommunityEntry()
                .addRow((SnmpCommunityMIB.SnmpCommunityEntryRow) row);

    }

    @SuppressWarnings("unchecked")
    protected void initTransportMappings() throws IOException {
        transportMappings = new TransportMapping[1];
        Address addr = GenericAddress.parse(address);
        setTransportMapping(
                TransportMappings
                        .getInstance()
                        .createTransportMapping(addr)
        );
        transportMappings[0] = getTransportMapping();
    }

    public TransportMapping<? extends Address> getTransportMapping() {
        return transportMapping;
    }

    public void setTransportMapping(TransportMapping<? extends Address> transportMapping) {
        this.transportMapping = transportMapping;
    }

    @Override
    protected void sendColdStartNotification() {
        this.notificationOriginator.notify(
                new OctetString(), SnmpConstants.coldStart, new VariableBinding[0]);
    }
}
