package com.bogdanbuduroiu.zeplerchat.client.controller;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Client {

    Set<Integer> liveClients;

    public Client() throws ExecutionException, InterruptedException {
        initializeClient();
    }

    private void initializeClient() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Set<Integer>> future = executor.submit(new RegistryDiscoveryWorker());

        liveClients = future.get();

        for (Integer port : liveClients) {
            System.out.println(port);
        }
    }
}
