package com.rakutenmobile.messageapi.usermessage.domain.exception;

public class MessageNotOwnedException extends Exception {
    public MessageNotOwnedException(String message) {
        super(message);
    }
}
