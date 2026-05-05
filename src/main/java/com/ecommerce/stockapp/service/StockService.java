package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.NotificationDao;
import com.ecommerce.stockapp.dao.ProductDao;
import com.ecommerce.stockapp.dao.StockMovementDao;
import com.ecommerce.stockapp.model.Product;
import com.ecommerce.stockapp.model.StockMovement;

import java.util.List;

public class StockService {
    private final ProductDao products;
    private final StockMovementDao movements;
    private final NotificationDao notifications;

    public StockService(ProductDao products, StockMovementDao movements, NotificationDao notifications) {
        this.products = products;
        this.movements = movements;
        this.notifications = notifications;
    }

    public List<Product> lowStockAlerts() {
        return products.lowStock(5);
    }

    public List<StockMovement> history() {
        return movements.findRecent();
    }

    public void notifyLowStock(int userId) {
        int count = lowStockAlerts().size();
        if (count > 0) {
            notifications.create(userId, count + " product(s) are low or out of stock.");
        }
    }
}
