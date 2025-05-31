package io.autoinvestor.infrastructure.repositories.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfolio")
public class PortfolioDocument {
    @Id private String id;
    private String userId;
    private String assetId;
}
