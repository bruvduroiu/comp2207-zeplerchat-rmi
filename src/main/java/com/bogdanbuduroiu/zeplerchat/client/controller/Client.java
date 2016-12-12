package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.Notification;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSink;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSource;
import com.bogdanbuduroiu.zeplerchat.server.controller.RegistryServer;

import javax.json.Json;
import java.io.IOException;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Client {

    String myUsername;
    int myPort;

    private NotificationSink inbox;
    private NotificationSource outbox;

    private CountDownLatch initializeLatch;

    public Client() throws ExecutionException, InterruptedException, IOException, NotBoundException {
        initializeClient();
        bindSource();
        inbox = new NotificationSink(myPort);
        new Thread(inbox).start();
    }

    private void initializeClient() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(new RegistryDiscoveryWorker());


        int port = future.get();
        if (port == 0) {
            initializeLatch = new CountDownLatch(1);
            RegistryServer registryServer = new RegistryServer(initializeLatch);

            registryServer.start();

            executor = Executors.newSingleThreadExecutor();

            initializeLatch.await();

            future = executor.submit(new RegistryDiscoveryWorker());

            myPort = future.get();
        }
        else {
            myPort = port;
        }
        executor.shutdown();

    }

    public void send(Notification notification) {
        try {
            outbox.broadcastNotification(notification);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindSource() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet;
        outbox = new NotificationSource();

        Registry registry = LocateRegistry.createRegistry(myPort);
        registry.rebind("source", outbox);

        byte[] sendData = Json.createObjectBuilder()
                .add("packet-type", Discovery.CONFIRM_BIND)
                .add("port", myPort)
                .build().toString().getBytes();

        packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);
        socket.close();

    }

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            Client client = new Client();

            System.out.print("Enter your username to log in:");
            client.myUsername = in.nextLine();

            String input;
            while (!(input=in.nextLine()).equals("/quit")) {
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
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private void refreshHosts() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet;

        byte[] sendData = Discovery.JSON_REFRESH_HOSTS.toString().getBytes();

        packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);

        socket.close();
    }
}
