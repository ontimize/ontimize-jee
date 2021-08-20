package com.ontimize.jee.common.locator;

import com.ontimize.jee.common.locator.UtilReferenceLocator.Message;

import java.util.List;

/**
 * Internal class for utility messages.
 *
 * @author Imatia Innovation
 */
public class BMessage implements Message {

    private String from = null;

    private String message = null;

    private long communication = -1;

    private List users = null;

    public BMessage(String from, String message, long com, List users) {
        this.from = from;
        this.message = message;
        this.communication = com;
        this.users = users;
    }

    public BMessage(String from, String message, long com) {
        this.from = from;
        this.message = message;
        this.communication = com;
    }

    public BMessage(String from, String message) {
        this.from = from;
        this.message = message;
    }

    @Override
    public String getUserFrom() {
        return this.from;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "Message from: '" + this.from + "'\n" + this.message;
    }

    @Override
    public long getCommunicationId() {
        return this.communication;
    }

    @Override
    public List getUsers() {
        return this.users;
    }

}
