package io.autoinvestor.infrastructure.read_models.users;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "inbox")
public class DecisionDocument {
    @Id private String userId;
    private String inboxId;

    public DecisionDocument() {}

    public DecisionDocument(String userId, String inboxId) {
        this.userId = userId;
        this.inboxId = inboxId;
    }
}
