package com.ecommerce.stockapp.model;

import java.math.BigDecimal;

public class DashboardStats {
    private final int products;
    private final int users;
    private final int orders;
    private final BigDecimal revenue;
    private final int lowStock;

    public DashboardStats(int products, int users, int orders, BigDecimal revenue, int lowStock) {
        this.products = products;
        this.users = users;
        this.orders = orders;
        this.revenue = revenue;
        this.lowStock = lowStock;
    }

    public int getProducts() { return products; }
    public int getUsers() { return users; }
    public int getOrders() { return orders; }
    public BigDecimal getRevenue() { return revenue; }
    public int getLowStock() { return lowStock; }
}
