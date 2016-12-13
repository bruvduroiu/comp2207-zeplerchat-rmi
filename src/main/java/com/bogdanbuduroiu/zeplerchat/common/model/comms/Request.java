package com.bogdanbuduroiu.zeplerchat.common.model.comms;

import javax.json.JsonObject;
import java.net.InetAddress;

/**
 * Created by bogdanbuduroiu on 12/12/2016.
 */
public class Request {

    public final JsonObject recvJson;
    public final InetAddress address;
    public final int port;

    public Request(JsonObject recvJson, InetAddress address, int port) {
        this.recvJson = recvJson;
        this.address = address;
        this.port = port;
    }
}
