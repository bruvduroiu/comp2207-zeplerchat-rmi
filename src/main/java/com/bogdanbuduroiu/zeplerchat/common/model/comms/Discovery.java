package com.bogdanbuduroiu.zeplerchat.common.model.comms;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Discovery {

    public static final String REFRESH_HOSTS = "REFRESH_HOSTS";
    public static final String SERVER_DISCOVERY = "SERVER_DISCOVERY";
    public static final String SERVER_DISCOVERED = "SERVER_DISCOVERED";
    public static final String HOSTS_DATA = "HOSTS_DATA";
    public static final String PORT_ALREADY_REGISTERED = "PORT_ALREADY_REGISTERED";

    public static final JsonObject JSON_SERVER_DISCOVERY = Json.createObjectBuilder().add("packet-type", Discovery.SERVER_DISCOVERY).build();
    public static final JsonObject JSON_REFRESH_HOSTS = Json.createObjectBuilder().add("packet-type", Discovery.REFRESH_HOSTS).build();
    public static final JsonObject JSON_PORT_ALREADY_REGISTERED = Json.createObjectBuilder().add("packet-type", Discovery.PORT_ALREADY_REGISTERED).build();

}
