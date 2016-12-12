package com.bogdanbuduroiu.zeplerchat.server.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import javax.json.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */

public class RegistryServer extends Thread {

    List<Integer> registeredPorts = new ArrayList<Integer>();

    private CountDownLatch latchInitialize;
    private final ScheduledExecutorService hostsScheduler = Executors.newSingleThreadScheduledExecutor();

    public RegistryServer(CountDownLatch latchInitialize) {
        this.latchInitialize = latchInitialize;
    }

    @Override
    public void run() {
        DatagramSocket socket;


        try {
            hostsScheduler.scheduleAtFixedRate(
                    () -> broadcastHosts(),
                    5,
                    5,
                    TimeUnit.SECONDS
            );

            socket = new DatagramSocket(8888, InetAddress.getLocalHost());

            System.out.println(getClass().getName() + ">>> Ready to receive broadcast packets!");

            while (true) {

                ByteArrayInputStream bais;
                byte[] recvBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);


                if (latchInitialize.getCount() > 0)
                    latchInitialize.countDown();

                socket.receive(packet);
                bais = new ByteArrayInputStream(packet.getData());

                JsonObject recvJson = Json.createReader(bais).readObject();


                String packetType = recvJson.getString("packet-type");


                byte[] confirmationData;
                DatagramPacket sendPacket;

                if (packetType.equals(Discovery.SERVER_DISCOVERY)) {

                    if (registeredPorts.contains(packet.getPort())) {

                        confirmationData = Discovery.JSON_PORT_ALREADY_REGISTERED.toString().getBytes();
                        sendPacket = new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                    } else {


                        JsonObject response = Json.createObjectBuilder()
                                .add("packet-type", Discovery.SERVER_DISCOVERED)
                                .add("port", packet.getPort())
                                .build();

                        confirmationData = response.toString().getBytes();

                        socket.send(new DatagramPacket(confirmationData, confirmationData.length, packet.getAddress(), packet.getPort()));

                        while (true) {
                            byte[] recvData = new byte[15000];
                            packet = new DatagramPacket(recvData, recvData.length);

                            socket.receive(packet);

                            bais = new ByteArrayInputStream(packet.getData());
                            JsonObject confirm = Json.createReader(bais).readObject();

                            if (confirm.getString("packet-type").equals(Discovery.CONFIRM_BIND)) {
                                registeredPorts.add(confirm.getInt("port"));
                                break;
                            }
                        }

                        broadcastHosts();

                    }


                } else if (packetType.equals(Discovery.REFRESH_HOSTS)) {

                    broadcastHosts();
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastHosts() {
        DatagramSocket socket;
        try {

            socket = new DatagramSocket();
            DatagramPacket packet;

            byte[] sendData = buildHostsData().toString().getBytes();

            for (Integer port : registeredPorts) {
                packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), port);
                socket.send(packet);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject buildHostsData() {

        String confirmationString = "";
        for (Integer port : registeredPorts)
            confirmationString += port.toString() + ",";
        confirmationString = new StringBuilder(confirmationString).deleteCharAt(confirmationString.length() - 1).toString();

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("packet-type", Discovery.HOSTS_DATA)
                .add("hosts", Json.createArrayBuilder().add(confirmationString))
                .build();

        return jsonObject;
    }

}
