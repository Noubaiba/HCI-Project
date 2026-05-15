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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

public class AdminDashboardView {
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final AdminController controller;
    private BorderPane root;
    private VBox navBox;
    private Button activeNavButton;

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


    private VBox nav() {
        // 1. Création du conteneur principal (Sidebar)
        VBox nav = new VBox(22);
        nav.getStyleClass().addAll("admin-sidebar", "card");
        nav.setPadding(new Insets(24));
        nav.setMinWidth(280);
        nav.setPrefWidth(280);

        // --- BLOC LOGO ET NOM (Fixe en haut) ---
        VBox branding = brandBlock();

        // Liste des boutons de navigation
        navBox = new VBox(10);
        navBox.getChildren().addAll(
                navButton("⌂", "Dashboard", this::showDashboard),
                navButton("◫", "Products", this::showProducts),
                navButton("◎", "Users", this::showUsers),
                navButton("≣", "Orders", this::showOrders),
                navButton("▣", "Reports", this::showReports),
                navButton("◌", "System logs", this::showLogs)
        );

        // Region pour pousser le bouton Logout vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bouton Logout
        Button logout = Ui.danger("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> controller.logout());

        // On ajoute tout directement dans la VBox 'nav' sans ScrollPane
        nav.getChildren().addAll(branding, navBox, spacer, logout);

        return nav;
    }

    private VBox brandBlock() {
        // Image du Logo
        ImageView logoImage = new ImageView();
        try {
            logoImage.setImage(new Image(getClass().getResource("/images/Stockify.png").toExternalForm()));
        } catch (Exception e) {
            // Fallback si l'image est manquante
        }
        logoImage.setFitWidth(45);
        logoImage.setFitHeight(45);
        logoImage.setPreserveRatio(true);

        // Nom du site
        Label siteName = new Label("Stockify");
        siteName.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #111827;");

        HBox logoRow = new HBox(12, logoImage, siteName);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(0, 0, 10, 0));

        // Ligne de séparation ou sous-titre
        Label eyebrow = new Label("ADMIN SPACE");
        eyebrow.getStyleClass().add("admin-sidebar-eyebrow");

        VBox brand = new VBox(8, logoRow, eyebrow);
        brand.setPadding(new Insets(0, 0, 15, 0));
        return brand;
    }



    private Button navButton(String iconText, String text, Runnable action) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("admin-sidebar-nav-icon");
        Label label = new Label(text);
        label.getStyleClass().add("admin-sidebar-nav-label");
        HBox content = new HBox(12, icon, label);
        content.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(content);
        button.getStyleClass().add("admin-nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.getProperties().put("navText", text);
        button.setOnAction(e -> {
            setActiveNav(button);
            action.run();
        });
        if (activeNavButton == null) {
            setActiveNav(button);
        }
        return button;
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

    private void showDashboard() {
        DashboardStats stats = safe(controller::stats);
        if (stats == null) {
            return;
        }

        HBox hero = new HBox(18, heroPanel(stats), quickActionsCard());
        HBox.setHgrow(hero.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(hero.getChildren().get(1), Priority.ALWAYS);

        GridPane statGrid = new GridPane();
        statGrid.setHgap(16);
        statGrid.setVgap(16);
        statGrid.add(metricCard("Products", String.valueOf(stats.getProducts()), "Catalogue actif"), 0, 0);
        statGrid.add(metricCard("Users", String.valueOf(stats.getUsers()), "Comptes plateforme"), 1, 0);
        statGrid.add(metricCard("Orders", String.valueOf(stats.getOrders()), "Commandes totales"), 2, 0);
        statGrid.add(metricCard("Revenue", "$" + stats.getRevenue(), "Chiffre d'affaires"), 3, 0);

        GridPane chartGrid = new GridPane();
        chartGrid.setHgap(16);
        chartGrid.setVgap(16);
        VBox categoryChart = chartCard("Products by category", categoryChart());
        VBox usersChart = chartCard("Users by role", usersByRoleChart());
        chartGrid.add(categoryChart, 0, 0);
        chartGrid.add(usersChart, 1, 0);
        GridPane.setHgrow(categoryChart, Priority.ALWAYS);
        GridPane.setHgrow(usersChart, Priority.ALWAYS);

        VBox insightPanel = surfaceCard(
                sectionTitle("Operational health", "A quick pulse of inventory, orders and catalogue balance."),
                progressLine("Low stock pressure", stats.getLowStock(), Math.max(1, stats.getProducts())),
                progressLine("Order activity", stats.getOrders(), Math.max(1, stats.getUsers() * 3)),
                progressLine("Catalog coverage", stats.getProducts(), Math.max(1, stats.getProducts() + stats.getLowStock()))
        );

        VBox body = new VBox(18,
                pageHero("Analytics overview", "A calmer, clearer snapshot of your admin activity.", "Admin analytics"),
                chartGrid,
                hero,
                statGrid,
                insightPanel
        );

        setContent("Analytics overview", body);
    }

    private VBox heroPanel(DashboardStats stats) {
        HBox heroTop = new HBox(12, iconBubble("✦", "admin-hero-icon"), new Label("TODAY"));
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
                sectionTitle("Quick actions", "Jump into the most common admin tasks.", "⚡"),
                products,
                users,
                orders
        );
        card.setPrefWidth(280);
        return card;
    }

    private VBox metricCard(String label, String value, String detail) {
        String iconText = switch (label) {
            case "Products" -> "📦";
            case "Users" -> "👥";
            case "Orders" -> "🧾";
            case "Revenue" -> "💰";
            default -> "•";
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
            case "Products by category" -> "◔";
            case "Users by role" -> "▥";
            case "Stock by product" -> "▤";
            case "Orders by status" -> "◕";
            default -> "•";
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

        VBox side = new VBox(8, infoChip("Clean admin flow"), infoChip("Faster decisions"));
        side.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hero = new HBox(18, copy, spacer, side);
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
        table.setItems(FXCollections.observableArrayList(controller.products("")));
        search.textProperty().addListener((o, old, value) -> table.setItems(FXCollections.observableArrayList(controller.products(value))));

        Button add = Ui.primary("+ Product");
        add.setOnAction(e -> {
            productDialog(null);
            table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
        });

        Button categories = Ui.secondary("+ Category");
        categories.setOnAction(e -> categoryDialog());

        Button delete = Ui.danger("Delete");
        delete.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && Ui.confirm("Delete product", "Delete " + selected.getName() + "?")) {
                run(() -> controller.deleteProduct(selected));
                table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
            }
        });

        Button export = Ui.secondary("Export CSV");
        export.setOnAction(e -> exportProducts(table.getItems()));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    productDialog(row.getItem());
                    table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
                }
            });
            return row;
        });

        VBox body = new VBox(16,
                pageHero("Product management", "Search, create, edit and clean up your catalogue from one workspace.", "Catalog operations"),
                surfaceCard(Ui.toolbar(search, add, categories, export, delete)),
                surfaceCard(table)
        );
        VBox.setVgrow(body.getChildren().get(body.getChildren().size() - 1), Priority.ALWAYS);
        setContent("Product management", body);
    }

    private void showUsers() {
        TableView<User> table = new TableView<>();
        decorateTable(table, 58);
        table.setItems(FXCollections.observableArrayList(controller.users()));
        table.getColumns().add(col("Name", User::getName));
        table.getColumns().add(col("Email", User::getEmail));
        table.getColumns().add(col("Role", u -> u.getRole().name()));
        table.getColumns().add(col("Status", u -> u.getStatus().name()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button stockManager = Ui.primary("+ Stock manager");
        stockManager.setOnAction(e -> {
            createStockManagerDialog();
            table.setItems(FXCollections.observableArrayList(controller.users()));
        });

        Button activate = Ui.secondary("Activate");
        activate.setOnAction(e -> updateUser(table, UserStatus.ACTIVE));
        Button deactivate = Ui.secondary("Deactivate");
        deactivate.setOnAction(e -> updateUser(table, UserStatus.INACTIVE));
        Button block = Ui.danger("Block");
        block.setOnAction(e -> updateUser(table, UserStatus.BLOCKED));
        Button export = Ui.secondary("Export CSV");
        export.setOnAction(e -> exportUsers(table.getItems()));

        VBox body = new VBox(16,
                pageHero("User administration", "Manage account states and onboard stock managers with a cleaner workflow.", "Access and accounts"),
                surfaceCard(Ui.toolbar(stockManager, activate, deactivate, export, block)),
                surfaceCard(table)
        );
        setContent("User administration", body);
    }

    private void showOrders() {
        TableView<Order> table = new TableView<>();
        decorateTable(table, 58);
        table.setItems(FXCollections.observableArrayList(controller.orders()));
        table.getColumns().add(col("Order", o -> "#" + o.getId()));
        table.getColumns().add(col("Customer", Order::getCustomerName));
        table.getColumns().add(col("Total", o -> "$" + o.getTotalPrice()));
        table.getColumns().add(col("Status", o -> o.getStatus().name()));
        table.getColumns().add(col("Date", o -> o.getDate().toString()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ComboBox<OrderStatus> status = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));
        status.setPromptText("Set status");
        Button update = Ui.primary("Update order");
        update.setOnAction(e -> {
            Order order = table.getSelectionModel().getSelectedItem();
            if (order != null && status.getValue() != null) {
                run(() -> controller.updateOrderStatus(order, status.getValue()));
                table.setItems(FXCollections.observableArrayList(controller.orders()));
            }
        });
        Button export = Ui.secondary("Export CSV");
        export.setOnAction(e -> exportOrders(table.getItems()));

        VBox body = new VBox(16,
                pageHero("All orders", "Keep the order pipeline tidy and update statuses without friction.", "Order flow"),
                surfaceCard(Ui.toolbar(status, update, export)),
                surfaceCard(table)
        );
        setContent("All orders", body);
    }

    private void showReports() {
        DashboardStats stats = safe(controller::stats);
        HBox kpiRow = new HBox(16);

        if (stats != null) {
            // Calcul sécurisé du panier moyen avec BigDecimal (Résout l'erreur de division)
            BigDecimal revenue = stats.getRevenue() != null ? stats.getRevenue() : BigDecimal.ZERO;
            BigDecimal average = revenue.divide(BigDecimal.valueOf(Math.max(1, stats.getOrders())), 2, java.math.RoundingMode.HALF_UP);

            kpiRow.getChildren().addAll(
                    miniStateCard("Taux de Conversion", "3.2%", "↑ 1.2% ce mois"),
                    miniStateCard("Panier Moyen", "$" + average, "Stable"),
                    miniStateCard("Ruptures de Stock", String.valueOf(stats.getLowStock()), "Action requise")
            );
            kpiRow.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        }


        // Zone de texte stylisée
        TextArea reportArea = new TextArea(controller.report());
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.getStyleClass().add("admin-report-display");
        VBox.setVgrow(reportArea, Priority.ALWAYS);

        Button refresh = Ui.secondary("Rafraîchir");
        refresh.setOnAction(e -> reportArea.setText(controller.report()));

        // Construction de la page avec un ScrollPane invisible
        VBox contentBody = new VBox(20,
                pageHero("Rapports d'activité", "Analyse détaillée des performances de la plateforme.", "Business Intelligence"),
                kpiRow,
                surfaceCard(
                        sectionTitle("Détails du rapport", "Données consolidées en temps réel"),
                        Ui.toolbar(refresh),
                        reportArea
                )
        );

        ScrollPane pageScroll = new ScrollPane(contentBody);
        pageScroll.getStyleClass().add("admin-page-scroll"); // Utilise le scroll sans barres
        pageScroll.setFitToWidth(true);

        setContent("Rapports", pageScroll);
    }
    private VBox miniStateCard(String label, String value, String trend) {
        Label lblLabel = new Label(label.toUpperCase());
        lblLabel.getStyleClass().add("admin-dialog-overline");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-number-small");
        lblValue.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label lblTrend = new Label(trend);
        lblTrend.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");

        VBox card = new VBox(6, lblLabel, lblValue, lblTrend);
        card.getStyleClass().add("admin-surface-card");
        card.setPadding(new Insets(15));
        card.setMinWidth(180);
        return card;
    }

    private void showLogs() {
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(controller.logs()));
        list.getStyleClass().add("admin-data-list");
        VBox body = new VBox(16,
                pageHero("System logs", "Recent system actions and admin events.", "Audit trail"),
                surfaceCard(list)
        );
        setContent("System logs", body);
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
                .collect(java.util.stream.Collectors.groupingBy(Product::getCategoryName, java.util.stream.Collectors.counting()))
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
        // 1. Déclaration et initialisation des champs (Résout "Cannot resolve symbol")
        TextField name = Ui.text("Product Name");
        TextField description = Ui.text("Description");
        TextField imageUrl = Ui.text("Image URL or /images/... path");
        TextField price = Ui.text("Price");
        TextField quantity = Ui.text("Quantity");

        // Dans productDialog...
        ComboBox<Category> category = new ComboBox<>(FXCollections.observableArrayList(controller.categories()));
        category.setPromptText("Select Category");
        category.setMaxWidth(Double.MAX_VALUE);

// Assurez-vous d'ajouter cette classe pour qu'elle récupère les styles de bordure/padding de votre CSS
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

        showAdminDialog(form);
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
        showAdminDialog(form);
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
                Ui.info("Stock manager created", "Activation email sent. Token: " + token);
                if (create.getScene() != null) {
                    create.getScene().getWindow().hide();
                }
            } catch (MessagingException | RuntimeException ex) {
                Ui.error(ex);
            }
        });
        VBox form = dialogForm(
                dialogSection("Create stock manager", "The account starts inactive and receives a real activation email."),
                dialogField("Full name", name),
                dialogField("Email address", email),
                dialogActions(create)
        );
        showAdminDialog(form);
    }

    private void updateUser(TableView<User> table, UserStatus status) {
        User user = table.getSelectionModel().getSelectedItem();
        if (user != null) {
            run(() -> controller.updateUserStatus(user, status));
            table.setItems(FXCollections.observableArrayList(controller.users()));
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

    private void showAdminDialog(VBox form) {
        Stage stage = new Stage();
        Window owner = root.getScene().getWindow();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        // 1. Bouton de fermeture (déjà configuré dans votre CSS)
        Button close = new Button("✕");
        close.getStyleClass().add("admin-dialog-close");
        close.setOnAction(e -> stage.close());

        HBox chrome = new HBox(close);
        chrome.setAlignment(Pos.TOP_RIGHT);

        // 2. On ajoute le bouton au formulaire existant (qui a déjà la classe .admin-dialog-card)
        form.getChildren().add(0, chrome);
        form.setMinWidth(680);
        form.setMaxWidth(680);

        // 3. Un SEUL ScrollPane pour tout le contenu
        // On enlève les prefViewportHeight/Width qui forcent la boîte grise
        ScrollPane modalScroll = new ScrollPane(form);
        modalScroll.setFitToWidth(true);
        modalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        modalScroll.getStyleClass().add("admin-dialog-scroll");

        // Limite la hauteur totale à 90% de la fenêtre pour éviter que ça sorte de l'écran
        modalScroll.setMaxHeight(owner.getHeight() * 0.9);

        // 4. Le Shell (fond sombre/transparent)
        StackPane shell = new StackPane(modalScroll);
        shell.getStyleClass().add("admin-dialog-shell");
        shell.setAlignment(Pos.CENTER);
        // Un padding léger pour que l'ombre de la carte blanche ne soit pas coupée
        shell.setPadding(new Insets(40));

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);
        AppTheme.apply(scene);

        stage.setScene(scene);

        // Centrage parfait sur la fenêtre principale
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
        Label overline = new Label("ADMIN FORM");
        overline.getStyleClass().add("admin-dialog-overline");
        Label badge = new Label("NEW");
        badge.getStyleClass().add("admin-dialog-badge");
        HBox top = new HBox(10, overline, badge);
        top.setAlignment(Pos.CENTER_LEFT);
        Label heading = new Label(title);
        heading.getStyleClass().add("admin-dialog-title");
        heading.setWrapText(true);
        Label copy = new Label(subtitle);
        copy.getStyleClass().add("admin-dialog-subtitle");
        copy.setWrapText(true);
        Region line = new Region();
        line.getStyleClass().add("admin-dialog-divider");
        line.setPrefHeight(1);
        return new VBox(8, top, heading, copy, line);
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
        if (navBox == null) {
            return;
        }
        for (javafx.scene.Node node : navBox.getChildren()) {
            if (node instanceof Button button && text.equals(button.getProperties().get("navText"))) {
                setActiveNav(button);
                return;
            }
        }
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
}
