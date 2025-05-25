package io.autoinvestor.domain.model;

import io.autoinvestor.domain.Id;


public class InboxId extends Id {
    InboxId(String id) {
        super(id);
    }

    public static InboxId generate() {
        return new InboxId(generateId());
    }

    public static InboxId from(String id) {
        return new InboxId(id);
    }
}
