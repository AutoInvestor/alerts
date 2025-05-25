package io.autoinvestor.application;

import io.autoinvestor.exceptions.DuplicatedException;

public class UserAlreadyExists extends DuplicatedException {
    public UserAlreadyExists(String message) {
        super(message);
    }
}
