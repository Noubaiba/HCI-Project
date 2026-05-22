package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.model.*;
import com.ecommerce.stockapp.service.ProductService;
import com.ecommerce.stockapp.service.StockService;
import com.ecommerce.stockapp.service.UserService;

import java.util.List;

public class StockManagerController {
    private final User currentUser;
    private final AuthController auth;
    private final ProductService products;
    private final StockService stock;
    private final UserService users;

    public StockManagerController(User currentUser, AuthController auth, ProductService products, StockService stock,
                                  UserService users) {
        this.currentUser = currentUser;
        this.auth = auth;
        this.products = products;
        this.stock = stock;
        this.users = users;
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
    public void updateProfile(User user) { users.updateProfile(user); }
    public int changerMotDePasse(String ancienMdp, String nouveauMdp) {
        java.util.Optional<User> userInDb = users.users().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(currentUser.getEmail()))
                .findFirst();
        if (userInDb.isEmpty()) {
            return 0;
        }
        if (!com.ecommerce.stockapp.util.PasswordUtil.verify(ancienMdp, userInDb.get().getPassword())) {
            return -1;
        }
        String hashed = com.ecommerce.stockapp.util.PasswordUtil.hash(nouveauMdp);
        users.updatePassword(userInDb.get().getId(), hashed);
        currentUser.setPassword(hashed);
        return 1;
    }
}
