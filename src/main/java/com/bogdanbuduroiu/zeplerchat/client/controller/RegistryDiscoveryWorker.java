package com.bogdanbuduroiu.zeplerchat.client.controller;

import com.bogdanbuduroiu.zeplerchat.common.model.comms.Discovery;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class RegistryDiscoveryWorker implements Callable<List<Integer>> {

    int attempts = 3;

    public List<Integer> call() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        ObjectInputStream ois;
        ByteArrayInputStream bais;


        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);

        oos.writeObject(Discovery.JSON_SERVER_DISCOVERY);
        oos.flush();
        byte[] sendData = baos.toByteArray();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 8888);

        socket.send(packet);
        System.out.println(getClass().getName() + ">>> Attempting to register with RegistryServer.");

        oos.close();
        baos.close();

        while (attempts > 0) {

            byte[] recvBuffer = new byte[15000];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);

            JsonObject response;

            try {
                socket.receive(recvPacket);

                bais = new ByteArrayInputStream(recvPacket.getData());
                ois = new ObjectInputStream(bais);

                response = (JsonObject) ois.readObject();

                String packetType = response.getString("packet-type");

                if (packetType.equals(Discovery.PORT_ALREADY_REGISTERED)) {

                }
                else if (packetType.equals(Discovery.HOSTS_DATA)) {
                    ArrayList<Integer> ports = new ArrayList<>();
                    String[] parse = response.getString("hosts").split(",");
                    for (String port : parse)
                        ports.add(Integer.parseInt(port));
                }
            }
            catch (SocketTimeoutException e) {
                attempts--;
            }
        }
        return new ArrayList<Integer>();
    }
}
