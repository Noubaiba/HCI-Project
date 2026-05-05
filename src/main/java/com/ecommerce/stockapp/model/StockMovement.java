package com.ecommerce.stockapp.model;

import java.time.LocalDateTime;

public class StockMovement {
    private int id;
    private String productName;
    private StockMovementType type;
    private int quantity;
    private LocalDateTime date;

    public StockMovement(int id, String productName, StockMovementType type, int quantity, LocalDateTime date) {
        this.id = id;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
    }

    public int getId() { return id; }
    public String getProductName() { return productName; }
    public StockMovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
}
