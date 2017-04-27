package com.medtech.snmp.agent;

import org.snmp4j.smi.OID;

import java.io.IOException;

public class App {
    private static String address = "0.0.0.0/2001";
    private static SNMPAgent agent;
    static final OID agentSystemDescription = new OID(".1.3.6.1.2.1.1.1.0");

    public static void main(String[] args) {
        try {
            agent = new SNMPAgent(address);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            agent.start();
            System.out.print("SNMP Agent is now running on " + address);
            agent.unregisterManagedObject(agent.getSnmpv2MIB());
            agent.registerManagedObject(
                    MOScalarFactory.createReadOnly(
                            agentSystemDescription, "Response from the agent"
                    )
            );

            while (true) {
                // prevent process from terminating
            }
        } catch (IOException error) {
            // TODO Auto-generated catch block
            error.printStackTrace();
        }
    }
}
