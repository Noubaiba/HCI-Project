package com.ecommerce.stockapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int id;
    private int userId;
    private String customerName;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime date;

    public Order(int id, int userId, String customerName, BigDecimal totalPrice, OrderStatus status, LocalDateTime date) {
        this.id = id;
        this.userId = userId;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.status = status;
        this.date = date;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getDate() { return date; }
}
