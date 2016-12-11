package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSink extends UnicastRemoteObject implements Notifiable, Runnable, Serializable {

    private List<Integer> registeredSources;

    public NotificationSink(List<Integer> registeredSources) throws RemoteException {
        this.registeredSources = registeredSources;
    }

    public void sendNotification(Notification notification) throws RemoteException {
        System.out.println();
    }

    public void run() {

    }
}
