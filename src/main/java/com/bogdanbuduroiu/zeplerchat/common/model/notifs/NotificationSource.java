package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public class NotificationSource extends UnicastRemoteObject {

    public NotificationSource() throws RemoteException {
    }

    public void sendNotification(Notification notification) throws RemoteException{

    }
}
