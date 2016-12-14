package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.config.Config;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.util.concurrent.Callable;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistrationWorker implements Callable<Integer> {

    public Integer call() throws Exception {
        ByteArrayInputStream bais;


        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(500);

        byte[] sendData = Discovery.JSON_PORT_REGISTRATION_REQUEST.toString().getBytes();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), Config.NS_PORT);

        socket.send(packet);
//        System.out.println(getClass().getName() + ">>> Attempting to register with RegistryServer.");


        byte[] recvBuffer = new byte[15000];
        DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

        JsonObject response;

        socket.receive(recvPacket);

        bais = new ByteArrayInputStream(recvPacket.getData());

        response = Json.createReader(bais).readObject();

        bais.close();

        String packetType = response.getString("packet-type");

        if (packetType.equals(Discovery.PORT_ALREADY_REGISTERED)) {
            return response.getInt("port");
        }
        else if (packetType.equals(Discovery.PORT_REGISTERED)) {
            return response.getInt("port");
        }
        return 0;
    }
}
