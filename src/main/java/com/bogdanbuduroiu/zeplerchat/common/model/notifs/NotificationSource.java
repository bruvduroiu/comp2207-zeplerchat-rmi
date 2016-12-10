package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class NotificationSource {

    Set<Notifiable> registeredSinks;
    List<Integer> registeredPorts;

    public NotificationSource(List<Integer> registeredPorts) {
        this.registeredPorts = registeredPorts;
        this.registeredSinks = new HashSet<Notifiable>();
        initializeSource();
    }


    private void initializeSource() {
        Registry registry;
        for (Integer port : registeredPorts) {
            try {
                registry = LocateRegistry.getRegistry("localhost", port);
                System.out.println(port);
                Notifiable sink = (Notifiable) registry.lookup("inbox");
                registeredSinks.add(sink);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendNotification(Notification notification) throws RemoteException {
        for (Notifiable sink : registeredSinks)
            sink.sendNotification(notification);
    }
}
