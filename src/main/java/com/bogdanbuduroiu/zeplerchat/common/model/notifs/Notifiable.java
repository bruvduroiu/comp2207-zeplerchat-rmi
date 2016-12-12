package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public interface Notifiable extends Remote {

    void sendNotification(Notification notification) throws RemoteException;
}
