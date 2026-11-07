package com.juliadrake.studiomanager.service;

/** A booking that cannot be completed; the message is shown to the client. */
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}
