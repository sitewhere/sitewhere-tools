package com.sitewhere.converter;

public class JiraAccessException extends Exception {

    private static final long serialVersionUID = 2832551946457762772L;

    public JiraAccessException() {
    }

    public JiraAccessException(String message) {
	super(message);
    }

    public JiraAccessException(Throwable cause) {
	super(cause);
    }

    public JiraAccessException(String message, Throwable cause) {
	super(message, cause);
    }

    public JiraAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }
}