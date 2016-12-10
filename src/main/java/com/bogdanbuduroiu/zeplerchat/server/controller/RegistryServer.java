package com.bogdanbuduroiu.zeplerchat.server.controller;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryServer implements Runnable {

    public void run() {
        try {
            Registry registry = LocateRegistry.createRegistry(8888);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
