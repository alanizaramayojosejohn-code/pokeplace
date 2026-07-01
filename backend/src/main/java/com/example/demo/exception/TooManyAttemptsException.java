package com.example.demo.exception;

public class TooManyAttemptsException extends RuntimeException {
    private final long secondsUntilUnlock;

    public TooManyAttemptsException(long secondsUntilUnlock) {
        super("Too many failed attempts. Try again in " + secondsUntilUnlock + " seconds.");
        this.secondsUntilUnlock = secondsUntilUnlock;
    }

    public long getSecondsUntilUnlock() {
        return secondsUntilUnlock;
    }
}
