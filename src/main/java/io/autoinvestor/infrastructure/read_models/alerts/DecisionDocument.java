package io.autoinvestor.infrastructure.read_models.alerts;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "alerts")
public class DecisionDocument {
    private String userId;
    private String assetId;
    private String type;
    private Date date;

    public DecisionDocument() {}

    public DecisionDocument(String userId, String assetId, String type, Date date) {
        this.userId = userId;
        this.assetId = assetId;
        this.type = type;
        this.date = date;
    }
}
