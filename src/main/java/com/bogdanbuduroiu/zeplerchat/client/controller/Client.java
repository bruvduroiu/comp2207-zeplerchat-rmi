package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.config.Config;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.Notification;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSink;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSource;
import com.bogdanbuduroiu.zeplerchat.server.controller.RegistryServer;

import javax.json.Json;
import java.io.IOException;
import java.net.*;
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
    boolean initialized = false;

    private NotificationSink inbox;
    private NotificationSource outbox;

    private CountDownLatch initializeLatch;

    public Client() throws ExecutionException, InterruptedException, IOException, NotBoundException {
        initializeClient();
    }

    private void initializeClient() throws ExecutionException, InterruptedException {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future<Boolean> heartbeatFuture = executor.submit(new HeartbeatWorker());

                        boolean isAlive = heartbeatFuture.get();
                        if (!isAlive) {
                            myPort = 0;
                            initialized = false;
                            initializeLatch = new CountDownLatch(1);
                            RegistryServer registryServer = new RegistryServer(initializeLatch);

                            registryServer.start();

                            executor = Executors.newSingleThreadExecutor();

                            initializeLatch.await();
                        }

                        if (myPort == 0) {
                            Future<Integer> future = executor.submit(new RegistryDiscoveryWorker());
                            myPort = future.get();
                        }
                        if (!initialized) {
                            bindSource();
                            inbox = new NotificationSink(myPort);
                            new Thread(inbox).start();
                            initialized = true;
                        }
                        executor.shutdown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                0,
                10,
                TimeUnit.SECONDS
        );
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

        while(myPort == 0) {}

        Registry registry = LocateRegistry.createRegistry(myPort);
        registry.rebind("source", outbox);

        byte[] sendData = Json.createObjectBuilder()
                .add("packet-type", Discovery.CONFIRM_BIND)
                .add("port", myPort)
                .build().toString().getBytes();

        packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), Config.NS_PORT);

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

        packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), Config.NS_PORT);

        socket.send(packet);

        socket.close();
    }
}
