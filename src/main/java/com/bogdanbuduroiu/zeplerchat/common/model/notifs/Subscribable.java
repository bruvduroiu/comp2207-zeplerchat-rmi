package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by bogdanbuduroiu on 11/12/2016.
 */
public interface Subscribable extends Remote {

    boolean subscribe(Notifiable notifiable, String username) throws RemoteException;
}
