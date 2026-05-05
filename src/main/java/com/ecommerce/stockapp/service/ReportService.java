package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.DashboardDao;
import com.ecommerce.stockapp.dao.ProductDao;
import com.ecommerce.stockapp.model.DashboardStats;
import com.ecommerce.stockapp.model.Product;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class ReportService {
    private final DashboardDao dashboardDao;
    private final ProductDao productDao;

    public ReportService(DashboardDao dashboardDao, ProductDao productDao) {
        this.dashboardDao = dashboardDao;
        this.productDao = productDao;
    }

    public String inventoryReport() {
        DashboardStats stats = dashboardDao.stats();
        String products = productDao.findAll("").stream()
                .map(this::line)
                .collect(Collectors.joining("\n"));
        return """
                PRODUCT & STOCK MANAGEMENT REPORT
                Generated: %s

                Products: %d
                Users: %d
                Orders: %d
                Revenue: %s
                Low stock products: %d

                INVENTORY
                %s
                """.formatted(LocalDateTime.now(), stats.getProducts(), stats.getUsers(), stats.getOrders(), stats.getRevenue(), stats.getLowStock(), products);
    }

    private String line(Product product) {
        return "%-25s | %-14s | Qty: %-5d | Price: %s".formatted(product.getName(), product.getCategoryName(), product.getQuantity(), product.getPrice());
    }
}
