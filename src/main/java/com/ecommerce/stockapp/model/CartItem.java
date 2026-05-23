package com.ecommerce.stockapp.model;

import java.math.BigDecimal;

public class CartItem {
    private int id;
    private int userId;
    private Product product;
    private int quantity;

    public CartItem(int id, int userId, Product product, int quantity) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return product.getPrice().multiply(BigDecimal.valueOf(quantity)); }
}
