package com.example.wedding_gifts.core.domain.exceptions.commun;

public abstract class InvalidValueException extends MyException {

    private static int statusCode = 422;
    private static String message = "Invalid Value";

    public InvalidValueException(String message, String exception, Throwable cause) {
        super(cause, statusCode, exception, message);
    }

    public InvalidValueException(String message, String exception) {
        super(statusCode, exception, message);
    }

    public InvalidValueException(String exception) {
        super(statusCode, exception, message);
    }

    public InvalidValueException() {
        super(statusCode, "InvalidValueException.class", message);
    }

}