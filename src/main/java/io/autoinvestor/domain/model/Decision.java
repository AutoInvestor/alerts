package io.autoinvestor.domain.model;

public enum Decision {
    BUY,
    SELL,
    HOLD,
    NONE,
    ;

    public static Decision from(String decision) {
        return switch (decision) {
            case "BUY" -> BUY;
            case "SELL" -> SELL;
            case "HOLD" -> HOLD;
            case "NONE" -> NONE;
            default -> throw new IllegalArgumentException("Unknown decision: " + decision);
        };
    }
}
