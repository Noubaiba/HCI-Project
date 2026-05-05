package com.ecommerce.stockapp.model;
import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int productId;
    private String productName; // Optionnel mais pratique pour l'affichage
    private int quantity;
    private BigDecimal price;

    public OrderItem(int id, int productId, String productName, int quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}