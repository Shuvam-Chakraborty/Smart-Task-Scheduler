package com.taskscheduler.exception;

/**
 * Thrown when a user provides a date that is invalid for task scheduling.
 * Examples: past dates, unparseable date strings, null dates.
 */
public class InvalidDateException extends Exception {

    private final String inputValue;

    public InvalidDateException(String message, String inputValue) {
        super(message);
        this.inputValue = inputValue;
    }

    public InvalidDateException(String message, String inputValue, Throwable cause) {
        super(message, cause);
        this.inputValue = inputValue;
    }

    public String getInputValue() { return inputValue; }

    @Override
    public String toString() {
        return String.format("InvalidDateException: %s [input was: \"%s\"]",
            getMessage(), inputValue);
    }
}
