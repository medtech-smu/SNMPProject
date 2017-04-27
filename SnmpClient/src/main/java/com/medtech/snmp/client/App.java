package com.medtech.snmp.client;

import org.snmp4j.smi.OID;

import java.io.IOException;

public class App {
    private static String address = "udp:127.0.0.1/2001";
    private static SNMPClient manager;
    static final OID agentSystemDescription = new OID(".1.3.6.1.2.1.1.1.0");

    public static void main(String[] args) {
        try {
            manager = new SNMPClient(address);
            System.out.println("SNMP Manager is now running on " + address);
            while (true) {
                try {
                    System.out.println(manager.getAsString(agentSystemDescription));
                } catch (Exception error) {
                    System.err.println("No response");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
