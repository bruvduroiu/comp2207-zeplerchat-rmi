package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;
import com.bogdanbuduroiu.zeplerchat.common.model.config.Config;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

/**
 * Created by bogdanbuduroiu on 12/12/2016.
 */
public class HeartbeatWorker implements Callable<Boolean> {

    int attempts = 3;

    @Override
    public Boolean call() throws Exception {
        ByteArrayInputStream bais;


        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(500);

        JsonObject heartbeatSyn = Json.createObjectBuilder()
                .add("packet-type", Discovery.HEARTBEAT_SYN)
                .add("timestamp", System.currentTimeMillis())
                .build();

        byte[] sendData = heartbeatSyn.toString().getBytes();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), Config.NS_PORT);

        socket.send(packet);

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

                if (packetType.equals(Discovery.HEARTBEAT_ACK)) {
                    socket.close();
                    return true;
                }

            }
            catch (SocketTimeoutException e) {
                attempts--;
            }
        }
        socket.close();
        return false;
    }
}
