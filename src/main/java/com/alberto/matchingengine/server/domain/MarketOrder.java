package com.alberto.matchingengine.server.domain;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class MarketOrder {
    private String type; // "BUY" or "SELL"
    private int quantity;
    private String accountId;
    private Double price; // For resting orders; null for client market orders.
    private long timestamp;

    public MarketOrder() {
        this.timestamp = System.nanoTime();
    }

    // Constructor for orders with a price (resting orders).
    public MarketOrder(String type, int quantity, String accountId, Double price) {
        this.type = type;
        this.quantity = quantity;
        this.accountId = accountId;
        this.price = price;
        this.timestamp = System.nanoTime();
    }

    // Constructor for market orders from the client (price is null).
    public MarketOrder(String type, int quantity, String accountId) {
        this(type, quantity, accountId, null);
    }

}