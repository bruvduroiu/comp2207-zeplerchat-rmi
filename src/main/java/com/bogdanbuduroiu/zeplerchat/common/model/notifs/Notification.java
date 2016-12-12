package com.bogdanbuduroiu.zeplerchat.common.model.notifs;

import java.io.Serializable;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Notification implements Serializable {

    private final String username;
    private final String message;

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public Notification(String username, String message) {

        this.username = username;
        this.message = message;
    }
}
