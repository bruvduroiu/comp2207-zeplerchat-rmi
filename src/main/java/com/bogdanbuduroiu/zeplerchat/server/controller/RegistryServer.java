package com.bogdanbuduroiu.zeplerchat.server.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.comms.Request;
import com.bogdanbuduroiu.zeplerchat.common.model.config.Config;

import javax.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */

public class RegistryServer extends Thread {

    List<Integer> registeredPorts = new ArrayList<Integer>();

    private CountDownLatch latchInitialize;
    private final ScheduledExecutorService hostsScheduler = Executors.newSingleThreadScheduledExecutor();
    private Deque<Request> requestQueue;

    private DatagramSocket socket;
    ByteArrayInputStream bais;

    public RegistryServer(CountDownLatch latchInitialize) {
        this.latchInitialize = latchInitialize;
        this.requestQueue = new ArrayDeque<>();
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

            socket = new DatagramSocket(Config.NS_PORT, InetAddress.getLocalHost());

            System.out.println(getClass().getName() + ">>> Ready to receive broadcast packets!");

            while (true) {

                while (!requestQueue.isEmpty())
                    processRequest(requestQueue.pollFirst());

                byte[] recvBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);


                if (latchInitialize.getCount() > 0)
                    latchInitialize.countDown();

                socket.receive(packet);

                bais = new ByteArrayInputStream(packet.getData());

                JsonObject recvJson = Json.createReader(bais).readObject();

                this.requestQueue.addLast(new Request(recvJson, packet.getAddress(), packet.getPort()));

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(Request request) throws IOException {
        String packetType = request.recvJson.getString("packet-type");
        socket = new DatagramSocket();

        byte[] confirmationData;
        DatagramPacket packet;

        if (packetType.equals(Discovery.SERVER_DISCOVERY)) {

            if (registeredPorts.contains(request.port)) {

                JsonObject response = Json.createObjectBuilder()
                        .add("packet-type", Discovery.PORT_ALREADY_REGISTERED)
                        .add("port", request.port)
                        .build();

                confirmationData = response.toString().getBytes();
                packet = new DatagramPacket(confirmationData, confirmationData.length, request.address, request.port);
                socket.send(packet);
            } else {
                JsonObject response = Json.createObjectBuilder()
                        .add("packet-type", Discovery.SERVER_DISCOVERED)
                        .add("port", request.port)
                        .build();

                confirmationData = response.toString().getBytes();

                socket.send(new DatagramPacket(confirmationData, confirmationData.length, request.address, request.port));
            }


        } else if (packetType.equals(Discovery.REFRESH_HOSTS)) {
            broadcastHosts();
        } else if (packetType.equals(Discovery.CONFIRM_BIND)) {
            registeredPorts.add(request.recvJson.getInt("port"));
            System.out.println("Received Bind COnfirm.");
            broadcastHosts();
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
