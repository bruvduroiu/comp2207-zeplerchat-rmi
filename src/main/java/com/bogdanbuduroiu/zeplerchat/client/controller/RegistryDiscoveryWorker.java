package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.Callable;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryDiscoveryWorker implements Callable<Integer> {

    int attempts = 3;

    public Integer call() throws Exception {
        ByteArrayInputStream bais;


        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);

        byte[] sendData = Discovery.JSON_SERVER_DISCOVERY.toString().getBytes();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);
        System.out.println(getClass().getName() + ">>> Attempting to register with RegistryServer.");

        while (attempts > 0) {

            byte[] recvBuffer = new byte[15000];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

            JsonObject response;

            try {
                socket.receive(recvPacket);

                bais = new ByteArrayInputStream(recvPacket.getData());

                response = Json.createReader(bais).readObject();

                bais.close();

                String packetType = response.getString("packet-type");

                if (packetType.equals(Discovery.PORT_ALREADY_REGISTERED)) {

                }
                else if (packetType.equals(Discovery.SERVER_DISCOVERED)) {
                    return response.getInt("port");
                }
            }
            catch (SocketTimeoutException e) {
                attempts--;
            }
        }
        return null;
    }
}
