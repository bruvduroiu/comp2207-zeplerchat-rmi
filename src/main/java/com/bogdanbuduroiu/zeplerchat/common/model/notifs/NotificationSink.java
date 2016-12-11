package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSink extends UnicastRemoteObject implements Notifiable, Runnable, Serializable {

    private List<Integer> registeredSources;

    public NotificationSink() throws RemoteException{
        registeredSources = new ArrayList<>();
    }

    public void sendNotification(Notification notification) throws RemoteException {
        System.out.println();
    }

    private void registerSink() throws RemoteException, NotBoundException {
        for (Integer port : registeredSources) {
            Registry registry = LocateRegistry.getRegistry("localhost", port);
            Subscribable source = (Subscribable) registry.lookup("source");
            source.subscribe(this);
        }
    }

    public void run() {
        try {
        ByteArrayInputStream bais;

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet;

        byte[] request = Discovery.JSON_REFRESH_HOSTS.toString().getBytes();
        packet = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);

            while (true) {
                byte[] recvBuffer = new byte[15000];

                packet = new DatagramPacket(recvBuffer, recvBuffer.length);

                socket.receive(packet);

                bais = new ByteArrayInputStream(packet.getData());

                JsonObject response = Json.createReader(bais).readObject();

                String packetType = response.getString("packet-type");

                if (packetType.equals(Discovery.HOSTS_DATA)) {
                    ArrayList<Integer> ports = new ArrayList<>();
                    String[] parse = response.getJsonArray("hosts").getJsonString(0).toString().replace("\"", "").split(",");
                    for (String port : parse)
                        registeredSources.add(Integer.parseInt(port));
                    registerSink();
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
