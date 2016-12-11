package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.Notification;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSink;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSource;
import com.bogdanbuduroiu.zeplerchat.server.controller.RegistryServer;

import java.io.IOException;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Client {

    private Registry clientRegistry;

    private List<Integer> liveClients;
    String myUsername;
    int myPort;

    private NotificationSink inbox;
    private NotificationSource outbox;

    private CountDownLatch initializeLatch;

    public Client() throws ExecutionException, InterruptedException, RemoteException {
        initializeClient();
    }

    private void initializeClient() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Integer>> future = executor.submit(new RegistryDiscoveryWorker());
        liveClients = future.get();
        executor.shutdown();

        if (liveClients.isEmpty()) {
            initializeLatch = new CountDownLatch(1);
            RegistryServer registryServer = new RegistryServer(initializeLatch);

            registryServer.start();

            executor = Executors.newSingleThreadExecutor();

            initializeLatch.await();

            future = executor.submit(new RegistryDiscoveryWorker());

            liveClients = future.get();
            executor.shutdown();
        }

    }

    public void send(Notification notification) {
        try {
            outbox.sendNotification(notification);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindSource() throws InterruptedException{

    }

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            Client client = new Client();

            System.out.print("Enter your username to log in:");
            client.myUsername = in.nextLine();

            String input;
            while (!(input=in.nextLine()).equals("/quit")) {
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
