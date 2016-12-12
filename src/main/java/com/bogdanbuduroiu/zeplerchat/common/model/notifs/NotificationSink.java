package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSink extends UnicastRemoteObject implements Notifiable, Runnable, Serializable {

    private List<Integer> registeredSources;
    private final ExecutorService hostsListener = Executors.newSingleThreadExecutor();
    private final int myPort;

    public NotificationSink(int myPort) throws RemoteException{
        registeredSources = new ArrayList<>();
        this.myPort = myPort;
    }

    public void sendNotification(Notification notification) throws RemoteException {
        System.out.printf("[%s]: %s", notification.getUsername(), notification.getMessage());
        System.out.println();
    }

    private void registerSink() throws RemoteException, NotBoundException, UnknownHostException {
        for (Integer port : registeredSources) {
            Registry registry = LocateRegistry.getRegistry(port);
            Subscribable source = (Subscribable) registry.lookup("source");
            source.subscribe(this);
        }
    }

    public void run() {
        try {

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet;

            byte[] request = Discovery.JSON_REFRESH_HOSTS.toString().getBytes();
            packet = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 8888);

            socket.send(packet);
            socket.close();

            hostsListener.execute(() -> {

                try {
                    while (true) {
                        byte[] recvBuffer = new byte[15000];

                        final DatagramSocket recvSocket = new DatagramSocket(myPort);
                        final DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

                        recvSocket.receive(recvPacket);

                        final ByteArrayInputStream bais = new ByteArrayInputStream(recvPacket.getData());

                        JsonObject response = Json.createReader(bais).readObject();

                        String packetType = response.getString("packet-type");

                        if (packetType.equals(Discovery.HOSTS_DATA)) {
                            String[] parse = response.getJsonArray("hosts").getJsonString(0).toString().replace("\"", "").split(",");
                            for (String port : parse)
                                registeredSources.add(Integer.parseInt(port));
                            registerSink();
                        }
                        recvSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }

            });

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
