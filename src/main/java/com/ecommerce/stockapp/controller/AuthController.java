package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.service.AuthService;
import com.ecommerce.stockapp.service.UserService;
import com.ecommerce.stockapp.dao.DashboardDao;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.service.*;
import com.ecommerce.stockapp.view.*;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List; // ✅ IMPORTANT FIX

public class AuthController {
    private final Stage stage;
    private final AuthService authService;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final StockService stockService;
    private final ReportService reportService;
    private final DashboardDao dashboardDao;
    private final ActivityLogDao activityLogDao;

    public AuthController(Stage stage, AuthService authService, UserService userService, ProductService productService,
                          CartService cartService, OrderService orderService, StockService stockService,
                          ReportService reportService, DashboardDao dashboardDao, ActivityLogDao activityLogDao) {
        this.stage = stage;
        this.authService = authService;
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.stockService = stockService;
        this.reportService = reportService;
        this.dashboardDao = dashboardDao;
        this.activityLogDao = activityLogDao;
    }

    public boolean login(String email, String password) {
        try {
            User user = authService.login(email, password);

            switch (user.getRole()) {
                case ADMIN -> set(new AdminDashboardView(
                        new AdminController(user, this, userService, productService, orderService, reportService, dashboardDao, activityLogDao)
                ).render());

                case STOCK_MANAGER -> set(new StockManagerDashboardView(
                        new StockManagerController(user, this, productService, stockService)
                ).render());

                case CUSTOMER -> {

                    // 1. Controller
                    CustomerController controller = new CustomerController(
                            user, this, productService, cartService, orderService, null
                    );

                    // 2. AppShell avec navItems DIRECTEMENT (FIX IMPORTANT)
                    AppShell shell = new AppShell(
                            user,
                            List.of(
                                    new AppShell.NavItem("/images/icons/catalog.jpeg", "Catalog", () -> {}),
                                    new AppShell.NavItem("/images/icons/cart.png", "Cart", () -> {}),
                                    new AppShell.NavItem("/images/icons/orders.png", "Orders", () -> {}),
                                    new AppShell.NavItem("/images/icons/profil.png", "Profile", () -> {})
                            ),
                            controller::logout
                    );

                    // 3. inject shell
                    controller.setAppShell(shell);

                    // 4. view
                    set(new CustomerDashboardView(controller, shell).render());
                }
            }

            return true;

        } catch (RuntimeException e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    public boolean registerCustomer(String name, String email, String password) {
        try {
            authService.registerCustomer(name, email, password);
            return true;
        } catch (RuntimeException e) {
            System.err.println("Erreur d'inscription : " + e.getMessage());
            return false;
        }
    }

    public void activateStockManager(String token, String password) {
        try {
            authService.activateStockManager(token, password);
            Ui.info("Account activated", "Your stock manager account is active. You can log in now.");
        } catch (RuntimeException e) {
            Ui.error(e);
        }
    }

    public void showActivationDialog(String token) {
        TextField tokenField = Ui.text("Activation token");
        tokenField.setText(token);
        tokenField.setEditable(false);
        PasswordField password = Ui.password("New password");
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Activate stock manager account");
        Button activate = Ui.primary("Activate account");
        activate.setOnAction(e -> {
            activateStockManager(tokenField.getText(), password.getText());
            dialog.close();
        });
        VBox body = Ui.card(Ui.title("Set your password"), Ui.subtitle("This secure activation was opened from the email link."), tokenField, password, activate);
        dialog.getDialogPane().setContent(body);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void logout() {
        set(new AuthView(this).render());
    }

    public boolean isEmailTaken(String email) {
        return userService.existsByEmail(email);
    }

    public void set(javafx.scene.Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1180, 760);
            AppTheme.apply(scene);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }
}  