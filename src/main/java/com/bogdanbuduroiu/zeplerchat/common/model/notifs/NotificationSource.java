package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSource extends UnicastRemoteObject implements Subscribable, Serializable{

    Set<Notifiable> registeredSinks;

    public NotificationSource() throws RemoteException {
        registeredSinks = new HashSet<>();
    }

    public void broadcastNotification(Notification notification) throws RemoteException{
        for (Notifiable sink : registeredSinks)
            sink.sendNotification(notification);
    }

    @Override
    public boolean subscribe(Notifiable notifiable) throws RemoteException {
        if (registeredSinks.contains(notifiable))
            return false;
        registeredSinks.add(notifiable);
        return true;
    }
}
