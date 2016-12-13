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

    List<Integer> registeredPorts;

    private CountDownLatch latchInitialize;
    private final ScheduledExecutorService hostsScheduler = Executors.newSingleThreadScheduledExecutor();
    private Deque<Request> requestQueue;

    private DatagramSocket socket;
    ByteArrayInputStream bais;

    private CountDownLatch electionLatch;

    public RegistryServer(CountDownLatch latchInitialize) {
        this.latchInitialize = latchInitialize;
        this.electionLatch = new CountDownLatch(1);
        this.requestQueue = new ArrayDeque<>();
        this.registeredPorts = new ArrayList<>();
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
            if (!(e instanceof BindException))
                e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(Request request) throws IOException {
        String packetType = request.recvJson.getString("packet-type");
        System.out.println(getClass().getName() + ">>> Packet processed: " + packetType);

        String responseStr;
        socket = new DatagramSocket();

        byte[] confirmationData;
        DatagramPacket packet;

        if (packetType.equals(Discovery.HEARTBEAT_SYN)) {

            confirmationData = Discovery.JSON_HEARTBEAT_ACK.toString().getBytes();

            System.out.println(getClass().getName() + ">>> Server response: " + Discovery.HEARTBEAT_ACK);

            socket.send(new DatagramPacket(confirmationData, confirmationData.length, request.address, request.port));

        } else if (packetType.equals(Discovery.PORT_REGISTRATION_REQUEST)) {

            if (registeredPorts.contains(request.port)) {

                JsonObject response = Json.createObjectBuilder()
                        .add("packet-type", Discovery.PORT_ALREADY_REGISTERED)
                        .add("port", request.port)
                        .build();

                responseStr = Discovery.PORT_ALREADY_REGISTERED;

                confirmationData = response.toString().getBytes();
                packet = new DatagramPacket(confirmationData, confirmationData.length, request.address, request.port);
                socket.send(packet);
            } else {
                JsonObject response = Json.createObjectBuilder()
                        .add("packet-type", Discovery.PORT_REGISTERED)
                        .add("port", request.port)
                        .build();

                responseStr = Discovery.PORT_REGISTERED;

                confirmationData = response.toString().getBytes();

                socket.send(new DatagramPacket(confirmationData, confirmationData.length, request.address, request.port));
            }
            System.out.println(getClass().getName() + ">>> Server response: " + responseStr);

        } else if (packetType.equals(Discovery.REFRESH_HOSTS)) {
            broadcastHosts();
        } else if (packetType.equals(Discovery.CONFIRM_BIND)) {
            registeredPorts.add(request.recvJson.getInt("port"));
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

        System.out.println(getClass().getName() + ">>> Broadcast Hosts: " + confirmationString);
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("packet-type", Discovery.HOSTS_DATA)
                .add("hosts", Json.createArrayBuilder().add(confirmationString))
                .build();

        return jsonObject;
    }

    private boolean leaderElection() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {

            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isUp() && networkInterface.isLoopback()) {

            }
        }
        return false;
    }
}
