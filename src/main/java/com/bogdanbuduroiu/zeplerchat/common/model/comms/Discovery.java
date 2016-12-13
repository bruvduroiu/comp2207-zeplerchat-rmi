package com.bogdanbuduroiu.zeplerchat.common.model.comms;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */

import javax.json.Json;
import javax.json.JsonObject;

public class Discovery {

    public static final String REFRESH_HOSTS = "REFRESH_HOSTS";
    public static final String PORT_REGISTRATION_REQUEST = "PORT_REGISTRATION_REQUEST";
    public static final String PORT_REGISTERED = "PORT_REGISTERED";
    public static final String HOSTS_DATA = "HOSTS_DATA";
    public static final String PORT_ALREADY_REGISTERED = "PORT_ALREADY_REGISTERED";
    public static final String CONFIRM_BIND = "CONFIRM_BIND";
    public static final String HEARTBEAT_SYN = "HEARTBEAT_SYN";
    public static final String HEARTBEAT_ACK = "HEARTBEAT_ACK";
    public static final String LEADER_ELECTION = "LEADER_ELECTION";

    public static final JsonObject JSON_PORT_REGISTRATION_REQUEST = Json.createObjectBuilder().add("packet-type", Discovery.PORT_REGISTRATION_REQUEST).build();
    public static final JsonObject JSON_REFRESH_HOSTS = Json.createObjectBuilder().add("packet-type", Discovery.REFRESH_HOSTS).build();
    public static final JsonObject JSON_HEARTBEAT_ACK = Json.createObjectBuilder().add("packet-type", Discovery.HEARTBEAT_ACK).build();

}
