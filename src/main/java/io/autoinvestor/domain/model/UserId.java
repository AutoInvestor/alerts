package io.autoinvestor.domain.model;

import io.autoinvestor.domain.Id;


public class UserId extends Id {
    UserId(String id) {
        super(id);
    }

    public static UserId from(String id) {
        return new UserId(id);
    }

    public static UserId empty() {
        return new UserId("");
    }
}
