package com.bogdanbuduroiu.zeplerchat.server.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import javax.json.*;

import java.io.*;
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

    public RegistryServer(CountDownLatch latchInitialize) {
        this.latchInitialize = latchInitialize;
    }

    @Override
    public void run() {
        DatagramSocket socket;

        ObjectInputStream ois;
        ByteArrayInputStream bais;

        ObjectOutputStream oos;
        ByteArrayOutputStream baos;

        try {
            socket = new DatagramSocket(8888, InetAddress.getLocalHost());

            System.out.println(getClass().getName() + ">>> Ready to receive broadcast packets!");

            while (true) {

                byte[] recvBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);


                if (latchInitialize.getCount() > 0)
                    latchInitialize.countDown();

                socket.receive(packet);
                bais = new ByteArrayInputStream(packet.getData());
                ois = new ObjectInputStream(bais);

                JsonObject recvJson = (JsonObject) ois.readObject();

                ois.close();
                bais.close();

                String packetType = recvJson.getString("packet-type");

//                System.out.println(getClass().getName() + ">>> Discovery packet received from: " + packet.getAddress().getHostAddress());
//                System.out.println(getClass().getName() + ">>> Packet received; data: " + packetType);


                byte[] confirmationData;
                DatagramPacket sendPacket;

                if (packetType.equals(Discovery.SERVER_DISCOVERY)) {

                    if (registeredPorts.contains(packet.getPort())) {
                        baos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(baos);

                        oos.writeObject(Discovery.JSON_PORT_ALREADY_REGISTERED);
                        oos.flush();

                        confirmationData = baos.toByteArray();
                        sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                        baos.close();
                        oos.close();
                    } else {
                        registeredPorts.add(packet.getPort());

                        JsonObject response = buildHostsData();

                        baos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(baos);

                        oos.writeObject(response);
                        oos.flush();

                        baos.close();
                        oos.close();

                        confirmationData = baos.toByteArray();

                        for (Integer port : registeredPorts) {
                            sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), port);
                            socket.send(sendPacket);
                        }
                    }


                } else if (packetType.equals(Discovery.REFRESH_HOSTS)) {

                    JsonObject response = buildHostsData();

                    baos = new ByteArrayOutputStream();
                    oos = new ObjectOutputStream(baos);

                    oos.writeObject(response);
                    oos.flush();

                    baos.close();
                    oos.close();

                    confirmationData = baos.toByteArray();
                    sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private JsonObject buildHostsData() {

        String confirmationString = "";
        for (Integer port : registeredPorts)
            confirmationString += port.toString() + ",";

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("packet-type", Discovery.HOSTS_DATA)
                .add("hosts", Json.createArrayBuilder().add(confirmationString))
                .build();

        return jsonObject;
    }

}
