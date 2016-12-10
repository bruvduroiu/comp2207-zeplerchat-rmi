package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class NotificationSink extends UnicastRemoteObject implements Notifiable {

    public NotificationSink() throws RemoteException {
    }

    public void sendNotification(Notification notification) throws RemoteException {
        System.out.println(notification.getUsername() + ": " + notification.getMessage());
    }
}
