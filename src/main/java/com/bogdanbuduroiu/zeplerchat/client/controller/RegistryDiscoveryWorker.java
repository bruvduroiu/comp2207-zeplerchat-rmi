package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryDiscoveryWorker implements Callable<List<Integer>> {

    int attempts = 3;

    public List<Integer> call() throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);

        byte[] sendData = Discovery.SERVER_DISCOVERY.getBytes();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);
        System.out.println(getClass().getName() + ">>> Attempting to register with RegistryServer.");

        while (attempts > 0) {

            byte[] recvBuffer = new byte[15000];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

            try {
                socket.receive(recvPacket);
            }
            catch (SocketTimeoutException e) {
                attempts--;
                continue;
            }

            String message = new String(recvPacket.getData()).trim();

            if (message.equals(Discovery.PORT_ALREADY_REGISTERED)) {

            }

            if (!message.startsWith(Discovery.SERVER_DISCOVERY)) {
                ArrayList<Integer> ports = new ArrayList<Integer>();
                String[] parse = message.split(",");
                for (String port : parse)
                    ports.add(Integer.parseInt(port));

                return ports;
            }
        }
        return new ArrayList<Integer>();
    }
}
