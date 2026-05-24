package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.AdminController;
import com.ecommerce.stockapp.model.Category;
import com.ecommerce.stockapp.model.DashboardStats;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.OrderStatus;
import com.ecommerce.stockapp.model.Product;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.model.UserStatus;
import jakarta.mail.MessagingException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import com.ecommerce.stockapp.util.IconFactory;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import java.util.Collections;
import java.util.ArrayList;

import com.ecommerce.stockapp.view.Ui;

import javafx.scene.control.PasswordField;

import javafx.scene.Node;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;

public class AdminDashboardView {
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int PAGE_SIZE = 10;
    private final AdminController controller;
    private BorderPane root;
    private VBox navBox;
    private Button activeNavButton;

    private boolean collapsed = false;
    private VBox sidebar;
    private final List<Node> collapsibleNodes = new ArrayList<>();

    private Label logoText;
    private Label sectionTitle;

    public AdminDashboardView(AdminController controller) {
        this.controller = controller;
    }

    public Parent render() {
        root = new BorderPane();
        root.getStyleClass().addAll("app-root", "admin-root");

        root.setLeft(nav());
        showDashboard();

        return root;
    }

    // =========================
    // SIDEBAR ICON
    // =========================
    private VBox hamburgerIcon() {
        VBox bars = new VBox(4);
        bars.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            Region bar = new Region();
            bar.getStyleClass().add("shell-menu-bar");
            bars.getChildren().add(bar);
        }

        return bars;
    }

    // =========================
    // NAVIGATION
    // =========================
    private VBox nav() {
        sidebar = new VBox(20);
        sidebar.getStyleClass().add("admin-sidebar");

        // Mode ouvert par d-faut : padding avec 20px - gauche pour le logo et les boutons
        sidebar.setPadding(new Insets(24, 10, 24, 20));
        sidebar.setFillWidth(true);
        sidebar.setAlignment(Pos.TOP_LEFT);

        sectionTitle = new Label("ADMIN SPACE");
        sectionTitle.getStyleClass().add("admin-sidebar-eyebrow");

        // ===================== HAMBURGER (TOUJOURS CENTR-) =====================
        Button menuButton = new Button();
        menuButton.setGraphic(hamburgerIcon());
        menuButton.getStyleClass().add("hamburger-btn");
        menuButton.setFocusTraversable(false);
        menuButton.setOnAction(e -> toggleSidebar());

        HBox menuPill = new HBox(menuButton);
        menuPill.setId("menuPill");
        menuPill.setAlignment(Pos.CENTER); // Force le hamburger au centre des le depart !
        menuPill.setMaxWidth(Double.MAX_VALUE);

        // ===================== LOGO =====================
        VBox logoBlock = new VBox(10, brandBlock(), sectionTitle);
        logoBlock.setId("logoBlock");
        logoBlock.setAlignment(Pos.TOP_LEFT); // Mode ouvert par defaut : a gauche
        logoBlock.setMaxWidth(Double.MAX_VALUE);

        // ===================== NAVIGATION =====================
        navBox = new VBox(10);
        navBox.setMaxWidth(Double.MAX_VALUE);
        navBox.getChildren().addAll(
                navButton(IconFactory.home(), "Dashboard", this::showDashboard),
                navButton(IconFactory.box(), "Products", this::showProducts),
                navButton(IconFactory.users(), "Users", this::showUsers),
                navButton(IconFactory.ordersIcon(), "Orders", this::showOrders),
                navButton(IconFactory.chart(), "Reports", this::showReports),
                navButton(IconFactory.profileIcon(), "Profile", this::showProfile),
                navButton(IconFactory.log(), "Logs", this::showLogs)
        );

        // Conteneur sup-rieur (Hamburger + Logo)
        VBox top = new VBox(12, menuPill, logoBlock);
        top.setId("topContainer");
        top.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox profile = SidebarProfileFactory.create(
                controller.currentUser(),
                this::showProfile,
                controller::logout
        );
        collapsibleNodes.add(profile);

        sidebar.getChildren().addAll(top, navBox, spacer, profile);

        return sidebar;
    }
    // =========================
    // BRAND BLOCK
    // =========================

    private VBox brandBlock() {
        ImageView logoImage = new ImageView();
        try {
            logoImage.setImage(new Image(
                    getClass().getResource("/images/Stockify.png").toExternalForm()
            ));
        } catch (Exception ignored) {}

        logoImage.setFitWidth(45);
        logoImage.setFitHeight(45);
        logoImage.setPreserveRatio(true);

        logoText = new Label("Stockify");
        logoText.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #111827;");

        if (!collapsibleNodes.contains(logoText)) {
            collapsibleNodes.add(logoText);
        }

        // On cr-e la ligne du logo. Son alignement sera g-r- dynamiquement dans toggleSidebar.
        HBox logoRow = new HBox(12, logoImage, logoText);
        logoRow.setId("logoRow"); // On lui donne un ID pour la retrouver facilement
        logoRow.setAlignment(Pos.CENTER_LEFT); // Par defaut ouvert : a gauche

        VBox brand = new VBox(8, logoRow);
        brand.setId("brandBlock"); // ID pour identification
        brand.setAlignment(Pos.CENTER_LEFT); // Par defaut ouvert : a gauche
        brand.setPadding(new Insets(0, 0, 15, 0));

        return brand;
    }
    // =========================
    // COLLAPSE SIDEBAR
    // =========================
    private void toggleSidebar() {
        collapsed = !collapsed;

        double w = collapsed ? 80 : 280;

        sidebar.setPrefWidth(w);
        sidebar.setMinWidth(w);
        sidebar.setMaxWidth(w);

        // 1. Gestion du Padding global de la sidebar
        if (collapsed) {
            sidebar.setPadding(new Insets(24, 0, 24, 0)); // Pas de decalage a gauche quand c'est ferme
            sidebar.setAlignment(Pos.TOP_CENTER);
        } else {
            sidebar.setPadding(new Insets(24, 10, 24, 20)); // Marge propre a gauche quand c'est ouvert
            sidebar.setAlignment(Pos.TOP_LEFT);
        }

        // Visibilit- des textes
        sectionTitle.setVisible(!collapsed);
        sectionTitle.setManaged(!collapsed);
        logoText.setVisible(!collapsed);
        logoText.setManaged(!collapsed);

        for (Node n : collapsibleNodes) {
            n.setVisible(!collapsed);
            n.setManaged(!collapsed);
        }

        // Alignement des boutons de navigation (Centre si ferm-, Gauche si ouvert)
        navBox.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                if (btn.getGraphic() instanceof HBox hbox) {
                    hbox.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
                }
            }
        });

        // 2. - TRAITEMENT SP-CIFIQUE ET TR-S STRICT POUR LE HAMBURGER ET LE LOGO
        if (sidebar.getChildren().get(0) instanceof VBox topContainer) {

            // A. Le Hamburger : Il reste TOUJOURS centr- au milieu de la largeur disponible
            if (topContainer.getChildren().get(0) instanceof HBox menuPill) {
                menuPill.setAlignment(Pos.CENTER);
            }

            // B. Le Logo et l'Espace Admin : Alignement dynamique selon l'-tat
            if (topContainer.getChildren().get(1) instanceof VBox logoBlock) {
                if (collapsed) {
                    logoBlock.setAlignment(Pos.TOP_CENTER);
                    topContainer.setAlignment(Pos.TOP_CENTER);

                    if (logoBlock.getChildren().get(0) instanceof VBox brandBlock) {
                        brandBlock.setAlignment(Pos.CENTER);
                        if (brandBlock.getChildren().get(0) instanceof HBox logoRow) {
                            logoRow.setAlignment(Pos.CENTER); // L'icone seule se centre parfaitement
                        }
                    }
                } else {

                    logoBlock.setAlignment(Pos.TOP_LEFT);
                    topContainer.setAlignment(Pos.TOP_LEFT);

                    if (logoBlock.getChildren().get(0) instanceof VBox brandBlock) {
                        brandBlock.setAlignment(Pos.CENTER_LEFT);
                        if (brandBlock.getChildren().get(0) instanceof HBox logoRow) {
                            logoRow.setAlignment(Pos.CENTER_LEFT); // Repositionne a gauche
                        }
                    }
                }
            }
        }
    }

    // =========================
    // NAV BUTTON FIX
    // =========================
    private Button navButton(Node icon, String label, Runnable action) {

        Label text = new Label(label);
        text.getStyleClass().add("admin-sidebar-nav-label");

        collapsibleNodes.add(text);

        HBox content = new HBox(12, icon, text);
        content.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(content);

        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);

        // - FIX: needed for selectNav()
        btn.getProperties().put("navText", label);

        btn.setOnAction(e -> {
            if (activeNavButton != null) {
                activeNavButton.getStyleClass().remove("active");
            }

            activeNavButton = btn;
            btn.getStyleClass().add("active");

            action.run();
        });

        return btn;
    }



    private void setActiveNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }

        activeNavButton = button;

        if (!button.getStyleClass().contains("active")) {
            button.getStyleClass().add("active");
        }
    }

    private VBox dashCategoryCard(String name, int productCount, int totalStock, int lowStock, String accent) {
        Label iconLabel = new Label(name.substring(0, 1).toUpperCase());
        iconLabel.getStyleClass().addAll("dash-cat-icon", accent);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("dash-cat-name");
        nameLabel.setWrapText(true);

        Label productsLabel = new Label(productCount + " products");
        productsLabel.getStyleClass().add("dash-cat-sub");

        Label stockLabel = new Label(String.valueOf(totalStock));
        stockLabel.getStyleClass().add("dash-cat-stock");

        Label stockHint = new Label("units in stock");
        stockHint.getStyleClass().add("dash-cat-sub");

        VBox stockBlock = new VBox(2, stockLabel, stockHint);

        Label alertLabel = new Label(lowStock > 0 ? lowStock + " low stock" : "All good OK");
        alertLabel.getStyleClass().addAll("dash-stock-badge", lowStock > 0 ? "dash-badge-low" : "dash-badge-ok");

        HBox top = new HBox(10, iconLabel, new VBox(2, nameLabel, productsLabel));
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);

        Region divider = new Region();
        divider.getStyleClass().add("admin-dialog-divider");
        divider.setPrefHeight(1);

        VBox card = new VBox(12, top, divider, stockBlock, alertLabel);
        card.getStyleClass().add("dash-cat-card");
        return card;
    }

    private void showDashboard() {
        DashboardStats stats = safe(controller::stats);
        if (stats == null) return;

        List<Product> products = controller.products("");
        List<Order> orders = controller.orders();
        List<String> logs = controller.logs();
        int totalStock = products.stream().mapToInt(Product::getQuantity).sum();
        BigDecimal revenue = stats.getRevenue() == null ? BigDecimal.ZERO : stats.getRevenue();

        VBox cardProducts = dashKpiCard("P", "Total Products", String.valueOf(stats.getProducts()), "+12%", "blue");
        VBox cardStock    = dashKpiCard("S", "Total Stock", String.valueOf(totalStock), stats.getLowStock() + " low", "orange");
        VBox cardOrders   = dashKpiCard("O", "Total Orders", String.valueOf(stats.getOrders()), "+8%", "purple");
        VBox cardRevenue  = dashKpiCard("$", "Revenue", formatPrice(revenue), "+18%", "green");

        HBox kpiRow = new HBox(16, cardProducts, cardStock, cardOrders, cardRevenue);
        kpiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        VBox revenueCard = dashboardPanel("Sales Revenue", "Monthly", monthlySalesChart());
        VBox categoryCard = topCategoriesCard(products);
        HBox analyticsRow = new HBox(16, revenueCard, categoryCard);
        HBox.setHgrow(revenueCard, Priority.ALWAYS);
        revenueCard.setMinWidth(500);
        categoryCard.setPrefWidth(390);
        categoryCard.setMinWidth(360);

        VBox activityCard = recentActivityCard(logs, orders);
        VBox productsCard = topProductsCard(products);
        HBox bottomRow = new HBox(16, activityCard, productsCard);
        HBox.setHgrow(activityCard, Priority.ALWAYS);
        HBox.setHgrow(productsCard, Priority.ALWAYS);
        activityCard.setMinWidth(0);
        productsCard.setMinWidth(0);
        activityCard.setPrefWidth(0);
        productsCard.setPrefWidth(0);
        activityCard.setMaxWidth(Double.MAX_VALUE);
        productsCard.setMaxWidth(Double.MAX_VALUE);

        VBox body = new VBox(18, kpiRow, analyticsRow, bottomRow);
        body.getStyleClass().add("dash-body");

        setContent("Dashboard", body);
    }

    private VBox dashKpiCard(String icon, String label, String value, String trend, String accent) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("dash-kpi-icon", accent);

        Label trendLabel = new Label(trend == null ? "" : " " + trend);
        trendLabel.getStyleClass().add("dash-kpi-trend");
        trendLabel.setVisible(trend != null);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, iconLabel, spacer, trendLabel);
        top.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("dash-kpi-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("dash-kpi-value");

        VBox card = new VBox(10, top, labelNode, valueNode);
        card.getStyleClass().addAll("dash-kpi-card", accent);
        return card;
    }

    private VBox dashboardWelcome(DashboardStats stats, BigDecimal averageOrder) {
        Label eyebrow = new Label("ADMIN OVERVIEW");
        eyebrow.getStyleClass().add("dash-welcome-eyebrow");

        Label title = new Label("Welcome back, " + controller.currentUser().getName() + "!");
        title.getStyleClass().add("dash-welcome-title");

        Label subtitle = new Label(stats.getOrders() + " orders tracked today . Average order " + formatPrice(averageOrder));
        subtitle.getStyleClass().add("dash-welcome-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addProduct = Ui.primary("Add product");
        addProduct.getStyleClass().add("dash-welcome-action");
        addProduct.setOnAction(e -> {
            selectNav("Products");
            productDialog(null);
        });

        HBox content = new HBox(18, new VBox(4, eyebrow, title, subtitle), spacer, addProduct);
        content.setAlignment(Pos.CENTER_LEFT);
        VBox card = new VBox(content);
        card.getStyleClass().add("dash-welcome-card");
        return card;
    }

    private VBox dashboardPanel(String title, String actionText, javafx.scene.Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dash-panel-title");

        Button action = Ui.secondary(actionText);
        action.getStyleClass().add("dash-panel-action");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleLabel, spacer, action);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(14, header, content);
        panel.getStyleClass().add("dash-panel");
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private VBox topCategoriesCard(List<Product> products) {
        PieChart chart = categoryChart();
        chart.setLegendVisible(false);
        chart.setLabelsVisible(false);
        chart.setPrefHeight(245);
        chart.getStyleClass().add("dash-donut-chart");

        java.util.Map<String, Integer> stockByCategory = products.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getCategoryName() == null || p.getCategoryName().isBlank() ? "Uncategorized" : p.getCategoryName(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.summingInt(Product::getQuantity)
                ));
        int total = Math.max(1, stockByCategory.values().stream().mapToInt(Integer::intValue).sum());

        VBox rows = new VBox(9);
        stockByCategory.entrySet().stream().limit(4).forEach(entry -> {
            int pct = (int) Math.round(entry.getValue() * 100.0 / total);
            rows.getChildren().add(categoryLine(entry.getKey(), entry.getValue() + " units", pct + "%"));
        });
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(categoryLine("No category", "0 units", "0%"));
        }

        VBox panel = dashboardPanel("Top Categories", "See All", new VBox(8, chart, rows));
        panel.getStyleClass().add("dash-category-panel");
        return panel;
    }

    private HBox categoryLine(String name, String value, String percent) {
        Label dot = new Label("-");
        dot.getStyleClass().add("dash-category-dot");
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("dash-category-name");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dash-category-value");
        Label percentLabel = new Label(percent);
        percentLabel.getStyleClass().add("dash-category-percent");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(7, dot, nameLabel, spacer, valueLabel, percentLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox recentActivityCard(List<String> logs, List<Order> orders) {
        VBox rows = new VBox(10);
        logs.stream().limit(4).forEach(log -> rows.getChildren().add(activityRow("A", log, "System")));
        if (rows.getChildren().isEmpty()) {
            orders.stream().limit(4).forEach(order -> rows.getChildren().add(activityRow(
                    "O",
                    "Order #" + order.getId() + " . " + order.getCustomerName(),
                    order.getStatus() == null ? "Order" : order.getStatus().name().replace('_', ' ')
            )));
        }
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(activityRow("N", "No recent activity yet", "Ready"));
        }

        VBox panel = dashboardPanel("Recent Activity", "See All", rows);
        panel.getStyleClass().add("dash-activity-panel");
        return panel;
    }

    private HBox activityRow(String iconText, String message, String badgeText) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("dash-activity-icon");

        Label title = new Label(message);
        title.getStyleClass().add("dash-activity-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setMinHeight(Region.USE_PREF_SIZE);

        Label meta = new Label("Latest update");
        meta.getStyleClass().add("dash-activity-meta");

        VBox copy = new VBox(2, title, meta);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Label badge = new Label(badgeText);
        badge.getStyleClass().add("dash-activity-badge");
        badge.setMinWidth(82);
        badge.setPrefWidth(82);
        badge.setMaxWidth(82);
        badge.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        HBox row = new HBox(12, icon, copy, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("dash-activity-row");
        return row;
    }

    private VBox topProductsCard(List<Product> products) {
        VBox rows = new VBox(0);
        products.stream()
                .sorted(java.util.Comparator.comparingInt(Product::getQuantity).reversed())
                .limit(5)
                .forEach(product -> rows.getChildren().add(productTableRow(product)));
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(productTableEmptyRow());
        }

        HBox header = new HBox(
                productTableHeader("Product", 250),
                productTableHeader("Stock", 70),
                productTableHeader("Price", 105),
                productTableHeader("Status", 100)
        );
        header.getStyleClass().add("dash-product-table-head");

        VBox table = new VBox(header, rows);
        table.getStyleClass().add("dash-product-table");

        VBox panel = dashboardPanel("Top Products", "Sort", table);
        panel.getStyleClass().add("dash-products-panel");
        return panel;
    }

    private Label productTableHeader(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("dash-product-th");
        label.setPrefWidth(width);
        return label;
    }

    private HBox productTableRow(Product product) {
        Label name = new Label(product.getName());
        name.getStyleClass().add("dash-product-cell-name");
        name.setPrefWidth(250);
        name.setMaxWidth(250);
        name.setWrapText(false);
        name.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        Label stock = new Label(String.valueOf(product.getQuantity()));
        stock.getStyleClass().add("dash-product-cell");
        stock.setPrefWidth(70);

        Label price = new Label(formatPrice(product.getPrice()));
        price.getStyleClass().add("dash-product-cell");
        price.setPrefWidth(105);
        price.setMaxWidth(105);
        price.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        Label status = new Label(product.getQuantity() < 10 ? "Low Stock" : "In Stock");
        status.getStyleClass().addAll("dash-stock-badge", product.getQuantity() < 10 ? "dash-badge-low" : "dash-badge-ok");
        status.setPrefWidth(100);
        status.setMaxWidth(100);
        status.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        HBox row = new HBox(name, stock, price, status);
        row.getStyleClass().add("dash-product-table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox productTableEmptyRow() {
        Label empty = new Label("No products yet");
        empty.getStyleClass().add("dash-product-cell-name");
        HBox row = new HBox(empty);
        row.getStyleClass().add("dash-product-table-row");
        return row;
    }

    private VBox dashProductCard(Product p) {
        // Image
        ImageView img = new ImageView();
        try {
            String url = resolveImage(p.getImageUrl());
            if (!url.isBlank()) img.setImage(new Image(url, true));
        } catch (Exception ignored) {}
        img.setFitWidth(54);
        img.setFitHeight(54);
        img.setPreserveRatio(true);

        // Stock badge
        String badgeText;
        String badgeClass;
        if (p.getQuantity() == 0) {
            badgeText = "Out of Stock"; badgeClass = "dash-badge-out";
        } else if (p.getQuantity() < 10) {
            badgeText = "Low Stock";    badgeClass = "dash-badge-low";
        } else {
            badgeText = "In Stock";     badgeClass = "dash-badge-ok";
        }
        Label badge = new Label(badgeText);
        badge.getStyleClass().addAll("dash-stock-badge", badgeClass);

        Label name = new Label(p.getName());
        name.getStyleClass().add("dash-product-name");
        name.setWrapText(true);

        Label category = new Label(p.getCategoryName() == null ? "-" : p.getCategoryName());
        category.getStyleClass().add("dash-product-category");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nameRow = new HBox(8, name, spacer, badge);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Stats row
        VBox stockCol  = dashStatCol("Stock",  String.valueOf(p.getQuantity()));
        VBox priceCol  = dashStatCol("Price",  "$" + p.getPrice());
        VBox soldCol   = dashStatCol("Sold",   "-");
        HBox statsRow  = new HBox(24, stockCol, priceCol, soldCol);

        // Progress bar
        double ratio = Math.min(1.0, p.getQuantity() / 300.0);
        HBox track = new HBox();
        track.getStyleClass().add("dash-progress-track");
        Region fill = new Region();
        fill.getStyleClass().addAll("dash-progress-fill",
                p.getQuantity() == 0 ? "out" : p.getQuantity() < 10 ? "low" : "ok");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(ratio));
        track.getChildren().add(fill);

        HBox top = new HBox(14, img, new VBox(4, nameRow, category));
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, top, statsRow, track);
        card.getStyleClass().add("dash-product-card");
        return card;
    }

    private VBox dashStatCol(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("dash-stat-label");
        Label v = new Label(value);
        v.getStyleClass().add("dash-stat-value");
        return new VBox(2, l, v);
    }

    private BarChart<String, Number> monthlySalesChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setTitle(null);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] values = {4100, 3800, 5000, 4500, 6100, 5800};
        for (int i = 0; i < months.length; i++)
            series.getData().add(new XYChart.Data<>(months[i], values[i]));
        chart.getData().add(series);
        chart.getStyleClass().add("dash-bar-chart");
        return chart;
    }

    private VBox heroPanel(DashboardStats stats) {
        HBox heroTop = new HBox(12, iconBubble("*", "admin-hero-icon"), new Label("TODAY"));
        ((Label) heroTop.getChildren().get(1)).getStyleClass().add("admin-hero-eyebrow");

        Label title = new Label("Your admin workspace is healthy and ready.");
        title.getStyleClass().add("admin-hero-title");
        title.setWrapText(true);

        Label copy = new Label("Monitor catalog quality, user activity and order flow from one place without the heavy dashboard feel.");
        copy.getStyleClass().add("admin-hero-copy");
        copy.setWrapText(true);

        HBox chips = new HBox(10,
                infoChip(stats.getLowStock() + " low stock alerts"),
                infoChip(stats.getUsers() + " active users"),
                infoChip(stats.getOrders() + " total orders")
        );

        VBox hero = new VBox(14, heroTop, title, copy, chips);
        hero.getStyleClass().add("admin-hero-card");
        hero.setPadding(new Insets(24));
        return hero;
    }

    private VBox quickActionsCard() {
        Button products = Ui.primary("Manage products");
        products.setMaxWidth(Double.MAX_VALUE);
        products.setOnAction(e -> {
            selectNav("Products");
            showProducts();
        });

        Button users = Ui.secondary("Review users");
        users.setMaxWidth(Double.MAX_VALUE);
        users.setOnAction(e -> {
            selectNav("Users");
            showUsers();
        });

        Button orders = Ui.secondary("Track orders");
        orders.setMaxWidth(Double.MAX_VALUE);
        orders.setOnAction(e -> {
            selectNav("Orders");
            showOrders();
        });

        VBox card = surfaceCard(
                sectionTitle("Quick actions", "Jump into the most common admin tasks.", "?"),
                products,
                users,
                orders
        );
        card.setPrefWidth(280);
        return card;
    }

    private VBox metricCard(String label, String value, String detail) {
        String iconText = switch (label) {
            case "Products" -> "?";
            case "Users" -> "?";
            case "Orders" -> "?";
            case "Revenue" -> "?";
            default -> "-";
        };

        Label overline = new Label(label.toUpperCase());
        overline.getStyleClass().add("admin-card-overline");

        Label number = new Label(value);
        number.getStyleClass().add("stat-number");

        Label hint = new Label(detail);
        hint.getStyleClass().add("subtitle");

        HBox top = new HBox(10, iconBubble(iconText, "admin-metric-icon"), overline);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, top, number, hint);
        card.getStyleClass().add("admin-metric-card");
        card.setPadding(new Insets(20));
        return card;
    }

    private VBox chartCard(String title, javafx.scene.Node chart) {
        String iconText = switch (title) {
            case "Products by category" -> "?";
            case "Users by role" -> "?";
            case "Stock by product" -> "?";
            case "Orders by status" -> "?";
            default -> "-";
        };
        VBox card = surfaceCard(sectionTitle(title, "Readable visuals with less noise.", iconText), chart);
        card.getStyleClass().add("chart-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private HBox infoChip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-chip-label");
        HBox chip = new HBox(label);
        chip.getStyleClass().add("admin-chip");
        chip.setAlignment(Pos.CENTER_LEFT);
        return chip;
    }

    private VBox sectionTitle(String title, String subtitle) {
        Label t = new Label(title);
        t.getStyleClass().add("title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("subtitle");
        s.setWrapText(true);
        return new VBox(4, t, s);
    }

    private VBox sectionTitle(String title, String subtitle, String iconText) {
        HBox titleRow = new HBox(10, iconBubble(iconText, "admin-section-icon"), new Label(title));
        titleRow.setAlignment(Pos.CENTER_LEFT);
        ((Label) titleRow.getChildren().get(1)).getStyleClass().add("title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("subtitle");
        s.setWrapText(true);
        return new VBox(4, titleRow, s);
    }

    private HBox pageHero(String title, String subtitle, String kicker) {
        Label overline = new Label(kicker.toUpperCase());
        overline.getStyleClass().add("admin-page-hero-overline");

        Label heroTitle = new Label(title);
        heroTitle.getStyleClass().add("admin-page-hero-title");
        heroTitle.setWrapText(true);

        Label heroSubtitle = new Label(subtitle);
        heroSubtitle.getStyleClass().add("admin-page-hero-subtitle");
        heroSubtitle.setWrapText(true);

        VBox copy = new VBox(8, overline, heroTitle, heroSubtitle);
        copy.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hero = new HBox(18, copy, spacer);
        hero.getStyleClass().add("admin-page-hero");
        hero.setAlignment(Pos.CENTER_LEFT);
        return hero;
    }

    private VBox surfaceCard(javafx.scene.Node... nodes) {
        VBox card = new VBox(16, nodes);
        card.getStyleClass().add("admin-surface-card");
        card.setPadding(new Insets(20));
        return card;
    }

    private StackPane iconBubble(String iconText, String styleClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add(styleClass);
        StackPane bubble = new StackPane(icon);
        bubble.getStyleClass().add("admin-icon-bubble");
        return bubble;
    }

    private VBox progressLine(String label, int value, int max) {
        Label caption = new Label(label + "  " + value + "/" + max);
        caption.getStyleClass().add("admin-progress-label");
        HBox track = new HBox();
        track.getStyleClass().add("progress-track");
        Region fill = new Region();
        fill.getStyleClass().add("progress-fill");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(Math.min(1.0, value / (double) max)));
        track.getChildren().add(fill);
        return new VBox(7, caption, track);
    }

    private void showProducts() {
        TextField search = Ui.text("Search products");
        TableView<Product> table = productTable();

        // Hauteur fixe - le scroll se fait sur la PAGE, pas dans la table
        double rowH = 96;
        table.setPrefHeight(PAGE_SIZE * rowH + 58);
        table.setMinHeight(PAGE_SIZE * rowH + 58);
        table.setMaxHeight(PAGE_SIZE * rowH + 58);

        Pagination pagination = new Pagination();
        pagination.getStyleClass().add("table-pagination");

        Runnable reload = () -> {
            List<Product> all = controller.products(search.getText());
            int pages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
            if (pagination.getPageCount() != pages) pagination.setPageCount(pages);
            int idx = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            table.setItems(FXCollections.observableArrayList(
                    all.subList(idx * PAGE_SIZE, Math.min(idx * PAGE_SIZE + PAGE_SIZE, all.size()))));
        };

        reload.run();
        pagination.currentPageIndexProperty().addListener((o, old, val) -> reload.run());
        search.textProperty().addListener((o, old, val) -> { pagination.setCurrentPageIndex(0); reload.run(); });

        Button add = Ui.primary("+ Product");
        add.setOnAction(e -> {
            productDialog(null);
            pagination.setCurrentPageIndex(0);
            reload.run();
        });

        Button categories = Ui.secondary("+ Category");
        categories.setOnAction(e -> categoryDialog());

        Button delete = Ui.danger("Delete");
        delete.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && Ui.confirm("Delete product", "Delete " + selected.getName() + "?")) {
                run(() -> controller.deleteProduct(selected));
                reload.run();
            }
        });

        Button export = Ui.secondary("Export CSV");
        export.setOnAction(e -> exportProducts(table.getItems()));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    productDialog(row.getItem());
                    reload.run();
                }
            });
            return row;
        });

        BorderPane tableShell = new BorderPane(table);
        tableShell.setBottom(pagination);
        BorderPane.setMargin(pagination, new Insets(8, 0, 0, 0));

        VBox body = new VBox(16,
                surfaceCard(Ui.toolbar(search, add, categories, export, delete)),
                surfaceCard(tableShell)
        );
        setContent("Product management", body);
    }

    private void showUsers() {
        List<User> users = controller.users();
        TableView<User> table = new TableView<>();
        decorateTable(table, 68);

        // Hauteur fixe - scroll sur la PAGE, pas dans la table
        table.setPrefHeight(PAGE_SIZE * 68.0 + 58);
        table.setMinHeight(PAGE_SIZE * 68.0 + 58);
        table.setMaxHeight(PAGE_SIZE * 68.0 + 58);

        table.getColumns().add(userIdentityColumn());
        table.getColumns().add(userEmailColumn());
        table.getColumns().add(userRoleColumn());
        table.getColumns().add(userStatusColumn());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Pagination pagination = new Pagination();
        pagination.getStyleClass().add("table-pagination");

        final List<User>[] src = new List[]{users};
        Runnable reload = () -> {
            List<User> all = src[0];
            int pages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
            if (pagination.getPageCount() != pages) pagination.setPageCount(pages);
            int idx = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            table.setItems(FXCollections.observableArrayList(
                    all.subList(idx * PAGE_SIZE, Math.min(idx * PAGE_SIZE + PAGE_SIZE, all.size()))));
        };
        reload.run();
        pagination.currentPageIndexProperty().addListener((o, old, val) -> reload.run());

        Button stockManager = Ui.primary("+ Stock manager");
        stockManager.getStyleClass().add("admin-action-primary");
        stockManager.setOnAction(e -> {
            createStockManagerDialog();
            src[0] = controller.users();
            pagination.setCurrentPageIndex(0);
            reload.run();
        });

        Button activate = Ui.secondary("Activate");
        activate.getStyleClass().add("admin-action-success");
        activate.setOnAction(e -> updateUser(table, UserStatus.ACTIVE, src, pagination, reload));
        Button deactivate = Ui.secondary("Deactivate");
        deactivate.getStyleClass().add("admin-action-muted");
        deactivate.setOnAction(e -> updateUser(table, UserStatus.INACTIVE, src, pagination, reload));
        Button block = Ui.danger("Block");
        block.getStyleClass().add("admin-action-danger");
        block.setOnAction(e -> updateUser(table, UserStatus.BLOCKED, src, pagination, reload));
        Button export = Ui.secondary("Export CSV");
        export.getStyleClass().add("admin-action-export");
        export.setOnAction(e -> exportUsers(table.getItems()));

        HBox overview = new HBox(16,
                adminOverviewCard("Total users", String.valueOf(users.size()), "All platform accounts", "U", "blue"),
                adminOverviewCard("Active", String.valueOf(countUsers(users, UserStatus.ACTIVE)), "Ready to use the store", "A", "green"),
                adminOverviewCard("Inactive", String.valueOf(countUsers(users, UserStatus.INACTIVE)), "Waiting for activation", "I", "orange"),
                adminOverviewCard("Blocked", String.valueOf(countUsers(users, UserStatus.BLOCKED)), "Restricted access", "B", "red")
        );
        overview.getStyleClass().add("admin-overview-row");

        HBox toolbar = Ui.toolbar(stockManager, activate, deactivate, export, block);
        toolbar.getStyleClass().add("admin-management-toolbar");

        BorderPane tableShell = new BorderPane(table);
        tableShell.setBottom(pagination);
        BorderPane.setMargin(pagination, new Insets(8, 0, 0, 0));

        VBox body = new VBox(16,
                overview,
                surfaceCard(sectionTitle("Account control", "Create managers, update access and export users from one clean workspace.", "U"), toolbar),
                surfaceCard(tableShell)
        );
        setContent("User administration", body);
    }

    private void showOrders() {
        List<Order> orders = controller.orders();
        TableView<Order> table = new TableView<>();
        decorateTable(table, 68);

        // Hauteur fixe - scroll sur la PAGE, pas dans la table
        table.setPrefHeight(PAGE_SIZE * 68.0 + 58);
        table.setMinHeight(PAGE_SIZE * 68.0 + 58);
        table.setMaxHeight(PAGE_SIZE * 68.0 + 58);

        table.getColumns().add(orderIdColumn());
        table.getColumns().add(orderCustomerColumn());
        table.getColumns().add(orderTotalColumn());
        table.getColumns().add(orderStatusColumn());
        table.getColumns().add(orderDateColumn());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Pagination pagination = new Pagination();
        pagination.getStyleClass().add("table-pagination");

        final List<Order>[] src = new List[]{orders};
        Runnable reload = () -> {
            List<Order> all = src[0];
            int pages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
            if (pagination.getPageCount() != pages) pagination.setPageCount(pages);
            int idx = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            table.setItems(FXCollections.observableArrayList(
                    all.subList(idx * PAGE_SIZE, Math.min(idx * PAGE_SIZE + PAGE_SIZE, all.size()))));
        };
        reload.run();
        pagination.currentPageIndexProperty().addListener((o, old, val) -> reload.run());

        ComboBox<OrderStatus> status = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));
        status.setPromptText("Set status");
        status.getStyleClass().add("admin-order-status-picker");
        Button update = Ui.primary("Update order");
        update.getStyleClass().add("admin-action-primary");
        update.setOnAction(e -> {
            Order order = table.getSelectionModel().getSelectedItem();
            if (order != null && status.getValue() != null) {
                run(() -> controller.updateOrderStatus(order, status.getValue()));
                src[0] = controller.orders();
                reload.run();
            }
        });
        Button export = Ui.secondary("Export CSV");
        export.getStyleClass().add("admin-action-export");
        export.setOnAction(e -> exportOrders(table.getItems()));

        HBox overview = new HBox(16,
                adminOverviewCard("Total orders", String.valueOf(orders.size()), "Orders in the pipeline", "O", "blue"),
                adminOverviewCard("Pending", String.valueOf(countOrders(orders, OrderStatus.PENDING, OrderStatus.PROCESSING)), "Need attention", "P", "orange"),
                adminOverviewCard("Completed", String.valueOf(countOrders(orders, OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED)), "Healthy customer flow", "C", "green"),
                adminOverviewCard("Cancelled", String.valueOf(countOrders(orders, OrderStatus.CANCELLED)), "Lost or stopped orders", "X", "red")
        );
        overview.getStyleClass().add("admin-overview-row");

        HBox toolbar = Ui.toolbar(status, update, export);
        toolbar.getStyleClass().add("admin-management-toolbar");

        BorderPane tableShell = new BorderPane(table);
        tableShell.setBottom(pagination);
        BorderPane.setMargin(pagination, new Insets(8, 0, 0, 0));

        VBox body = new VBox(16,
                overview,
                surfaceCard(sectionTitle("Order workflow", "Select an order, change its status and keep fulfillment moving.", "O"), toolbar),
                surfaceCard(tableShell)
        );
        setContent("All orders", body);
    }

    private VBox adminOverviewCard(String label, String value, String detail, String iconText, String accentClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("admin-overview-icon");
        icon.getStyleClass().add(accentClass);

        Label caption = new Label(label.toUpperCase());
        caption.getStyleClass().add("admin-card-overline");

        Label number = new Label(value);
        number.getStyleClass().add("admin-overview-value");

        Label hint = new Label(detail);
        hint.getStyleClass().add("admin-overview-detail");
        hint.setWrapText(true);

        HBox top = new HBox(10, icon, caption);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(11, top, number, hint);
        card.getStyleClass().add("admin-overview-card");
        card.getStyleClass().add(accentClass);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private int countUsers(List<User> users, UserStatus status) {
        int count = 0;
        for (User user : users) {
            if (user.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    private int countOrders(List<Order> orders, OrderStatus... statuses) {
        int count = 0;
        for (Order order : orders) {
            for (OrderStatus status : statuses) {
                if (order.getStatus() == status) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private TableColumn<User, String> userIdentityColumn() {
        TableColumn<User, String> column = new TableColumn<>("User");
        column.setPrefWidth(330);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                User user = getTableView().getItems().get(getIndex());
                Label avatar = new Label(initials(user.getName()));
                avatar.getStyleClass().add("admin-user-avatar");

                Label name = new Label(user.getName());
                name.getStyleClass().add("admin-user-name");

                Label id = new Label("ID #" + user.getId());
                id.getStyleClass().add("admin-user-id");

                VBox copy = new VBox(3, name, id);
                copy.setAlignment(Pos.CENTER_LEFT);

                HBox row = new HBox(11, avatar, copy);
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
        return column;
    }

    private TableColumn<User, String> userEmailColumn() {
        TableColumn<User, String> column = new TableColumn<>("Email");
        column.setPrefWidth(270);
        column.setMinWidth(230);
        column.setMaxWidth(290);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label label = new Label(email);
                label.getStyleClass().add("admin-email-full");
                label.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
                label.setMinWidth(Region.USE_PREF_SIZE);
                setText(null);
                setGraphic(label);
            }
        });
        return column;
    }

    private TableColumn<User, String> userRoleColumn() {
        TableColumn<User, String> column = new TableColumn<>("Role");
        column.setPrefWidth(140);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole() == null ? "" : data.getValue().getRole().name()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item == null ? "UNKNOWN" : item.replace('_', ' '));
                badge.getStyleClass().add("admin-role-badge");
                setText(null);
                setGraphic(badge);
            }
        });
        return column;
    }

    private TableColumn<User, String> userStatusColumn() {
        TableColumn<User, String> column = new TableColumn<>("Status");
        column.setPrefWidth(160);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus() == null ? "" : data.getValue().getStatus().name()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item == null || item.isBlank() ? "UNKNOWN" : item);
                badge.getStyleClass().add("admin-status-badge");
                badge.getStyleClass().add(statusAccent(item));
                setText(null);
                setGraphic(badge);
            }
        });
        return column;
    }

    private TableColumn<Order, String> orderIdColumn() {
        TableColumn<Order, String> column = new TableColumn<>("Order");
        column.setPrefWidth(130);
        column.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label label = new Label(item);
                label.getStyleClass().add("admin-order-id");
                setText(null);
                setGraphic(label);
            }
        });
        return column;
    }

    private TableColumn<Order, String> orderCustomerColumn() {
        TableColumn<Order, String> column = new TableColumn<>("Customer");
        column.setPrefWidth(230);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Order order = getTableView().getItems().get(getIndex());
                Label avatar = new Label(initials(order.getCustomerName()));
                avatar.getStyleClass().add("admin-order-avatar");

                Label customer = new Label(item == null || item.isBlank() ? "Customer" : item);
                customer.getStyleClass().add("admin-user-name");

                Label detail = new Label("Customer #" + order.getUserId());
                detail.getStyleClass().add("admin-user-id");

                VBox copy = new VBox(3, customer, detail);
                copy.setAlignment(Pos.CENTER_LEFT);
                HBox row = new HBox(11, avatar, copy);
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
        return column;
    }

    private TableColumn<Order, String> orderTotalColumn() {
        TableColumn<Order, String> column = new TableColumn<>("Total");
        column.setPrefWidth(140);
        column.setCellValueFactory(data -> new SimpleStringProperty(formatPrice(data.getValue().getTotalPrice())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label price = new Label(item);
                price.getStyleClass().add("admin-order-total");
                setText(null);
                setGraphic(price);
            }
        });
        return column;
    }

    private TableColumn<Order, String> orderStatusColumn() {
        TableColumn<Order, String> column = new TableColumn<>("Status");
        column.setPrefWidth(170);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus() == null ? "" : data.getValue().getStatus().name()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item == null ? "UNKNOWN" : item);
                badge.getStyleClass().add("admin-order-status-badge");
                badge.getStyleClass().add(orderAccent(item));
                setText(null);
                setGraphic(badge);
            }
        });
        return column;
    }

    private TableColumn<Order, String> orderDateColumn() {
        TableColumn<Order, String> column = new TableColumn<>("Date");
        column.setPrefWidth(170);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate() == null ? "" : data.getValue().getDate().format(EXPORT_DATE_FORMAT)));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label date = new Label(item);
                date.getStyleClass().add("admin-order-date");
                setText(null);
                setGraphic(date);
            }
        });
        return column;
    }

    private String statusAccent(String status) {
        String value = status == null ? "" : status.toUpperCase();
        if (value.contains("BLOCKED")) {
            return "blocked";
        }
        if (value.contains("INACTIVE")) {
            return "inactive";
        }
        if (value.contains("ACTIVE")) {
            return "active";
        }
        return "inactive";
    }

    private String orderAccent(String status) {
        String value = status == null ? "" : status.toUpperCase();
        if (value.contains("DELIVERED") || value.contains("SHIPPED") || value.contains("PAID")) {
            return "completed";
        }
        if (value.contains("CANCELLED")) {
            return "cancelled";
        }
        if (value.contains("PROCESSING")) {
            return "processing";
        }
        return "pending";
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showProfile() {
        selectNav("Profile");
        User user = controller.currentUser();

        StackPane avatar = new StackPane(new Label(initials(user.getName())));
        avatar.getStyleClass().add("role-profile-avatar");

        Label name = new Label(value(user.getName()).toUpperCase());
        name.getStyleClass().add("role-profile-hero-name");
        Label email = new Label(value(user.getEmail()));
        email.getStyleClass().add("role-profile-hero-email");
        Label badge = new Label("ADMINISTRATOR");
        badge.getStyleClass().add("role-profile-badge");

        VBox identity = new VBox(8, name, email, badge);
        HBox hero = new HBox(34, avatar, identity);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(58, 54, 48, 54));

        VBox rows = new VBox(16,
                profileMenuRow("Contact & Support", "Modifier le nom et le telephone", "C",
                        () -> setContent("CONTACT", createContactProfilePage())),
                profileMenuRow("Adresse", profileSubtitle(user.getDeliveryAddress(), "Aucune adresse definie"), "A",
                        () -> setContent("ADRESSE", createAddressProfilePage())),
                profileMenuRow("Securite du compte", "Compte admin actif et protege", "S",
                        () -> setContent("SECURITE", createSecurityProfilePage()))
        );
        rows.setPadding(new Insets(0, 54, 54, 54));

        VBox card = new VBox(0, hero, rows);
        card.getStyleClass().add("role-profile-main-card");
        card.setMaxWidth(980);

        Button logout = profileLogoutButton(controller::logout);
        VBox body = new VBox(20, card, logout);
        body.setAlignment(Pos.TOP_CENTER);
        body.setPadding(new Insets(12, 0, 40, 0));
        setContent("VOTRE PROFIL", body);
    }

    private Button profileLogoutButton(Runnable action) {
        Button logout = new Button("Logout");
        logout.getStyleClass().add("role-profile-logout");
        logout.setOnAction(e -> action.run());
        return logout;
    }

    private HBox profileMenuRow(String title, String subtitle, String iconText, Runnable action) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("role-profile-row-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("role-profile-row-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("role-profile-row-subtitle");
        VBox texts = new VBox(4, titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("->");
        arrow.getStyleClass().add("role-profile-row-arrow");

        HBox row = new HBox(24, icon, texts, spacer, arrow);
        row.getStyleClass().add("role-profile-menu-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setOnMouseClicked(e -> action.run());
        return row;
    }

    private Node createContactProfilePage() {
        User user = controller.currentUser();
        TextField name = profileInput(value(user.getName()), "Nom complet");
        TextField email = profileInput(value(user.getEmail()), "Email");
        email.setEditable(false);
        TextField phone = profileInput(value(user.getPhone()), "+212 600 000 000");

        Button save = Ui.primary("Mettre a jour le profil");
        save.getStyleClass().add("role-profile-wide-save");
        save.setOnAction(e -> saveProfile(name.getText(), phone.getText(), user.getDeliveryAddress(), "Profil mis a jour."));

        VBox form = profileFormPage("Coordonnees",
                profileBackButton(this::showProfile),
                profileField("NOM COMPLET", name),
                profileField("ADRESSE EMAIL", email),
                profileField("TELEPHONE", phone),
                save);
        return form;
    }

    private Node createAddressProfilePage() {
        User user = controller.currentUser();
        TextArea address = new TextArea(value(user.getDeliveryAddress()));
        address.setPromptText("Adresse professionnelle ou adresse de contact");
        address.setPrefRowCount(5);
        address.setWrapText(true);
        address.getStyleClass().add("role-profile-area");

        Button save = Ui.primary("Enregistrer l'adresse");
        save.getStyleClass().add("role-profile-wide-save");
        save.setOnAction(e -> saveProfile(user.getName(), user.getPhone(), address.getText(), "Adresse mise a jour."));

        return profileFormPage("Adresse",
                profileBackButton(this::showProfile),
                profileField("ADRESSE", address),
                save);
    }

    private Node createSecurityProfilePage() {
        PasswordField currentPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();

        Label success = profileFeedback("role-profile-success");
        Label errorCurrent = profileError();
        Label errorNew = profileError();
        Label errorConfirm = profileError();

        VBox currentBox = profileField("MOT DE PASSE ACTUEL", currentPass);
        VBox newBox = profileField("NOUVEAU MOT DE PASSE", newPass);
        VBox confirmBox = profileField("CONFIRMER LE MOT DE PASSE", confirmPass);

        Button save = new Button("Changer le mot de passe");
        save.getStyleClass().add("role-profile-password-save");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            resetSecurityFeedback(success, errorCurrent, errorNew, errorConfirm, currentBox, newBox, confirmBox);

            String oldPassword = currentPass.getText();
            String newPassword = newPass.getText();
            String confirmation = confirmPass.getText();

            if (oldPassword == null || oldPassword.isBlank()) {
                showFieldError(errorCurrent, currentBox, "Veuillez saisir votre mot de passe actuel.");
                return;
            }
            if (newPassword == null || newPassword.isBlank()) {
                showFieldError(errorNew, newBox, "Veuillez saisir un nouveau mot de passe.");
                return;
            }
            if (!newPassword.equals(confirmation)) {
                showFieldError(errorConfirm, confirmBox, "Les mots de passe ne correspondent pas.");
                return;
            }

            int result = controller.changerMotDePasse(oldPassword, newPassword);
            if (result == 1) {
                success.setText("Mot de passe mis a jour avec succes !");
                success.setVisible(true);
                success.setManaged(true);
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> showProfile());
                delay.play();
            } else if (result == -1) {
                showFieldError(errorCurrent, currentBox, "Le mot de passe actuel est incorrect.");
            } else {
                showFieldError(errorCurrent, currentBox, "Une erreur est survenue lors de la mise a jour.");
            }
        });

        return profileFormPage("Securite du compte",
                profileBackButton(this::showProfile),
                success,
                currentBox,
                errorCurrent,
                newBox,
                errorNew,
                confirmBox,
                errorConfirm,
                save);
    }

    private VBox profileFormPage(String title, Node... children) {
        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("role-profile-form-title");

        VBox form = new VBox(22);
        if (children.length > 0) {
            form.getChildren().add(children[0]);
        }
        form.getChildren().add(pageTitle);
        for (int i = 1; i < children.length; i++) {
            form.getChildren().add(children[i]);
        }
        form.setMaxWidth(660);

        VBox page = new VBox(30, form);
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(54, 0, 70, 0));
        page.setStyle("-fx-background-color: white;");
        return page;
    }

    private Button profileBackButton(Runnable action) {
        Button back = new Button("<-");
        back.getStyleClass().add("role-profile-back");
        back.setOnAction(e -> action.run());
        return back;
    }

    private TextField profileInput(String value, String prompt) {
        TextField field = Ui.text(prompt);
        field.setText(value(value));
        return field;
    }

    private void saveProfile(String name, String phone, String address, String message) {
        if (name == null || name.trim().isEmpty()) {
            Ui.error(new IllegalArgumentException("Name is required."));
            return;
        }
        User user = controller.currentUser();
        user.setName(name.trim());
        user.setPhone(phone == null ? null : phone.trim());
        user.setDeliveryAddress(address == null ? null : address.trim());
        try {
            controller.updateProfile(user);
            Ui.info("Profile", message);
            showProfile();
        } catch (RuntimeException ex) {
            Ui.error(ex);
        }
    }

    private VBox profileField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("role-profile-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.getStyleClass().add("role-profile-input");
        }
        return new VBox(8, label, field);
    }

    private Label profileFeedback(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private Label profileError() {
        Label label = profileFeedback("role-profile-error");
        return label;
    }

    private void resetSecurityFeedback(Label success, Label errorCurrent, Label errorNew, Label errorConfirm,
                                       VBox currentBox, VBox newBox, VBox confirmBox) {
        success.setVisible(false);
        success.setManaged(false);
        errorCurrent.setVisible(false);
        errorCurrent.setManaged(false);
        errorNew.setVisible(false);
        errorNew.setManaged(false);
        errorConfirm.setVisible(false);
        errorConfirm.setManaged(false);
        currentBox.getStyleClass().remove("role-profile-field-error");
        newBox.getStyleClass().remove("role-profile-field-error");
        confirmBox.getStyleClass().remove("role-profile-field-error");
    }

    private void showFieldError(Label label, VBox fieldBox, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
        if (!fieldBox.getStyleClass().contains("role-profile-field-error")) {
            fieldBox.getStyleClass().add("role-profile-field-error");
        }
    }

    private TextField profileReadonly(String value) {
        TextField field = new TextField(value(value));
        field.setEditable(false);
        field.setFocusTraversable(false);
        field.getStyleClass().add("role-profile-readonly");
        return field;
    }

    private VBox profileFact(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("role-profile-fact-label");
        Label v = new Label(value(value));
        v.getStyleClass().add("role-profile-fact-value");
        return new VBox(2, l, v);
    }

    private Label profileChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("role-profile-chip");
        return chip;
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private String profileSubtitle(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void showReports() {
        DashboardStats stats = safe(controller::stats);
        if (stats == null) return;

        List<Product> products = controller.products("");
        List<User> users = controller.users();
        List<Order> orders = controller.orders();
        BigDecimal revenue = stats.getRevenue() == null ? BigDecimal.ZERO : stats.getRevenue();
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);
        BigDecimal thisMonthRevenue = revenueForMonth(orders, thisMonth);
        BigDecimal lastMonthRevenue = revenueForMonth(orders, lastMonth);
        int thisMonthOrders = ordersForMonth(orders, thisMonth);
        int lastMonthOrders = ordersForMonth(orders, lastMonth);

        Button exportBtn = Ui.secondary("Download report");
        exportBtn.getStyleClass().add("admin-report-dark-action");
        exportBtn.setOnAction(e -> exportCsv("report-export.csv", "report\n" + controller.report()));

        Button moreBtn = Ui.secondary("Request custom report");
        moreBtn.getStyleClass().add("admin-report-dark-action");

        HBox header = reportHeader(exportBtn, moreBtn);

        BigDecimal average = revenue.divide(
                BigDecimal.valueOf(Math.max(1, stats.getOrders())), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal thisMonthAverage = averageForMonth(orders, thisMonth);
        BigDecimal lastMonthAverage = averageForMonth(orders, lastMonth);

        HBox metricRow = new HBox(16,
                reportSummaryMetric("Net Income", formatPrice(revenue), formatChange(thisMonthRevenue, lastMonthRevenue), "income"),
                reportSummaryMetric("Order per Month", String.valueOf(thisMonthOrders), formatChange(thisMonthOrders, lastMonthOrders), "orders"),
                reportSummaryMetric("Average Contract", formatPrice(average), formatChange(thisMonthAverage, lastMonthAverage), "average"),
                reportSummaryMetric("Growth Rate", percentText(percentChange(thisMonthRevenue, lastMonthRevenue)), "Revenue change from last month", "growth")
        );
        metricRow.getStyleClass().add("admin-report-summary-row");
        metricRow.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        VBox salesPanel = reportPanel("Total Sales", "Revenue and order trend", reportTotalSalesLineChart(orders), "admin-report-chart-panel");
        VBox channelPanel = reportPanel("Sales Activity", "Orders by status", reportSalesActivityDonut(orders), "admin-report-chart-panel");
        HBox chartsRow = new HBox(16, salesPanel, channelPanel);
        HBox.setHgrow(salesPanel, Priority.ALWAYS);
        HBox.setHgrow(channelPanel, Priority.ALWAYS);

        VBox productsPanel = topProductPerformancePanel(products);
        VBox customerPanel = customerByCountryPanel(users);
        HBox lowerRow = new HBox(16, productsPanel, customerPanel);
        HBox.setHgrow(productsPanel, Priority.ALWAYS);
        HBox.setHgrow(customerPanel, Priority.ALWAYS);

        VBox contentBody = new VBox(12, header, metricRow, chartsRow, lowerRow);
        contentBody.getStyleClass().add("admin-report-dark-page");

        setReportContent(contentBody);
    }

    private HBox reportHeader(Button exportBtn, Button moreBtn) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, exportBtn, moreBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(spacer, actions);
        header.getStyleClass().add("admin-report-page-header");
        header.setAlignment(Pos.CENTER_RIGHT);
        return header;
    }

    private VBox reportSummaryMetric(String label, String value, String hint, String accent) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("admin-report-summary-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("admin-report-summary-value");

        Label hintNode = new Label(hint);
        hintNode.getStyleClass().add("admin-report-summary-hint");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, labelNode, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox metric = new VBox(10, top, valueNode, hintNode);
        metric.getStyleClass().addAll("admin-report-summary-metric", accent);
        return metric;
    }

    private VBox reportPanel(String title, String legend, javafx.scene.Node content, String styleClass) {
        Label titleNode = new Label(title);
        titleNode.getStyleClass().add("admin-report-panel-title");

        Button menu = Ui.secondary("...");
        menu.getStyleClass().add("admin-report-panel-menu");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox titleBlock;
        if (legend != null && !legend.isBlank()) {
            Label legendNode = new Label(legend);
            legendNode.getStyleClass().add("admin-report-panel-legend");
            titleBlock = new VBox(4, titleNode, legendNode);
        } else {
            titleBlock = new VBox(4, titleNode);
        }

        HBox header = new HBox(10, titleBlock, spacer, menu);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(14, header, content);
        panel.getStyleClass().addAll("admin-report-panel", styleClass);
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private LineChart<String, Number> reportTotalSalesLineChart(List<Order> orders) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(true);
        chart.setTitle(null);
        chart.getStyleClass().add("admin-report-line-chart");

        XYChart.Series<String, Number> revenue = new XYChart.Series<>();
        revenue.setName("Revenue");
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Orders");

        YearMonth start = YearMonth.now().minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth month = start.plusMonths(i);
            String label = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            revenue.getData().add(new XYChart.Data<>(label, revenueForMonth(orders, month)));
            ordersSeries.getData().add(new XYChart.Data<>(label, ordersForMonth(orders, month)));
        }

        chart.getData().addAll(revenue, ordersSeries);
        chart.setPrefHeight(270);
        return chart;
    }

    private VBox reportSalesActivityDonut(List<Order> orders) {
        var statusCounts = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        order -> order.getStatus() == null ? "UNKNOWN" : order.getStatus().name(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));
        if (statusCounts.isEmpty()) {
            statusCounts.put("NO ORDERS", 1L);
        }

        PieChart chart = new PieChart();
        statusCounts.forEach((status, count) -> chart.getData().add(
                new PieChart.Data(status.replace('_', ' '), count)
        ));
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setTitle(null);
        chart.setPrefHeight(240);
        chart.getStyleClass().add("admin-report-donut-chart");

        Region hole = new Region();
        hole.getStyleClass().add("admin-report-donut-hole");
        hole.setMouseTransparent(true);

        StackPane donut = new StackPane(chart, hole);
        donut.getStyleClass().add("admin-report-donut-wrap");

        HBox legend = new HBox(14);
        int[] legendIndex = {0};
        statusCounts.keySet().forEach(status -> {
            legend.getChildren().add(reportLegendChip(status.replace('_', ' '), "sales-" + (legendIndex[0] % 5)));
            legendIndex[0]++;
        });
        legend.setAlignment(Pos.CENTER);

        VBox box = new VBox(8, donut, legend);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private VBox topProductPerformancePanel(List<Product> products) {
        VBox rows = new VBox(0);
        HBox head = new HBox(
                reportTableHead("Product Name", 270),
                reportTableHead("Stock", 90),
                reportTableHead("Price", 110),
                reportTableHead("Value", 120)
        );
        head.getStyleClass().add("admin-report-product-head");

        rows.getChildren().add(head);
        products.stream()
                .sorted(java.util.Comparator.comparing(
                        p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice(),
                        java.util.Comparator.reverseOrder()
                ))
                .limit(5)
                .forEach(product -> rows.getChildren().add(reportProductRow(product)));

        if (products.isEmpty()) {
            Label empty = new Label("No product data available.");
            empty.getStyleClass().add("admin-report-empty");
            rows.getChildren().add(new HBox(empty));
        }

        return reportPanel("Top Deals", "Companies, deal value and sales owners", rows, "admin-report-table-panel");
    }

    private Label reportTableHead(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("admin-report-table-head");
        label.setPrefWidth(width);
        return label;
    }

    private HBox reportProductRow(Product product) {
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal value = price.multiply(BigDecimal.valueOf(Math.max(0, product.getQuantity())));

        Label name = new Label(product.getName());
        name.getStyleClass().add("admin-report-product-name");
        name.setPrefWidth(270);

        Label stock = new Label(String.valueOf(product.getQuantity()));
        stock.getStyleClass().add("admin-report-product-cell");
        stock.setPrefWidth(90);

        Label priceLabel = new Label(formatPrice(price));
        priceLabel.getStyleClass().add("admin-report-product-cell");
        priceLabel.setPrefWidth(110);

        Label valueLabel = new Label(formatPrice(value));
        valueLabel.getStyleClass().add("admin-report-product-value");
        valueLabel.setPrefWidth(120);

        HBox row = new HBox(name, stock, priceLabel, valueLabel);
        row.getStyleClass().add("admin-report-product-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox customerByCountryPanel(List<User> users) {
        return customerHeatmapPanel(users);
    }

    private VBox customerHeatmapPanel(List<User> users) {
        YearMonth start = YearMonth.now().minusMonths(9);
        YearMonth[] months = new YearMonth[10];
        for (int i = 0; i < months.length; i++) {
            months[i] = start.plusMonths(i);
        }

        String[] seriesNames = {"Active Customers", "Inactive Customers", "Blocked Customers", "New Customers"};
        String[] baseColors = {"#0d9488", "#14b8a6", "#5eead4", "#a7f3d0"};
        int[][] data = customerHeatmapData(users, months);

        // -- Legend row -----------------------------------------------------------
        HBox legend = new HBox(16);
        legend.setPadding(new Insets(0, 0, 10, 0));
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setAlignment(Pos.CENTER_LEFT);

        // First row of legend: 3 items
        HBox legendRow1 = new HBox(16);
        legendRow1.setAlignment(Pos.CENTER_LEFT);
        HBox legendRow2 = new HBox(16);
        legendRow2.setAlignment(Pos.CENTER_LEFT);

        for (int s = 0; s < seriesNames.length; s++) {
            Region dot = new Region();
            dot.setPrefSize(10, 3);
            dot.setMinSize(10, 3);
            dot.setMaxSize(10, 3);
            dot.setStyle("-fx-background-color: " + baseColors[s] + "; -fx-background-radius: 2;");

            Label lbl = new Label(seriesNames[s]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");

            HBox item = new HBox(5, dot, lbl);
            item.setAlignment(Pos.CENTER_LEFT);

            if (s < 3) legendRow1.getChildren().add(item);
            else       legendRow2.getChildren().add(item);
        }

        VBox legendBox = new VBox(4, legendRow1, legendRow2);

        // -- Y-axis labels (100%, 80%, 60%, 40%, 20%) -----------------------------
        String[] yLabels = {"100%", "80%", "60%", "40%", "20%"};

        // -- Grid layout: yLabels column + heatmap cells --------------------------
        final double CELL_SIZE = 26;
        final double CELL_GAP  = 4;
        final int ROWS = 5;   // percentage bands
        final int COLS = months.length;

        GridPane grid = new GridPane();
        grid.setHgap(CELL_GAP);
        grid.setVgap(CELL_GAP);
        grid.setAlignment(Pos.CENTER_LEFT);

        // Y-axis labels (column 0)
        for (int r = 0; r < ROWS; r++) {
            Label yLbl = new Label(yLabels[r]);
            yLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-min-width: 32px;");
            yLbl.setAlignment(Pos.CENTER_RIGHT);
            GridPane.setHalignment(yLbl, javafx.geometry.HPos.RIGHT);
            grid.add(yLbl, 0, r);
        }

        // Heatmap cells (columns 1..COLS)
        // We map each data series to a row band based on value ranges
        // For each column (month), for each row band (0=100%, 4=20%),
        // find which series has the highest value in that band and color accordingly
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                // Threshold: row 0 = values 80-100, row 1 = 60-80, etc.
                int lo = 100 - (row + 1) * 20;
                int hi = 100 - row * 20;

                // Find best matching series for this cell
                int bestSeries = -1;
                int bestVal = -1;
                for (int s = 0; s < data.length; s++) {
                    int v = data[s][col];
                    if (v >= lo && (row == 0 ? v <= hi : v < hi) && v > bestVal) {
                        bestVal = v;
                        bestSeries = s;
                    }
                }

                // Secondary: if no exact match, use proximity
                if (bestSeries == -1) {
                    for (int s = 0; s < data.length; s++) {
                        int v = data[s][col];
                        int dist = Math.min(Math.abs(v - lo), Math.abs(v - hi));
                        if (dist < 10 && v > bestVal) {
                            bestVal = v;
                            bestSeries = s;
                        }
                    }
                }

                javafx.scene.shape.Rectangle cell = new javafx.scene.shape.Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setArcWidth(5);
                cell.setArcHeight(5);

                if (bestSeries >= 0) {
                    // Intensity based on how strong the value is
                    double intensity = bestVal / 100.0;
                    String hex = baseColors[bestSeries];
                    Color base = Color.web(hex);
                    // Blend with white based on intensity
                    Color blended = base.interpolate(Color.web("#e6fffa"), 1.0 - intensity * 0.85);
                    cell.setFill(blended);
                } else {
                    cell.setFill(Color.web("#f0fdf9"));
                }

                grid.add(cell, col + 1, row);
            }
        }

        // -- X-axis month labels ---------------------------------------------------
        for (int col = 0; col < COLS; col++) {
            Label xLbl = new Label(months[col].format(DateTimeFormatter.ofPattern("MMM")));
            xLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");
            xLbl.setAlignment(Pos.CENTER);
            xLbl.setMinWidth(CELL_SIZE);
            GridPane.setHalignment(xLbl, javafx.geometry.HPos.CENTER);
            grid.add(xLbl, col + 1, ROWS);
        }

        VBox content = new VBox(12, legendBox, grid);
        content.setPadding(new Insets(4, 0, 0, 0));

        return reportPanel("Customer's Report", null, content, "admin-report-customer-panel");
    }

    private BigDecimal revenueForMonth(List<Order> orders, YearMonth month) {
        return orders.stream()
                .filter(order -> order.getDate() != null && YearMonth.from(order.getDate()).equals(month))
                .map(order -> order.getTotalPrice() == null ? BigDecimal.ZERO : order.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int ordersForMonth(List<Order> orders, YearMonth month) {
        return (int) orders.stream()
                .filter(order -> order.getDate() != null && YearMonth.from(order.getDate()).equals(month))
                .count();
    }

    private BigDecimal averageForMonth(List<Order> orders, YearMonth month) {
        int count = ordersForMonth(orders, month);
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return revenueForMonth(orders, month).divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private String formatChange(BigDecimal current, BigDecimal previous) {
        return percentText(percentChange(current, previous)) + " from last month";
    }

    private String formatChange(int current, int previous) {
        return percentText(percentChange(BigDecimal.valueOf(current), BigDecimal.valueOf(previous))) + " from last month";
    }

    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        current = current == null ? BigDecimal.ZERO : current;
        previous = previous == null ? BigDecimal.ZERO : previous;
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous.abs(), 1, java.math.RoundingMode.HALF_UP);
    }

    private String percentText(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        String sign = safeValue.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return sign + safeValue.setScale(1, java.math.RoundingMode.HALF_UP) + "%";
    }

    private int[][] customerHeatmapData(List<User> users, YearMonth[] months) {
        int[][] raw = new int[4][months.length];
        for (int col = 0; col < months.length; col++) {
            YearMonth month = months[col];
            for (User user : users) {
                YearMonth createdMonth = user.getCreatedAt() == null ? null : YearMonth.from(user.getCreatedAt());
                boolean existedByMonth = createdMonth == null || !createdMonth.isAfter(month);
                boolean createdThisMonth = createdMonth != null && createdMonth.equals(month);
                if (user.getStatus() == UserStatus.ACTIVE && existedByMonth) raw[0][col]++;
                if (user.getStatus() == UserStatus.INACTIVE && existedByMonth) raw[1][col]++;
                if (user.getStatus() == UserStatus.BLOCKED && existedByMonth) raw[2][col]++;
                if (createdThisMonth) raw[3][col]++;
            }
        }

        int max = 0;
        for (int[] row : raw) {
            for (int value : row) {
                max = Math.max(max, value);
            }
        }
        if (max == 0) {
            return raw;
        }

        int[][] normalized = new int[raw.length][months.length];
        for (int row = 0; row < raw.length; row++) {
            for (int col = 0; col < months.length; col++) {
                normalized[row][col] = (int) Math.round(raw[row][col] * 100.0 / max);
            }
        }
        return normalized;
    }

    private HBox reportLegendChip(String text, String accent) {
        Region dot = new Region();
        dot.getStyleClass().addAll("admin-report-dot", accent);
        Label label = new Label(text);
        label.getStyleClass().add("admin-report-legend-label");
        HBox chip = new HBox(6, dot, label);
        chip.setAlignment(Pos.CENTER_LEFT);
        return chip;
    }

    private Region reportMapPin(String accent, double x, double y) {
        Region pin = new Region();
        pin.getStyleClass().addAll("admin-report-map-pin", accent);
        pin.setLayoutX(x);
        pin.setLayoutY(y);
        return pin;
    }

    private VBox reportMiniStat(String label, String value) {
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("admin-report-mini-value");
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("admin-report-mini-label");
        VBox stat = new VBox(2, valueNode, labelNode);
        stat.getStyleClass().add("admin-report-mini-stat");
        return stat;
    }

    private VBox reportKpiCard(String label, String value, String detail, String iconText, String accentClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().addAll("admin-report-kpi-icon", accentClass);

        Label overline = new Label(label.toUpperCase());
        overline.getStyleClass().add("admin-card-overline");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().addAll("admin-report-kpi-value", accentClass);

        Label detailNode = new Label(detail);
        detailNode.getStyleClass().add("subtitle");
        detailNode.setWrapText(true);

        HBox top = new HBox(10, icon, overline);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, top, valueNode, detailNode);
        card.getStyleClass().addAll("admin-report-kpi-card", accentClass);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
//    private VBox miniStateCard(String label, String value, String trend) {
//        Label lblLabel = new Label(label.toUpperCase());
//        lblLabel.getStyleClass().add("admin-dialog-overline");
//
//        Label lblValue = new Label(value);
//        lblValue.getStyleClass().add("stat-number-small");
//        lblValue.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");
//
//        Label lblTrend = new Label(trend);
//        lblTrend.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
//
//        VBox card = new VBox(6, lblLabel, lblValue, lblTrend);
//        card.getStyleClass().add("admin-surface-card");
//        card.setPadding(new Insets(15));
//        card.setMinWidth(180);
//        return card;
//    }

    private void showLogs() {
        var logs = FXCollections.observableArrayList(controller.logs());
        FilteredList<String> filteredLogs = new FilteredList<>(logs, log -> true);

        TextField search = Ui.text("Search logs, users, actions...");
        search.getStyleClass().add("admin-log-search");

        Pagination pagination = new Pagination();
        pagination.getStyleClass().add("table-pagination");

        ListView<String> list = new ListView<>();
        list.getStyleClass().add("admin-data-list");
        list.getStyleClass().add("admin-log-list");
        // Hauteur fixe - scroll sur la PAGE, pas dans la liste
        list.setPrefHeight(PAGE_SIZE * 72.0 + 20);
        list.setMinHeight(PAGE_SIZE * 72.0 + 20);
        list.setMaxHeight(PAGE_SIZE * 72.0 + 20);
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(null);
                setGraphic(logRow(item));
            }
        });

        Runnable reloadLogs = () -> {
            List<String> all = filteredLogs;
            int pages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
            if (pagination.getPageCount() != pages) pagination.setPageCount(pages);
            int idx = Math.min(pagination.getCurrentPageIndex(), pages - 1);
            list.setItems(FXCollections.observableArrayList(
                    all.subList(idx * PAGE_SIZE, Math.min(idx * PAGE_SIZE + PAGE_SIZE, all.size()))));
        };

        reloadLogs.run();
        pagination.currentPageIndexProperty().addListener((o, old, val) -> reloadLogs.run());

        search.textProperty().addListener((observable, oldValue, value) -> {
            String needle = value == null ? "" : value.trim().toLowerCase();
            filteredLogs.setPredicate(log -> needle.isBlank() || log.toLowerCase().contains(needle));
            pagination.setCurrentPageIndex(0);
            reloadLogs.run();
        });

        Button refresh = Ui.secondary("Refresh");
        refresh.getStyleClass().add("admin-log-toolbar-button");
        refresh.setOnAction(e -> showLogs());

        Button export = Ui.secondary("Export CSV");
        export.getStyleClass().add("admin-log-export-button");
        export.setOnAction(e -> exportLogs(filteredLogs));

        HBox summary = new HBox(16,
                logSummaryCard("Total events", String.valueOf(logs.size()), "Last 100 system actions", "T", "total"),
                logSummaryCard("Security", String.valueOf(countLogs(logs, "LOGIN", "ACTIVATE")), "Access and account activity", "S", "security"),
                logSummaryCard("Catalog", String.valueOf(countLogs(logs, "PRODUCT", "CATEGORY", "STOCK")), "Inventory and product updates", "C", "catalog"),
                logSummaryCard("Orders", String.valueOf(countLogs(logs, "ORDER")), "Customer order lifecycle", "O", "order")
        );
        summary.getStyleClass().add("admin-log-summary-row");

        HBox toolbar = Ui.toolbar(search, refresh, export);
        toolbar.getStyleClass().add("admin-log-toolbar");
        HBox.setHgrow(search, Priority.ALWAYS);

        BorderPane listShell = new BorderPane(list);
        listShell.setBottom(pagination);
        BorderPane.setMargin(pagination, new Insets(8, 0, 0, 0));

        VBox body = new VBox(16,
                summary,
                surfaceCard(sectionTitle("Live activity", "Filter, review and export recent events with a cleaner admin flow.", "A"), toolbar, listShell)
        );
        setContent("System logs", body);
    }

    private VBox logSummaryCard(String label, String value, String detail, String iconText, String accentClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("admin-log-summary-icon");
        icon.getStyleClass().add(accentClass);

        Label labelNode = new Label(label.toUpperCase());
        labelNode.getStyleClass().add("admin-card-overline");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("admin-log-summary-value");

        Label detailNode = new Label(detail);
        detailNode.getStyleClass().add("subtitle");
        detailNode.setWrapText(true);

        HBox top = new HBox(10, icon, labelNode);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, top, valueNode, detailNode);
        card.getStyleClass().add("admin-log-summary-card");
        card.getStyleClass().add(accentClass);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private int countLogs(List<String> logs, String... keywords) {
        int count = 0;
        for (String log : logs) {
            String upper = log == null ? "" : log.toUpperCase();
            for (String keyword : keywords) {
                if (upper.contains(keyword)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private HBox logRow(String rawLog) {
        String[] parts = rawLog.split("\\s\\|\\s", 4);
        String time = parts.length > 0 ? parts[0] : "";
        String actor = parts.length > 1 ? parts[1] : "system";
        String action = parts.length > 2 ? parts[2] : "SYSTEM_EVENT";
        String details = parts.length > 3 ? parts[3] : rawLog;

        Label icon = new Label(logInitial(action));
        icon.getStyleClass().add("admin-log-row-icon");
        icon.getStyleClass().add(logAccentClass(action));

        Label actionBadge = new Label(action.replace('_', ' '));
        actionBadge.getStyleClass().add("admin-log-action-badge");
        actionBadge.getStyleClass().add(logAccentClass(action));
        actionBadge.setWrapText(true);

        Label actorLabel = new Label(actor);
        actorLabel.getStyleClass().add("admin-log-actor");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("admin-log-time");

        HBox meta = new HBox(10, actorLabel, timeLabel);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label detailsLabel = new Label(details);
        detailsLabel.getStyleClass().add("admin-log-details");
        detailsLabel.setWrapText(true);

        VBox content = new VBox(8, meta, actionBadge, detailsLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox row = new HBox(16, icon, content);
        row.getStyleClass().add("admin-log-row");
        row.getStyleClass().add(logAccentClass(action));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String logInitial(String action) {
        if (action == null || action.isBlank()) {
            return "A";
        }
        return action.substring(0, 1).toUpperCase();
    }

    private String logAccentClass(String action) {
        String upper = action == null ? "" : action.toUpperCase();
        if (upper.contains("DELETE") || upper.contains("FAILED") || upper.contains("BLOCK")) {
            return "danger";
        }
        if (upper.contains("LOGIN") || upper.contains("ACTIVATE") || upper.contains("EMAIL")) {
            return "security";
        }
        if (upper.contains("ORDER")) {
            return "order";
        }
        if (upper.contains("PRODUCT") || upper.contains("CATEGORY") || upper.contains("STOCK")) {
            return "catalog";
        }
        return "neutral";
    }

    private void exportLogs(List<String> logs) {
        StringBuilder csv = new StringBuilder("date,actor,action,details\n");
        for (String log : logs) {
            String[] parts = log.split("\\s\\|\\s", 4);
            csv.append(csvCell(parts.length > 0 ? parts[0] : ""))
                    .append(csvCell(parts.length > 1 ? parts[1] : ""))
                    .append(csvCell(parts.length > 2 ? parts[2] : ""))
                    .append(csvCell(parts.length > 3 ? parts[3] : log))
                    .append('\n');
        }
        exportCsv("system-logs-export.csv", csv.toString());
    }

    private TableView<Product> productTable() {
        TableView<Product> table = new TableView<>();
        decorateTable(table, 96);
        table.getColumns().add(productImageColumn());
        table.getColumns().add(productInfoColumn());
        table.getColumns().add(productCategoryColumn());
        table.getColumns().add(productPriceColumn());
        table.getColumns().add(productStockColumn());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private <T> void decorateTable(TableView<T> table, double rowHeight) {
        table.getStyleClass().add("admin-data-table");
        table.setFixedCellSize(rowHeight);
    }

    private TableColumn<Product, String> productImageColumn() {
        TableColumn<Product, String> column = new TableColumn<>("Image");
        column.setPrefWidth(120);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImageUrl()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                StackPane frame = new StackPane();
                frame.getStyleClass().add("admin-image-frame");
                frame.setPrefSize(68, 68);

                ImageView imageView = new ImageView();
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);

                if (imageUrl != null && !imageUrl.isBlank()) {
                    try {
                        imageView.setImage(new Image(resolveImage(imageUrl), true));
                        frame.getChildren().add(imageView);
                    } catch (Exception e) {
                        frame.getChildren().add(imagePlaceholder());
                    }
                } else {
                    frame.getChildren().add(imagePlaceholder());
                }
                setText(null);
                setGraphic(frame);
            }
        });
        return column;
    }

    private TableColumn<Product, String> productInfoColumn() {
        TableColumn<Product, String> column = new TableColumn<>("Product");
        column.setPrefWidth(340);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Product product = getTableView().getItems().get(getIndex());
                Label name = new Label(product.getName());
                name.getStyleClass().add("admin-product-name");
                name.setWrapText(true);

                String descriptionText = product.getDescription() == null || product.getDescription().isBlank()
                        ? "No description available"
                        : product.getDescription();
                Label description = new Label(descriptionText);
                description.getStyleClass().add("admin-product-description");
                description.setWrapText(true);
                description.setMaxWidth(320);

                VBox content = new VBox(6, name, description);
                content.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(content);
            }
        });
        return column;
    }

    private TableColumn<Product, String> productCategoryColumn() {
        TableColumn<Product, String> column = new TableColumn<>("Category");
        column.setPrefWidth(170);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label((item == null || item.isBlank()) ? "Uncategorized" : item);
                badge.getStyleClass().add("admin-category-badge");
                setText(null);
                setGraphic(badge);
            }
        });
        return column;
    }

    private TableColumn<Product, String> productPriceColumn() {
        TableColumn<Product, String> column = new TableColumn<>("Price");
        column.setPrefWidth(130);
        column.setCellValueFactory(data -> new SimpleStringProperty(formatPrice(data.getValue().getPrice())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label price = new Label(item);
                price.getStyleClass().add("admin-price-label");
                setText(null);
                setGraphic(price);
            }
        });
        return column;
    }

    private TableColumn<Product, String> productStockColumn() {
        TableColumn<Product, String> column = new TableColumn<>("Stock");
        column.setPrefWidth(140);
        column.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Product product = getTableView().getItems().get(getIndex());
                Label stock = new Label(product.getQuantity() + " units");
                stock.getStyleClass().add(product.getQuantity() <= 5 ? "admin-stock-badge-low" : "admin-stock-badge-ok");
                setText(null);
                setGraphic(stock);
            }
        });
        return column;
    }

    private BarChart<String, Number> inventoryChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setTitle(null);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        controller.products("").stream().limit(8)
                .forEach(product -> series.getData().add(new XYChart.Data<>(product.getName(), product.getQuantity())));
        chart.getData().add(series);
        return chart;
    }

    private PieChart categoryChart() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setTitle(null);
        controller.products("").stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        product -> product.getCategoryName() == null || product.getCategoryName().isBlank()
                                ? "Uncategorized"
                                : product.getCategoryName(),
                        java.util.stream.Collectors.counting()
                ))
                .forEach((category, count) -> chart.getData().add(new PieChart.Data(category == null ? "Uncategorized" : category, count)));
        return chart;
    }

    private PieChart orderStatusChart() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setTitle(null);
        controller.orders().stream()
                .collect(java.util.stream.Collectors.groupingBy(order -> order.getStatus().name(), LinkedHashMap::new, java.util.stream.Collectors.counting()))
                .forEach((status, count) -> chart.getData().add(new PieChart.Data(status, count)));
        return chart;
    }

    private BarChart<String, Number> usersByRoleChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setTitle(null);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        controller.users().stream()
                .collect(java.util.stream.Collectors.groupingBy(user -> user.getRole().name(), LinkedHashMap::new, java.util.stream.Collectors.counting()))
                .forEach((role, count) -> series.getData().add(new XYChart.Data<>(role.replace('_', ' '), count)));
        chart.getData().add(series);
        return chart;
    }

    private void productDialog(Product product) {
        // 1. D-claration et initialisation des champs (R-sout "Cannot resolve symbol")
        TextField name = Ui.text("Product Name");
        TextField description = Ui.text("Description");
        TextField imageUrl = Ui.text("Image URL or /images/... path");
        TextField price = Ui.text("Price");
        TextField quantity = Ui.text("Quantity");

        // Dans productDialog...
        ComboBox<Category> category = new ComboBox<>(FXCollections.observableArrayList(controller.categories()));
        category.setPromptText("Select Category");
        category.setMaxWidth(Double.MAX_VALUE);

// Assurez-vous d'ajouter cette classe pour qu'elle r-cup-re les styles de bordure/padding de votre CSS
        category.getStyleClass().add("combo-box");

        // Style commun pour les inputs
        styleDialogInput(name, description, imageUrl, price, quantity, category);

        // 2. Remplissage si on est en mode "Edition"
        if (product != null) {
            name.setText(product.getName());
            description.setText(product.getDescription());
            imageUrl.setText(product.getImageUrl());
            price.setText(String.valueOf(product.getPrice()));
            quantity.setText(String.valueOf(product.getQuantity()));
            category.getItems().stream()
                    .filter(c -> c.getId() == product.getCategoryId())
                    .findFirst()
                    .ifPresent(category::setValue);
        }

        // 3. Bouton Sauvegarder
        Button save = Ui.primary("Save Product");
        save.getStyleClass().add("admin-dialog-primary-button");
        save.setOnAction(e -> {
            try {
                Category selected = category.getValue();
                Product next = new Product(
                        product == null ? 0 : product.getId(),
                        name.getText(),
                        description.getText(),
                        imageUrl.getText(),
                        new java.math.BigDecimal(price.getText()),
                        Integer.parseInt(quantity.getText()),
                        selected == null ? 0 : selected.getId(),
                        selected == null ? "" : selected.getName()
                );
                run(() -> controller.saveProduct(next));
                save.getScene().getWindow().hide();
            } catch (Exception ex) {
                Ui.error(new RuntimeException("Please check your inputs (Price and Quantity must be numbers)"));
            }
        });

        // 4. Construction du formulaire avec le nouveau design
        VBox form = dialogForm(
                dialogSection("Product Details", "Keep each catalogue item complete and consistent."),

                dialogField("Product Name", name),

                dialogRow(
                        dialogField("Category", category),
                        dialogField("Price ($)", price)
                ),

                dialogRow(
                        dialogField("Initial Stock", quantity),
                        dialogField("Image Source", imageUrl)
                ),

                dialogField("Description", description),

                dialogActions(save)
        );
        form.getStyleClass().add("admin-product-dialog-card");

        showAdminDialog(form, product == null ? "New product" : "Edit product", true);
    }

    private void categoryDialog() {
        TextField name = Ui.text("Category name");
        styleDialogInput(name);
        Button create = Ui.primary("Create category");
        create.getStyleClass().add("admin-dialog-primary-button");
        create.setOnAction(e -> {
            run(() -> controller.createCategory(name.getText()));
            if (create.getScene() != null) {
                create.getScene().getWindow().hide();
            }
        });
        VBox form = dialogForm(
                dialogSection("New category", "Create a cleaner product grouping."),
                dialogField("Category name", name),
                dialogActions(create)
        );
        showAdminDialog(form, "New category", true);
    }

    private void createStockManagerDialog() {
        TextField name = Ui.text("Full name");
        TextField email = Ui.text("Email");
        styleDialogInput(name, email);
        Button create = Ui.primary("Create and email activation");
        create.getStyleClass().add("admin-dialog-primary-button");
        create.setOnAction(e -> {
            try {
                String token = controller.createStockManager(name.getText(), email.getText());
                if (create.getScene() != null) {
                    create.getScene().getWindow().hide();
                }
                modernPopup(
                        "Account Created",
                        "Activation email sent successfully.",
                        true
                );
            } catch (MessagingException | RuntimeException ex) {
                if (create.getScene() != null) {
                    create.getScene().getWindow().hide();
                }
                modernPopup(
                        "Email Already Exists",
                        "This email is already used.",
                        false
                );
            }
        });
        VBox form = dialogForm(
                dialogSection("Create stock manager", "The account starts inactive and receives a real activation email."),
                dialogField("Full name", name),
                dialogField("Email address", email),
                dialogActions(create)
        );
        showAdminDialog(form, "Create stock manager", true);
    }

    private void modernPopup(String title, String message, boolean success) {

        Alert alert = new Alert(
                success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18px;" +
                        "-fx-border-radius: 18px;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.2, 0, 5);"
        );

        Label content = (Label) dialogPane.lookup(".content.label");

        content.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #475569;" +
                        "-fx-padding: 10 0 10 0;"
        );

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.setText("Close");

        okButton.setStyle(
                "-fx-background-color: " + (success ? "#2563eb" : "#ef4444") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 8 22;" +
                        "-fx-cursor: hand;"
        );

        alert.showAndWait();
    }

    private void updateUser(TableView<User> table, UserStatus status,
                            List<User>[] src, Pagination pagination, Runnable reload) {
        User user = table.getSelectionModel().getSelectedItem();
        if (user != null) {
            run(() -> controller.updateUserStatus(user, status));
            src[0] = controller.users();
            pagination.setCurrentPageIndex(0);
            reload.run();
        }
    }

    private void setContent(String title, javafx.scene.Node content) {
        VBox page = new VBox(18, Ui.header(controller.currentUser(), title), content);
        page.setPadding(new Insets(28));
        VBox.setVgrow(content, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("admin-page-scroll");
        root.setCenter(scrollPane);
    }

    private void setReportContent(javafx.scene.Node content) {
        VBox page = new VBox(10, Ui.header(controller.currentUser(), "Reports"), content);
        page.setPadding(new Insets(22, 4, 22, 4));
        page.getStyleClass().add("admin-report-dark-shell");
        VBox.setVgrow(content, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().addAll("admin-page-scroll", "admin-report-dark-scroll");
        root.setCenter(scrollPane);
    }

    private void showAdminDialog(VBox form) {
        showAdminDialog(form, null, false);
    }

    private void showAdminDialog(VBox form, String title, boolean nativeChrome) {
        Stage stage = new Stage();
        Window owner = root.getScene().getWindow();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (nativeChrome) {
            stage.setTitle(title == null || title.isBlank() ? "Dialog" : title);
        } else {
            stage.initStyle(StageStyle.TRANSPARENT);
        }

        if (!nativeChrome) {
            // 1. Bouton de fermeture (d-j- configur- dans votre CSS)
            Button close = new Button("X");
            close.getStyleClass().add("admin-dialog-close");
            close.setOnAction(e -> stage.close());

            HBox chrome = new HBox(close);
            chrome.getStyleClass().add("admin-dialog-chrome");
            chrome.setAlignment(Pos.TOP_RIGHT);

            // 2. On ajoute le bouton au formulaire existant (qui a d-j- la classe .admin-dialog-card)
            form.getChildren().add(0, chrome);
        }
        form.setMinWidth(680);
        form.setMaxWidth(680);

        // 3. Un SEUL ScrollPane pour tout le contenu
        // On enl-ve les prefViewportHeight/Width qui forcent la bo-te grise
        ScrollPane modalScroll = new ScrollPane(form);
        modalScroll.setFitToWidth(true);
        modalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        modalScroll.getStyleClass().add("admin-dialog-scroll");

        // Limite la hauteur totale - 90% de la fen-tre pour -viter que -a sorte de l'-cran
        modalScroll.setMaxHeight(owner.getHeight() * 0.9);

        // 4. Le Shell (fond sombre/transparent)
        StackPane shell = new StackPane(modalScroll);
        shell.getStyleClass().add("admin-dialog-shell");
        shell.setAlignment(Pos.CENTER);
        // Un padding l-ger pour que l'ombre de la carte blanche ne soit pas coup-e
        shell.setPadding(nativeChrome ? new Insets(0) : new Insets(40));

        Scene scene = new Scene(shell);
        scene.setFill(nativeChrome ? Color.WHITE : Color.TRANSPARENT);
        AppTheme.apply(scene);

        stage.setScene(scene);

        // Centrage parfait sur la fen-tre principale
        stage.setOnShown(e -> {
            stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
        });

        stage.showAndWait();
    }

    private VBox dialogForm(javafx.scene.Node... nodes) {
        VBox box = new VBox(16, nodes);
        box.getStyleClass().add("admin-dialog-card");
        return box;
    }

    private VBox dialogSection(String title, String subtitle) {
        Label heading = new Label(title);
        heading.getStyleClass().add("admin-dialog-title");
        heading.setWrapText(true);
        Label copy = new Label(subtitle);
        copy.getStyleClass().add("admin-dialog-subtitle");
        copy.setWrapText(true);
        Region line = new Region();
        line.getStyleClass().add("admin-dialog-divider");
        line.setPrefHeight(1);
        return new VBox(8, heading, copy, line);
    }

    private VBox dialogField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("admin-dialog-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        VBox box = new VBox(8, label, field);
        box.getStyleClass().add("admin-dialog-field");
        return box;
    }

    private HBox dialogRow(javafx.scene.Node... nodes) {
        HBox row = new HBox(16, nodes);
        row.getStyleClass().add("admin-dialog-row");
        for (javafx.scene.Node node : nodes) {
            if (node instanceof Region region) {
                HBox.setHgrow(region, Priority.ALWAYS);
                region.setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private HBox dialogActions(Button primaryAction) {
        HBox actions = new HBox(primaryAction);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("admin-dialog-actions");
        primaryAction.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(primaryAction, Priority.ALWAYS);
        primaryAction.setPrefHeight(54);
        return actions;
    }

    private void styleDialogInput(javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.getStyleClass().add("admin-dialog-input");
        }
    }

    private Label imagePlaceholder() {
        Label placeholder = new Label("IMG");
        placeholder.getStyleClass().add("admin-image-placeholder");
        return placeholder;
    }

    private String resolveImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "";
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file:")) {
            return imageUrl;
        }
        if (imageUrl.startsWith("/")) {
            var resource = getClass().getResource(imageUrl);
            if (resource != null) {
                return resource.toExternalForm();
            }
        }
        return new File(imageUrl).toURI().toString();
    }

    private void exportProducts(List<Product> products) {
        StringBuilder csv = new StringBuilder("id,name,category,price,quantity,imageUrl,description\n");
        for (Product product : products) {
            csv.append(csvCell(product.getId()))
                    .append(csvCell(product.getName()))
                    .append(csvCell(product.getCategoryName()))
                    .append(csvCell(product.getPrice()))
                    .append(csvCell(product.getQuantity()))
                    .append(csvCell(product.getImageUrl()))
                    .append(csvCell(product.getDescription()))
                    .append('\n');
        }
        exportCsv("products-export.csv", csv.toString());
    }

    private void exportUsers(List<User> users) {
        StringBuilder csv = new StringBuilder("id,name,email,role,status,phone,address,createdAt\n");
        for (User user : users) {
            csv.append(csvCell(user.getId()))
                    .append(csvCell(user.getName()))
                    .append(csvCell(user.getEmail()))
                    .append(csvCell(user.getRole() == null ? "" : user.getRole().name()))
                    .append(csvCell(user.getStatus() == null ? "" : user.getStatus().name()))
                    .append(csvCell(user.getPhone()))
                    .append(csvCell(user.getDeliveryAddress()))
                    .append(csvCell(user.getCreatedAt() == null ? "" : user.getCreatedAt().format(EXPORT_DATE_FORMAT)))
                    .append('\n');
        }
        exportCsv("users-export.csv", csv.toString());
    }

    private void exportOrders(List<Order> orders) {
        StringBuilder csv = new StringBuilder("id,userId,customer,total,status,date\n");
        for (Order order : orders) {
            csv.append(csvCell(order.getId()))
                    .append(csvCell(order.getUserId()))
                    .append(csvCell(order.getCustomerName()))
                    .append(csvCell(order.getTotalPrice()))
                    .append(csvCell(order.getStatus() == null ? "" : order.getStatus().name()))
                    .append(csvCell(order.getDate() == null ? "" : order.getDate().format(EXPORT_DATE_FORMAT)))
                    .append('\n');
        }
        exportCsv("orders-export.csv", csv.toString());
    }

    private void exportCsv(String suggestedName, String content) {
        if (root == null || root.getScene() == null) {
            Ui.error(new IllegalStateException("Window is not ready for export."));
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        chooser.setInitialFileName(suggestedName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));
        File target = chooser.showSaveDialog(root.getScene().getWindow());
        if (target == null) {
            return;
        }
        try {
            Files.writeString(target.toPath(), content);
            Ui.info("Export completed", "File saved to " + target.getAbsolutePath());
        } catch (IOException e) {
            Ui.error(e);
        }
    }

    private String csvCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped + "\",";
    }

    private void selectNav(String text) {

        if (navBox == null || text == null) return;

        navBox.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(btn -> {
                    Object v = btn.getProperties().get("navText");
                    return v instanceof String && text.equals(v);
                })
                .findFirst()
                .ifPresent(this::setActiveNav);
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }

    private void run(Runnable action) {
        try {
            action.run();
            Ui.info("Done", "Action completed successfully.");
        } catch (RuntimeException e) {
            Ui.error(e);
        }
    }

    private <T> T safe(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            Ui.error(e);
            return null;
        }
    }

    private int parseInt(String value) {
        return Integer.parseInt(value);
    }

    private String formatPrice(BigDecimal value) {
        return value == null ? "$0.00" : "$" + value;
    }

    private BigDecimal parseMoney(String value) {
        return new BigDecimal(value);
    }

    private VBox buildReportDisplay(String rawReport) {
        VBox container = new VBox(16);
        container.getStyleClass().add("admin-report-body");

        if (rawReport == null || rawReport.isBlank()) {
            Label empty = new Label("No report data available.");
            empty.getStyleClass().add("subtitle");
            container.getChildren().add(empty);
            return container;
        }

        String[] lines = rawReport.split("\n");

        // -- Summary table -----------------------------------------
        List<String[]> summaryRows = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("Generated") || trimmed.contains("|")) continue;
            if (trimmed.equals(trimmed.toUpperCase())) continue;
            if (trimmed.contains(":")) {
                String[] parts = trimmed.split(":", 2);
                if (parts.length == 2 && !parts[1].isBlank())
                    summaryRows.add(new String[]{parts[0].trim(), parts[1].trim()});
            }
        }

        if (!summaryRows.isEmpty()) {
            TableView<String[]> summaryTable = new TableView<>();
            decorateTable(summaryTable, 48);
            summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

            TableColumn<String[], String> keyCol = new TableColumn<>("Metric");
            keyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
            keyCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-report-kv-key");
                    setGraphic(l);
                }
            });

            TableColumn<String[], String> valCol = new TableColumn<>("Value");
            valCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
            valCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-report-kv-value");
                    setGraphic(l);
                }
            });

            summaryTable.getColumns().addAll(keyCol, valCol);
            summaryTable.setItems(FXCollections.observableArrayList(summaryRows));

            double summaryHeight = summaryRows.size() * 48.0 + 58;
            summaryTable.setPrefHeight(summaryHeight);
            summaryTable.setMinHeight(summaryHeight);
            summaryTable.setMaxHeight(summaryHeight);

            Label summaryHeader = new Label("SUMMARY");
            summaryHeader.getStyleClass().add("admin-report-group-header");
            container.getChildren().addAll(summaryHeader, summaryTable);
        }

        // -- Inventory table ---------------------------------------
        List<String[]> inventoryRows = new ArrayList<>();
        boolean inInventory = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("INVENTORY")) { inInventory = true; continue; }
            if (!inInventory || !trimmed.contains("|")) continue;

            String[] parts = trimmed.split("\\|");
            String name     = parts.length > 0 ? parts[0].trim() : "";
            String category = parts.length > 1 ? parts[1].trim() : "";
            String qty      = "";
            String price    = "";

            for (int i = 2; i < parts.length; i++) {
                String p = parts[i].trim();
                if (p.toLowerCase().startsWith("qty"))   qty   = p;
                if (p.toLowerCase().startsWith("price")) price = p;
            }

            if (!name.isBlank())
                inventoryRows.add(new String[]{name, category, qty, price});
        }

        if (!inventoryRows.isEmpty()) {
            TableView<String[]> inventoryTable = new TableView<>();
            decorateTable(inventoryTable, 52);
            inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

            TableColumn<String[], String> nameCol = new TableColumn<>("Product");
            nameCol.setPrefWidth(350);
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
            nameCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-report-inventory-name");
                    l.setWrapText(true);
                    setGraphic(l);
                }
            });

            TableColumn<String[], String> catCol = new TableColumn<>("Category");
            catCol.setPrefWidth(180);
            catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
            catCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-report-inventory-chip");
                    setGraphic(l);
                }
            });

            TableColumn<String[], String> qtyCol = new TableColumn<>("Quantity");
            qtyCol.setPrefWidth(100);
            qtyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
            qtyCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-report-kv-value");
                    setGraphic(l);
                }
            });

            TableColumn<String[], String> priceCol = new TableColumn<>("Price");
            priceCol.setPrefWidth(100);
            priceCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
            priceCol.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); return; }
                    Label l = new Label(item);
                    l.getStyleClass().add("admin-price-label");
                    setGraphic(l);
                }
            });

            inventoryTable.getColumns().addAll(nameCol, catCol, qtyCol, priceCol);

            // -- Pagination comme les autres pages --------------------
            double rowH = 52;
            inventoryTable.setPrefHeight(PAGE_SIZE * rowH + 58);
            inventoryTable.setMinHeight(PAGE_SIZE * rowH + 58);
            inventoryTable.setMaxHeight(PAGE_SIZE * rowH + 58);

            Pagination inventoryPagination = new Pagination();
            inventoryPagination.getStyleClass().add("table-pagination");

            Runnable reloadInventory = () -> {
                int pages = Math.max(1, (int) Math.ceil(inventoryRows.size() / (double) PAGE_SIZE));
                if (inventoryPagination.getPageCount() != pages) inventoryPagination.setPageCount(pages);
                int idx = Math.min(inventoryPagination.getCurrentPageIndex(), pages - 1);
                inventoryTable.setItems(FXCollections.observableArrayList(
                        inventoryRows.subList(idx * PAGE_SIZE, Math.min(idx * PAGE_SIZE + PAGE_SIZE, inventoryRows.size()))));
            };
            reloadInventory.run();
            inventoryPagination.currentPageIndexProperty().addListener((o, old, val) -> reloadInventory.run());

            BorderPane inventoryShell = new BorderPane(inventoryTable);
            inventoryShell.setBottom(inventoryPagination);
            BorderPane.setMargin(inventoryPagination, new Insets(8, 0, 0, 0));

            Label inventoryHeader = new Label("INVENTORY");
            inventoryHeader.getStyleClass().add("admin-report-group-header");
            container.getChildren().addAll(inventoryHeader, inventoryShell);
        }

        return container;
    }

}
