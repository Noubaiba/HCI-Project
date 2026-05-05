package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.dao.DashboardDao;
import com.ecommerce.stockapp.model.*;
import com.ecommerce.stockapp.service.*;
import jakarta.mail.MessagingException;

import java.util.List;

public class AdminController {
    private final User currentUser;
    private final AuthController auth;
    private final UserService users;
    private final ProductService products;
    private final OrderService orders;
    private final ReportService reports;
    private final DashboardDao dashboard;
    private final ActivityLogDao logs;

    public AdminController(User currentUser, AuthController auth, UserService users, ProductService products,
                           OrderService orders, ReportService reports, DashboardDao dashboard, ActivityLogDao logs) {
        this.currentUser = currentUser;
        this.auth = auth;
        this.users = users;
        this.products = products;
        this.orders = orders;
        this.reports = reports;
        this.dashboard = dashboard;
        this.logs = logs;
    }

    public User currentUser() { return currentUser; }
    public void logout() { auth.logout(); }
    public DashboardStats stats() { return dashboard.stats(); }
    public List<User> users() { return users.users(); }
    public List<Product> products(String search) { return products.products(search); }
    public List<Category> categories() { return products.categories(); }
    public List<Order> orders() { return orders.allOrders(); }
    public List<String> logs() { return logs.recent(); }
    public String report() { return reports.inventoryReport(); }
    public void saveProduct(Product product) { products.saveProduct(currentUser.getId(), product); }
    public void deleteProduct(Product product) { products.deleteProduct(currentUser.getId(), product); }
    public void createCategory(String name) { products.createCategory(currentUser.getId(), name); }
    public void updateUserStatus(User user, UserStatus status) { users.updateStatus(currentUser.getId(), user, status); }
    public void updateRole(User user, Role role) { users.updateRole(currentUser.getId(), user, role); }
    public void updateOrderStatus(Order order, OrderStatus status) { orders.updateOrderStatus(currentUser.getId(), order, status); }

    public String createStockManager(String name, String email) throws MessagingException {
        return users.createStockManager(currentUser.getId(), name, email);
    }
}
