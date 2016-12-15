package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.config.Config;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
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
    private String username;

    public NotificationSink(int myPort, String username) throws RemoteException{
        registeredSources = new ArrayList<>();
        this.username = username;

        this.myPort = myPort;
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public void sendNotification(Notification notification) throws RemoteException {
//        System.out.printf("[%s]: %s", notification.getUsername(), notification.getMessage());
        System.out.println();
        System.out.println("[" + notification.getUsername() + "]: " + notification.getMessage());
    }

    private void registerSink() throws RemoteException, NotBoundException, UnknownHostException {
        Iterator<Integer> portsIter = registeredSources.iterator();
        while (portsIter.hasNext()){
            int port = portsIter.next();
            try {

                Registry registry = LocateRegistry.getRegistry(port);
                Subscribable source = (Subscribable) registry.lookup("source");
                source.subscribe(this, username);
            } catch (java.rmi.ConnectException e) {
                portsIter.remove();
            }
        }
    }

    public void run() {
        try {

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet;

            byte[] request = Discovery.JSON_REFRESH_HOSTS.toString().getBytes();
            packet = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), Config.NS_PORT);

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
                            registeredSources = new ArrayList<>();
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
