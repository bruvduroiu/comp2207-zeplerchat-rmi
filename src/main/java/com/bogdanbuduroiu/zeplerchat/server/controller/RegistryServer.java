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
import java.util.concurrent.CountDownLatch;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryServer extends Thread {

    Set<Integer> registeredPorts = new HashSet<Integer>();

    private CountDownLatch latchInitialize;
    private CountDownLatch portsLatch;

    public RegistryServer(CountDownLatch latchInitialize, CountDownLatch portsLatch) {
        this.latchInitialize = latchInitialize;
        this.portsLatch = portsLatch;
    }

    @Override
    public void run() {
        DatagramSocket socket;

        try {
            socket = new DatagramSocket(8888, InetAddress.getLocalHost());

            System.out.println(getClass().getName() + ">>> Ready to receive broadcast packets!");

            while (true) {

                byte[] recvBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);

                if (latchInitialize.getCount() > 0)
                    latchInitialize.countDown();

                socket.receive(packet);


                String message = new String(packet.getData()).trim();

                System.out.println(getClass().getName() + ">>> Discovery packet received from: " + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>> Packet received; data: " + message);


                byte[] confirmationData;
                DatagramPacket sendPacket;
                if (message.equals(Discovery.SERVER_DISCOVERY)) {

                    if (registeredPorts.contains(packet.getPort())) {
                        confirmationData = Discovery.PORT_ALREADY_REGISTERED.getBytes();
                        sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                    } else {
                        registeredPorts.add(packet.getPort());

                        if (portsLatch.getCount() > 0)
                            portsLatch.countDown();

                        if (latchInitialize.getCount() != 0)
                            latchInitialize.countDown();

                        String confirmationString = "";
                        for (Integer port : registeredPorts)
                            confirmationString += port.toString() + ",";
                        confirmationData = confirmationString.getBytes();

                        for (Integer port : registeredPorts) {
                            sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), port);
                            socket.send(sendPacket);
                        }
                    }


                } else if (message.equals(Discovery.REFRESH_HOSTS)) {
                    String confirmationString = "";
                    for (Integer port : registeredPorts)
                        confirmationString += port.toString() + ",";
                    confirmationData = confirmationString.getBytes();
                    sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
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
