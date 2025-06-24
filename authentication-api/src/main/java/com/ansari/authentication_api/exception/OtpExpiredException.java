package com.ansari.authentication_api.exception;

public class OtpExpiredException extends Throwable {
    public OtpExpiredException(String message) {
        super(message);
    }
}
