package org.inlm2.common;

import java.io.Serializable;

public class Message implements Serializable{

    private MessageType messageType;
    private String message;

    public Message(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
