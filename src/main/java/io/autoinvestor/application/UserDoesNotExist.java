package io.autoinvestor.application;

import io.autoinvestor.exceptions.NotFoundException;

public class UserDoesNotExist extends NotFoundException {
    public UserDoesNotExist(String message) {
        super(message);
    }
}
