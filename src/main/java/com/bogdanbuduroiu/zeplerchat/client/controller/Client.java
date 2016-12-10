package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.notifs.Notification;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSink;
import com.bogdanbuduroiu.zeplerchat.common.model.notifs.NotificationSource;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
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

        for (Integer port : liveClients) {
            System.out.println(port);
        }


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

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(System.in);

            Client client = new Client();

            String input = in.next();

            Notification notif = new Notification("ardentslayer", input);

            client.send(notif);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
