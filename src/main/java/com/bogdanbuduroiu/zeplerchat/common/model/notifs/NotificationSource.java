package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.io.Serializable;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSource extends UnicastRemoteObject implements Subscribable, Serializable{

    Map<Notifiable, String> registeredSinks;
    Map<String, Deque<Notification>> queuedMessages;

    public NotificationSource() throws RemoteException {
        registeredSinks = new HashMap<>();
        queuedMessages = new HashMap<>();
    }

    public void broadcastNotification(Notification notification) throws RemoteException{
        Iterator<Notifiable> sinkIterator = registeredSinks.keySet().iterator();

        while (sinkIterator.hasNext()) {
            Notifiable sink = sinkIterator.next();
            try {
                if (!queuedMessages.containsKey(sink.getUsername()))
                    queuedMessages.put(sink.getUsername(), new ArrayDeque<>());
                else {
                }


                sink.sendNotification(notification);
            } catch (ConnectException e) {
                queuedMessages.get(registeredSinks.get(sink)).addLast(notification);
                sinkIterator.remove();
            }
        }

        for (String username : queuedMessages.keySet()) {
            if (!registeredSinks.containsValue(username)) {
                queuedMessages.get(username).addLast(notification);
            }
        }
    }

    @Override
    public boolean subscribe(Notifiable notifiable) throws RemoteException {
        if (registeredSinks.containsKey(notifiable))
            return false;
        registeredSinks.put(notifiable, notifiable.getUsername());

        if (queuedMessages.containsKey(notifiable.getUsername())) {
            Deque<Notification> missedMessages = queuedMessages.get(notifiable.getUsername());
            while (!missedMessages.isEmpty()) {
                notifiable.sendNotification(missedMessages.poll());
            }
        }

        return true;
    }
}
