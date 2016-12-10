package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.Notification;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSink;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSource;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Client {

    private Registry clientRegistry;
    private List<Integer> liveClients;
    private NotificationSink inbox;
    private NotificationSource outbox;
    String myUsername;
    int myPort;

    public Client() throws ExecutionException, InterruptedException {
        initializeClient();
        bindInbox();
        outbox = new NotificationSource(liveClients);
    }

    private void initializeClient() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<List<Integer>> future = executor.submit(new RegistryDiscoveryWorker());

        liveClients = future.get();
        myPort = liveClients.get(liveClients.size()-1);

        executor.shutdown();
    }

    public void send(Notification notification) {
        try {
            outbox.sendNotification(notification);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindInbox() {

        try {
            clientRegistry = LocateRegistry.createRegistry(myPort);
            inbox = new NotificationSink();

            clientRegistry.bind("inbox", inbox);

            System.out.println(getClass().getName() + ">>> Bound remote inbox on port: " + myPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    private void refreshHosts() throws IOException {

        DatagramSocket socket = new DatagramSocket();

        byte[] data = Discovery.REFRESH_HOSTS.getBytes();
        byte[] recvBuffer = new byte[15000];
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 8888);
        DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

        socket.send(packet);

        while (true) {
            socket.receive(recvPacket);
            String message = new String(recvPacket.getData()).trim();

            if (message.equals(Discovery.REFRESH_HOSTS))
                continue;

            liveClients = new ArrayList<Integer>();
            for (String port : message.split(","))
                liveClients.add(Integer.parseInt(port));
            outbox = new NotificationSource(liveClients);
            break;
        }
    }

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            Client client = new Client();

            System.out.print("Enter your username to log in:");
            client.myUsername = in.next();

            String input;
            while (!(input=in.next()).equals("/quit")) {
                client.refreshHosts();
                Notification notif = new Notification(client.myUsername, input);
                client.send(notif);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
