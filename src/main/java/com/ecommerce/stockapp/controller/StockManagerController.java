package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.model.*;
import com.ecommerce.stockapp.service.ProductService;
import com.ecommerce.stockapp.service.StockService;

import java.util.List;

public class StockManagerController {
    private final User currentUser;
    private final AuthController auth;
    private final ProductService products;
    private final StockService stock;

    public StockManagerController(User currentUser, AuthController auth, ProductService products, StockService stock) {
        this.currentUser = currentUser;
        this.auth = auth;
        this.products = products;
        this.stock = stock;
    }

    public User currentUser() { return currentUser; }
    public void logout() { auth.logout(); }
    public List<Product> products(String search) { return products.products(search); }
    public List<Category> categories() { return products.categories(); }
    public List<Product> lowStock() { return stock.lowStockAlerts(); }
    public List<StockMovement> history() { return stock.history(); }
    public void saveProduct(Product product) { products.saveProduct(currentUser.getId(), product); }
    public void deleteProduct(Product product) { products.deleteProduct(currentUser.getId(), product); }
    public void addStock(Product product, int quantity) { products.addStock(currentUser.getId(), product, quantity); }
    public void adjustStock(Product product, int quantity) { products.adjustStock(currentUser.getId(), product, quantity); }
}
