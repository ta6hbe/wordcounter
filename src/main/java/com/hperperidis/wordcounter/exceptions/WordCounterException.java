package com.hperperidis.wordcounter.exceptions;

public class WordCounterException extends RuntimeException {

    public WordCounterException(String message) {
        super(message);
    }

    public WordCounterException(String message, Throwable exception) {
        super(message, exception);
    }
}
