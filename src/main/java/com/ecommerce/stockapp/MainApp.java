package com.ecommerce.stockapp;

import com.ecommerce.stockapp.controller.AuthController;
import com.ecommerce.stockapp.dao.*;
import com.ecommerce.stockapp.service.*;
import com.ecommerce.stockapp.view.AppTheme;
import com.ecommerce.stockapp.view.AuthView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        // 1. Initialisation de la base de données et des DAOs
        Database database = new Database();
        UserDao userDao = new UserDao(database);
        CategoryDao categoryDao = new CategoryDao(database);
        ProductDao productDao = new ProductDao(database);
        OrderDao orderDao = new OrderDao(database);
        CartDao cartDao = new CartDao(database);
        StockMovementDao stockMovementDao = new StockMovementDao(database);
        NotificationDao notificationDao = new NotificationDao(database);
        ActivityLogDao activityLogDao = new ActivityLogDao(database);
        DashboardDao dashboardDao = new DashboardDao(database);

        // 2. Initialisation des Services
        EmailService emailService = new EmailService();
        AuthService authService = new AuthService(userDao, activityLogDao);
        ProductService productService = new ProductService(productDao, categoryDao, stockMovementDao, activityLogDao);
        UserService userService = new UserService(userDao, emailService, activityLogDao);
        CartService cartService = new CartService(cartDao, productDao);
        OrderService orderService = new OrderService(orderDao, cartDao, productDao, stockMovementDao, notificationDao, activityLogDao);
        StockService stockService = new StockService(productDao, stockMovementDao, notificationDao);
        ReportService reportService = new ReportService(dashboardDao, productDao);

        // 3. Initialisation de l'AuthController avec l'injection des dépendances requises
        AuthController authController = new AuthController(
                stage,
                authService,
                userService,
                productService,
                cartService,
                orderService,
                stockService,
                reportService,
                dashboardDao,
                activityLogDao,
                userDao // Inclus selon ta deuxième version
        );

        // 4. Démarrage du service de liens d'activation (écouteur de protocole)
        new ActivationLinkService(authController).start();

        // 5. Configuration de la vue initiale (Login/Auth)
        AuthView view = new AuthView(authController);
        Scene scene = new Scene(view.render(), 1180, 760);

        // Application du thème CSS (ton fichier combiné précédemment)
        AppTheme.apply(scene);

        stage.setTitle("Product & Stock Management System");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}