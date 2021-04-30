package com.ontimize.jee.common.locator;

import com.ontimize.jee.common.locator.UtilReferenceLocator.Message;

import java.util.List;

/**
 * Internal class for utility messages.
 *
 * @author Imatia Innovation
 */
public class SMessage implements Message {

    public static final int ADD_USER = 0;

    public static final int REMOVE_USER = 1;

    public static final int SIGN_DOWN = 2;

    private String from = null;

    private List users = null;

    private String user = null;

    private int type = -1;

    private long communication = -1;

    public SMessage(String from, int type, String user, long com, List users) {
        this.from = from;
        this.type = type;
        this.user = user;
        this.users = users;
        this.communication = com;
    }

    public String getUser() {
        return this.user;
    }

    @Override
    public List getUsers() {
        return this.users;
    }

    @Override
    public String getMessage() {
        return "STATE MESSAGE";
    }

    @Override
    public String getUserFrom() {
        return this.from;
    }

    public int getType() {
        return this.type;
    }

    @Override
    public long getCommunicationId() {
        return this.communication;
    }

}
