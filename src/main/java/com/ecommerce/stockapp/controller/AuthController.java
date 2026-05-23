package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.dao.DashboardDao;
import com.ecommerce.stockapp.dao.UserDao;
import com.ecommerce.stockapp.model.Role;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.model.UserStatus;
import com.ecommerce.stockapp.service.*;
import com.ecommerce.stockapp.view.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private final UserDao userDao;

    public AuthController(Stage stage, AuthService authService, UserService userService, ProductService productService,
                          CartService cartService, OrderService orderService, StockService stockService,
                          ReportService reportService, DashboardDao dashboardDao, ActivityLogDao activityLogDao,
                          UserDao userDao) {
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
        this.userDao = userDao;
    }

    public boolean login(String email, String password) {
        try {
            User user = authService.login(email, password);
            PaymentService payementService=new PaymentService();

            switch (user.getRole()) {
                case ADMIN -> set(new AdminDashboardView(
                        new AdminController(user, this, userService, productService, orderService, reportService, dashboardDao, activityLogDao)
                ).render());

                case STOCK_MANAGER -> set(new StockManagerDashboardView(
                        new StockManagerController(user, this, productService, stockService, userService)
                ).render());

                case CUSTOMER -> {
                    showCustomerDashboard(user, payementService);
                }
            }
            return true;

        } catch (RuntimeException e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // --- Autres méthodes (Register, Activate, etc.) ---

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
            Ui.info("Account activated", "Your stock manager account is active.");
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
        VBox body = Ui.card(Ui.title("Set your password"), Ui.subtitle("Secure activation."), tokenField, password, activate);
        dialog.getDialogPane().setContent(body);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void logout() {
        set(new AuthView(this).render());
    }

    public void showLoginScreen() {
        set(new AuthView(this, false).render());
    }

    public void showRegisterScreen() {
        set(new AuthView(this, true).render());
    }

    public void continueAsGuest() {
        User guest = new User(
                0,
                "Guest",
                "",
                null,
                Role.CUSTOMER,
                UserStatus.ACTIVE,
                null,
                null,
                null,
                null,
                null
        );
        showCustomerDashboard(guest, null);
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

    private void showCustomerDashboard(User user, PaymentService paymentService) {
        CustomerController controller = new CustomerController(
                user, this, productService, cartService, orderService, userService, null, paymentService
        );

        CustomerDashboardView view = new CustomerDashboardView(controller, null);

        AppShell shell = new AppShell(
                user,
                view.navItems(),
                this::logout,
                controller.isGuest(),
                this::showLoginScreen,
                this::showRegisterScreen
        );

        controller.setAppShell(shell);

        CustomerDashboardView finalView = new CustomerDashboardView(controller, shell);
        set(finalView.render());
    }
}
