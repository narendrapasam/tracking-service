package com.app.tracking.exception;
public class TrackingException extends RuntimeException {

	public TrackingException() {
        super();
    }

    public TrackingException(String message) {
        super(message);
    }

    public TrackingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackingException(Throwable cause) {
        super(cause);
    }
}
