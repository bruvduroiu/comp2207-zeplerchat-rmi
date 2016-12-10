package com.bogdanbuduroiu.zeplerchat.server.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryServer implements Runnable {

    Set<Integer> registeredPorts = new HashSet<Integer>();

    public void run() {
        DatagramSocket socket;

        try {
            socket = new DatagramSocket(8888, InetAddress.getLocalHost());

            System.out.println(getClass().getName() + ">>> Ready to receive broadcast packets!");


            while (true) {
                byte[] recvBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);

                socket.receive(packet);


                String message = new String(packet.getData()).trim();

                System.out.println(getClass().getName() + ">>> Discovery packet received from: " + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>> Packet received; data: " + message);


                if (message.equals(Discovery.SERVER_DISCOVERY)) {

                    byte[] confirmationData;

                    if (registeredPorts.contains(packet.getPort())) {
                        confirmationData = Discovery.PORT_ALREADY_REGISTERED.getBytes();
                    } else {
                        registeredPorts.add(packet.getPort());
                        String confirmationString = "";
                        for (Integer port : registeredPorts)
                            confirmationString += port.toString() + ",";
                        confirmationData = confirmationString.getBytes();
                    }

                    DatagramPacket sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());

                    socket.send(sendPacket);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
